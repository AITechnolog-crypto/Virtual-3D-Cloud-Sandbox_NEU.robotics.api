package com.june.model.common;

// Sehr einfacher Platzhalter – echte QR-Generierung kann später via ZXing erfolgen
public class QRCode {
    private String payload;
    private String format = "QR";

    public QRCode() {}
    public QRCode(String payload) { this.payload = payload; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}