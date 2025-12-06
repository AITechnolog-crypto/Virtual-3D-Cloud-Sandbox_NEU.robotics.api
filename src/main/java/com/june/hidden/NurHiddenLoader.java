package com.june.hidden;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.*;

/**
 * NurHiddenLoader — kompakter Loader/Packager für verschlüsselte Java-Payloads.
 *
 * Funktionen (Menü):
 * 1) Quellcode eingeben (vollqualifizierter Klassenname + Source); kompiliert in-memory
 *    und speichert die Klassenbytes AES‑GCM‑verschlüsselt in payload.bin.
 * 2) Verschlüsselte payload.bin laden, entschlüsseln, Klasse definieren und instanziieren;
 *    die Klasse muss das Interface {@link PayloadTask} implementieren; anschließend wird run(ctx) ausgeführt.
 * 3) Vorliegende .class‑Datei verschlüsselt als payload.bin packen.
 *
 * Sicherheit:
 * - Schlüsselableitung via PBKDF2WithHmacSHA256 aus Passphrase + Geräte‑Fingerprint.
 * - AES/GCM/NoPadding mit 12‑Byte IV und 128‑Bit Tag. Salt/IV werden im Dateiformat gespeichert.
 * - Keine Geheimnisse im Code hinterlegt. Dieses Tool ist für lokale Demo-/Entwicklungszwecke gedacht.
 */
