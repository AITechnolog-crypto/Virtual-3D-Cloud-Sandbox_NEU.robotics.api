package com.june.geospatial.secure.model;

import com.june.geospatial.secure.util.QrUtil;
import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Repräsentiert eine einheitliche Wallet für verschiedene Zahlungsmethoden.
 * Unterstützt IBAN, Bitcoin und Ethereum-Adressen sowie die Generierung von QR-Codes.
 */
public class UnifiedWallet {
    private final String iban;
    private final String btcAddress;
    private final String ethAddress;
    private final String receiverName;

    public UnifiedWallet(String iban, String btcAddress, String ethAddress, String receiverName) {
        if (iban == null || iban.trim().isEmpty()) {
            throw new IllegalArgumentException("IBAN darf nicht leer sein");
        }
        if (btcAddress == null || btcAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Bitcoin-Adresse darf nicht leer sein");
        }
        if (ethAddress == null || ethAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Ethereum-Adresse darf nicht leer sein");
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new IllegalArgumentException("Empfängername darf nicht leer sein");
        }

        this.iban = iban.trim();
        this.btcAddress = btcAddress.trim();
        this.ethAddress = ethAddress.trim();
        this.receiverName = receiverName.trim();
    }

    /**
     * Erstellt eine Standard-Wallet mit vordefinierten Werten.
     * @return Eine neue Instanz von UnifiedWallet mit Standardwerten
     */
    public static UnifiedWallet defaults() {
        return new UnifiedWallet(
            "AT921400057010099023",
            "bc1qzdg8qjd42rg2sa6t4wqzchx40rjmz0xyh3gccd",
            "0x40dEa729f32481A707917AcBBD9eaA84AcB66367",
            "ShariaBoots SecureSwarm"
        );
    }

    // Getter
    public String getIban() { return iban; }
    public String getBtcAddress() { return btcAddress; }
    public String getEthAddress() { return ethAddress; }
    public String getReceiverName() { return receiverName; }

    // Builder-Pattern Methoden
    public UnifiedWallet withIBAN(String v) {
        return v == null ? this : new UnifiedWallet(v, btcAddress, ethAddress, receiverName);
    }

    public UnifiedWallet withBTC(String v) {
        return v == null ? this : new UnifiedWallet(iban, v, ethAddress, receiverName);
    }

    public UnifiedWallet withETH(String v) {
        return v == null ? this : new UnifiedWallet(iban, btcAddress, v, receiverName);
    }

    /**
     * Gibt eine Zusammenfassung der Wallet-Informationen als JSON-String zurück.
     * @return JSON-String mit den Wallet-Informationen
     */
    public String getSummary() {
        return String.format(
            "{\"iban\":\"%s\",\"btc\":\"%s\",\"eth\":\"%s\",\"receiver\":\"%s\"}",
            iban, btcAddress, ethAddress, receiverName
        );
    }

