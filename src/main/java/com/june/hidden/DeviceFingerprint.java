package com.june.hidden;

/**
 * Einfache Geräte-Fingerprint-Hilfsklasse.
 * Nutzt nur harmlose System-Properties und bildet einen stabilen Hash ab.
 */
public final class DeviceFingerprint {
    private DeviceFingerprint() {}

    public static String fingerprint() {
        String os = System.getProperty("os.name", "");
        String arch = System.getProperty("os.arch", "");
        String user = System.getProperty("user.name", "");
        String home = System.getProperty("user.home", "");
        String java = System.getProperty("java.version", "");
        String t = os + "|" + arch + "|" + user + "|" + home + "|" + java;
        return Integer.toHexString(t.hashCode());
    }
}
