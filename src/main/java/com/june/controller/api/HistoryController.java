package com.june.controller.api;

import com.june.service.history.HistoryStore;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class HistoryController {

    private final HistoryStore history;

    public HistoryController(HistoryStore history){
        this.history = history;
    }

    @GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> summary(@RequestParam(value = "months", required = false) Integer months){
        int maxFiles = (months == null || months <= 0 || months > 24) ? 12 : months;
        HistoryStore.Summary s = history.summarize(maxFiles);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(s.toMap());
    }
}