    /**
     * Generiert QR-Codes für verschiedene Zahlungsmethoden und gibt eine Textdarstellung zurück.
     * @param btcAmount Betrag in BTC (optional)
     * @param sepaAmountEUR Betrag in EUR für SEPA-Überweisung (optional)
     * @param sepaNote Verwendungszweck für SEPA-Überweisung
     * @return Formatierter String mit Zahlungsinformationen
     */
    public String renderQrSection(Double btcAmount, Double sepaAmountEUR, String sepaNote) {
        StringBuilder sb = new StringBuilder();
        sb.append("== Zahlungsinformationen ==\n\n");

        // SEPA-Informationen
        sb.append("### SEPA-Überweisung\n");
        sb.append("**Empfänger:** ").append(receiverName).append("\n");
        sb.append("**IBAN:** `").append(formatIban(iban)).append("`\n");
        if (sepaAmountEUR != null && sepaAmountEUR > 0) {
            sb.append("**Betrag:** ").append(String.format("%.2f EUR", sepaAmountEUR)).append("\n");
        }
        if (sepaNote != null && !sepaNote.trim().isEmpty()) {
            sb.append("**Verwendungszweck:** ").append(sepaNote).append("\n");
        }

        // QR-Code für SEPA generieren
        String epc = QrUtil.epcSepaPayload(receiverName, iban, sepaAmountEUR,
            sepaNote == null ? "SecureSwarm Support" : sepaNote);
        String qrSepaPath = "qrcodes/sepa_qr.png";
        QrUtil.writePngIfZXing(epc, new File(qrSepaPath), 300);
        sb.append("![SEPA QR-Code](").append(qrSepaPath).append(")\n\n");

        // Bitcoin-Informationen
        sb.append("### Bitcoin\n");
        sb.append("**Adresse:** `").append(btcAddress).append("`\n");
        if (btcAmount != null && btcAmount > 0) {
            sb.append("**Betrag:** ").append(btcAmount).append(" BTC\n");
        }

        // QR-Code für Bitcoin generieren
        String btcUri = QrUtil.bitcoinUri(btcAddress, btcAmount);
        String qrBtcPath = "qrcodes/btc_qr.png";
        QrUtil.writePngIfZXing(btcUri, new File(qrBtcPath), 300);
        sb.append("![Bitcoin QR-Code](").append(qrBtcPath).append(")\n\n");

        // Ethereum-Informationen
        sb.append("### Ethereum\n");
        sb.append("**Adresse:** `").append(ethAddress).append("`\n\n");

        // QR-Code für Ethereum generieren
        String ethUri = QrUtil.ethereumUri(ethAddress);
        String qrEthPath = "qrcodes/eth_qr.png";
        QrUtil.writePngIfZXing(ethUri, new File(qrEthPath), 300);
        sb.append("![Ethereum QR-Code](").append(qrEthPath).append(")\n\n");

        // PayPal-Link
        if (sepaAmountEUR != null && sepaAmountEUR > 0) {
            sb.append("### PayPal\n");
            sb.append("Alternativ können Sie auch über PayPal spenden:  ");
            sb.append(String.format("[Spende mit PayPal](https://www.paypal.me/SanelCrnkic?amount=%.2f&currency_code=EUR)",
                sepaAmountEUR));
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Formatiert eine IBAN für die Anzeige (alle 4 Zeichen ein Leerzeichen).
     * @param iban Die zu formatierende IBAN
     * @return Formatierte IBAN
     */
    private String formatIban(String iban) {
        if (iban == null || iban.length() < 4) {
            return iban;
        }
        StringBuilder formatted = new StringBuilder(iban);
        for (int i = 4; i < formatted.length(); i += 5) {
            formatted.insert(i, ' ');
        }
        return formatted.toString();
    }

    @Override
    public String toString() {
        return "UnifiedWallet{" +
               "iban='" + maskString(iban, 4) + "'" +
               ", btcAddress='" + maskString(btcAddress, 4) + "'" +
               ", ethAddress='" + maskString(ethAddress, 4) + "'" +
               ", receiverName='" + receiverName + "'" +
               "}";
    }

    /**
     * Maskiert sensible Daten für die Ausgabe.
     * @param input Der zu maskierende String
     * @param visibleChars Anzahl der sichtbaren Zeichen am Ende
     * @return Maskierter String
     */
    private String maskString(String input, int visibleChars) {
        if (input == null || input.length() <= visibleChars) {
            return input;
        }
        int maskLength = input.length() - visibleChars;
        return "*".repeat(maskLength) + input.substring(maskLength);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnifiedWallet that = (UnifiedWallet) o;
        return Objects.equals(iban, that.iban) &&
               Objects.equals(btcAddress, that.btcAddress) &&
               Objects.equals(ethAddress, that.ethAddress) &&
               Objects.equals(receiverName, that.receiverName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iban, btcAddress, ethAddress, receiverName);
    }
}
