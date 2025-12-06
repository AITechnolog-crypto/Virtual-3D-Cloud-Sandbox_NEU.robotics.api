package com.june.controller.api.v1.simulation;

import com.june.model.simulation.Scene3D;
import com.june.service.simulation.Render3DService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulation")
public class Render3DController {
    private final Render3DService render3DService;
    public Render3DController(Render3DService render3DService){ this.render3DService = render3DService; }

    @GetMapping("/scene")
    public ResponseEntity<Scene3D> getScene(){
        return ResponseEntity.ok(render3DService.getScene());
    }
}