public class NurHiddenLoader {
    static final String PAYLOAD_FILE = "payload.bin";
    static final int SALT_LEN = 16;
    static final int IV_LEN = 12;
    static final int GCM_TAG_BITS = 128;
    static final int PBKDF2_ITERS = 120_000;
    static final int KEY_BITS = 256;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        println("NurHiddenLoader bereit");
        println("1 Quellcode eingeben und als verschluesselte Payload speichern");
        println("2 Verschluesselte Payload laden und ausfuehren");
        println("3 Payload aus Klassenbytecode packen Dateiweg angeben");
        print("Auswahl: ");
        String choice = br.readLine();
        if ("1".equals(choice)) {
            handleSourceToPayload(br);
        } else if ("2".equals(choice)) {
            handleRunPayload(br);
        } else if ("3".equals(choice)) {
            handleClassFileToPayload(br);
        } else {
            println("Abbruch");
        }
    }

    private static void handleSourceToPayload(BufferedReader br) throws Exception {
        println("Vollqualifizierten Klassennamen angeben Beispiel: my.pkg.HiddenPayload");
        print("ClassName: ");
        String className = br.readLine().trim();
        println("Source eingeben Ende mit Zeile ::END");
        String source = readUntilEnd(br);

        println("Passphrase fuer Verschluesselung eingeben wird nicht angezeigt");
        String pass = readPassword(br);

        Map<String, byte[]> compiled = SourceCompiler.compileInMemory(className, source);
        if (!compiled.containsKey(className)) {
            println("Fehler Kompilation lieferte keine Bytes fuer " + className);
            return;
        }
        byte[] classBytes = compiled.get(className);
        writeEncryptedPayload(PAYLOAD_FILE, className, classBytes, pass);
        println("Payload gespeichert in " + PAYLOAD_FILE);
    }

    private static void handleClassFileToPayload(BufferedReader br) throws Exception {
        println("Pfad zu .class angeben und vollqualifizierten Namen z.B. build/classes/java/main/my/pkg/HiddenPayload.class");
        print("Pfad: ");
        String path = br.readLine().trim();
        print("ClassName: ");
        String className = br.readLine().trim();
        println("Passphrase fuer Verschluesselung eingeben wird nicht angezeigt");
        String pass = readPassword(br);
        byte[] classBytes;
        try (FileInputStream fis = new FileInputStream(path)) {
            classBytes = readAll(fis);
        }
        writeEncryptedPayload(PAYLOAD_FILE, className, classBytes, pass);
        println("Payload gespeichert in " + PAYLOAD_FILE);
    }

    private static void handleRunPayload(BufferedReader br) throws Exception {
        File f = new File(PAYLOAD_FILE);
        if (!f.exists()) {
            println("Keine payload.bin gefunden zuerst erstellen");
            return;
        }
        println("Passphrase eingeben um Payload zu laden wird nicht angezeigt");
        String pass = readPassword(br);
        EncryptedClassLoader loader = new EncryptedClassLoader(pass);
        Class<?> payloadClass = loader.loadEncrypted(PAYLOAD_FILE);
        Object task = payloadClass.getDeclaredConstructor().newInstance();
        if (!(task instanceof PayloadTask)) {
            println("Geladene Klasse implementiert nicht PayloadTask");
            return;
        }
        PayloadTask p = (PayloadTask) task;
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("timestamp", System.currentTimeMillis());
        ctx.put("device", DeviceFingerprint.fingerprint());
        p.run(ctx);
        println("Payload ausgefuehrt");
    }

    private static void writeEncryptedPayload(String outPath, String className, byte[] classBytes, String pass) throws Exception {
        byte[] salt = new byte[SALT_LEN];
        byte[] iv = new byte[IV_LEN];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(salt);
        sr.nextBytes(iv);

        byte[] key = deriveKey(pass, salt);
        byte[] ciphertext = encrypt(classBytes, key, iv);

        byte[] nameBytes = className.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(new byte[]{'N','H','V','8'});
        bos.write(salt);
        bos.write(iv);
        bos.write(shortToBytes((short) nameBytes.length));
        bos.write(nameBytes);
        bos.write(ciphertext);
        try (FileOutputStream fos = new FileOutputStream(outPath)) {
            fos.write(bos.toByteArray());
        }
        Arrays.fill(key, (byte) 0);
    }

    private static byte[] encrypt(byte[] plain, byte[] key, byte[] iv) throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec sk = new SecretKeySpec(key, "AES");
        c.init(Cipher.ENCRYPT_MODE, sk, new GCMParameterSpec(GCM_TAG_BITS, iv));
        return c.doFinal(plain);
    }

    static byte[] decrypt(byte[] cipher, byte[] key, byte[] iv) throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec sk = new SecretKeySpec(key, "AES");
        c.init(Cipher.DECRYPT_MODE, sk, new GCMParameterSpec(GCM_TAG_BITS, iv));
        return c.doFinal(cipher);
    }

    private static byte[] deriveKey(String pass, byte[] salt) throws Exception {
        String dev = DeviceFingerprint.fingerprint();
        char[] chars = (pass + "|" + dev).toCharArray();
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(chars, salt, PBKDF2_ITERS, KEY_BITS);
            return f.generateSecret(spec).getEncoded();
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    private static String readUntilEnd(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = br.readLine();
            if (line == null) break;
            if ("::END".equals(line)) break;
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private static String readPassword(BufferedReader br) throws IOException {
        if (System.console() != null) {
            char[] c = System.console().readPassword();
            return new String(c);
        }
        return br.readLine();
    }

    private static byte[] shortToBytes(short s) {
        return new byte[]{(byte)((s >> 8) & 0xFF), (byte)(s & 0xFF)};
    }

    private static short bytesToShort(byte[] b, int off) {
        return (short) (((b[off] & 0xFF) << 8) | (b[off+1] & 0xFF));
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) != -1) bos.write(buf, 0, r);
        return bos.toByteArray();
    }

    private static void println(String s){ System.out.println(s); }
    private static void print(String s){ System.out.print(s); }

    static class EncryptedClassLoader extends ClassLoader {
        private final String pass;
        EncryptedClassLoader(String pass) { super(NurHiddenLoader.class.getClassLoader()); this.pass = pass; }

        Class<?> loadEncrypted(String path) throws Exception {
            byte[] file;
            try (FileInputStream fis = new FileInputStream(path)) {
                file = readAll(fis);
            }
            if (file.length < 4 + SALT_LEN + IV_LEN + 2) throw new IOException("Payload zu klein");
            if (!(file[0]=='N' && file[1]=='H' && file[2]=='V' && file[3]=='8')) throw new IOException("Header ungueltig");
            int off = 4;
            byte[] salt = Arrays.copyOfRange(file, off, off+SALT_LEN); off += SALT_LEN;
            byte[] iv = Arrays.copyOfRange(file, off, off+IV_LEN); off += IV_LEN;
            short nameLen = bytesToShort(file, off); off += 2;
            byte[] nameBytes = Arrays.copyOfRange(file, off, off+nameLen); off += nameLen;
            String className = new String(nameBytes, StandardCharsets.UTF_8);
            byte[] cipher = Arrays.copyOfRange(file, off, file.length);
            byte[] key = deriveKey(pass, salt);
            byte[] plain = decrypt(cipher, key, iv);
            Arrays.fill(key, (byte)0);
            return defineClass(className, plain, 0, plain.length);
        }
    }
}
