package com.june.hidden;

import java.util.Map;

/**
 * Vertrag für auszuführende Payloads. Implementierende Klassen werden
 * vom NurHiddenLoader geladen und mit einem einfachen Kontext ausgeführt.
 */
public interface PayloadTask {
    void run(Map<String, Object> context) throws Exception;
}
