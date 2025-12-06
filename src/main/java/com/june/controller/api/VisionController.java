package com.june.controller.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * VisionController – einfache Schnittstellenprüfung für Grafik/Vision.
 *  - GET /api/vision/ping   → { ok:true, time, policy:"GERMAN_ONLY" }
 */
@RestController
@RequestMapping("/api/vision")
@CrossOrigin(origins = "*")
public class VisionController {

    @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> ping(){
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("ok", true);
        out.put("time", Instant.now().toString());
        out.put("policy", "GERMAN_ONLY");
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(out);
    }
}
