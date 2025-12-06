package com.june.controller.api;

import com.june.service.federation.FederationStore;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/federation")
@CrossOrigin(origins = "*")
public class FederationController {

    private final FederationStore store;
    public FederationController(FederationStore store){ this.store = store; }

    @PostMapping(value = "/submitModel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> submit(@RequestBody Map<String,Object> model){
        if (model == null || model.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error","leerer Body"));
        // keep only known fields to reduce risk
        Map<String,Object> m = new LinkedHashMap<>();
        copyIfPresent(model, m, "slope", "intercept", "min", "max", "avg", "n", "source", "instanceId");
        if (!m.containsKey("slope") && !m.containsKey("intercept")){
            return ResponseEntity.badRequest().body(Map.of("error","Modellparameter fehlen"));
        }
        store.submit(m);
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(Map.of("ok", true));
    }

    @GetMapping(value = "/aggregate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> aggregate(@RequestParam(value = "limit", required = false) Integer limit){
        int lim = (limit == null || limit < 1 || limit > 5000) ? 2000 : limit;
        Map<String,Object> agg = store.aggregate(lim);
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(agg);
    }

    @GetMapping(value = "/models", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String,Object>>> models(@RequestParam(value = "limit", required = false) Integer limit){
        int lim = (limit == null || limit < 1 || limit > 2000) ? 100 : limit;
        List<Map<String,Object>> list = store.list(lim);
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(list);
    }

    @PostMapping(value = "/adoptAggregate")
    public ResponseEntity<Map<String,Object>> adopt(){
        try {
            Path agg = FederationStore.aggregateFile();
            if (!Files.exists(agg)) return ResponseEntity.status(409).body(Map.of("error","Kein Aggregat vorhanden"));
            String json = Files.readString(agg, StandardCharsets.UTF_8);
            // write into LearningController's model file path
            Path learnDir = Paths.get("data", "learn");
            Files.createDirectories(learnDir);
            Path modelFile = learnDir.resolve("energy-model.json");
            Files.writeString(modelFile, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(Map.of("ok",true));
        } catch (Exception e){
            return ResponseEntity.status(500).body(Map.of("error","Adopt‑Fehler: "+e.getMessage()));
        }
    }

    private static void copyIfPresent(Map<String,Object> from, Map<String,Object> to, String... keys){
        for (String k : keys){ if (from.containsKey(k)) to.put(k, from.get(k)); }
    }
}
