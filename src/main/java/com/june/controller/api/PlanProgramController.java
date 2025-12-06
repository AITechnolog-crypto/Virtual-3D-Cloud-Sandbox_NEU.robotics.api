package com.june.controller.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ml/plan")
@CrossOrigin(origins = "*")
public class PlanProgramController {

    private static final Path PLAN_DIR = Paths.get("data", "ml");
    private static final Path PLAN_FILE = PLAN_DIR.resolve("policy-plan.json");

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> get(){
        try {
            if (!Files.exists(PLAN_FILE)){
                String empty = "{\"planTitle\":\"\",\"objectives\":[],\"requiredSteps\":[]}";
                return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(empty);
            }
            String s = Files.readString(PLAN_FILE, StandardCharsets.UTF_8);
            return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(s);
        } catch (Exception e){
            return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Plan lesen fehlgeschlagen\"}");
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> set(@RequestBody Map<String,Object> body){
        try {
            Files.createDirectories(PLAN_DIR);
            String json = toJsonFiltered(body);
            if (json.length() > 50_000){
                return ResponseEntity.badRequest().body(Map.of("error","Plan zu groß"));
            }
            Files.writeString(PLAN_FILE, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(Map.of("ok",true));
        } catch (Exception e){
            return ResponseEntity.status(500).body(Map.of("error","Plan speichern fehlgeschlagen"));
        }
    }

    // keep only expected fields, simple JSON builder
    private static String toJsonFiltered(Map<String,Object> in){
        String title = String.valueOf(in.getOrDefault("planTitle", "")).trim();
        if (title.length() > 200) title = title.substring(0,200);
        Object objectives = in.get("objectives");
        Object required = in.get("requiredSteps");
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"planTitle\":\"").append(escape(title)).append("\"");
        sb.append(",\"objectives\":").append(arrayOfStrings(objectives));
        sb.append(",\"requiredSteps\":").append(arrayOfStrings(required));
        sb.append('}');
        return sb.toString();
    }
    private static String arrayOfStrings(Object o){
        if (!(o instanceof Iterable<?>)) return "[]";
        StringBuilder sb = new StringBuilder("["); boolean first=true;
        for (Object v : (Iterable<?>)o){
            String s = String.valueOf(v);
            if (!first) sb.append(','); first=false;
            sb.append('"').append(escape(s)).append('"');
        }
        sb.append(']'); return sb.toString();
    }
    private static String escape(String s){ return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n"); }
}
