package com.june.hidden;

import java.util.Map;

/**
 * Kleines Demo‑Payload. Kann genutzt werden, um den Loader zu testen.
 * Vollqualifizierter Name: com.june.hidden.HelloPayloadTask
 */
public class HelloPayloadTask implements PayloadTask {
    @Override
    public void run(Map<String, Object> context) throws Exception {
        System.out.println("[HelloPayloadTask] Kontext: " + context);
        System.out.println("[HelloPayloadTask] Hallo von der verschlüsselten Payload! ✨");
    }
}
