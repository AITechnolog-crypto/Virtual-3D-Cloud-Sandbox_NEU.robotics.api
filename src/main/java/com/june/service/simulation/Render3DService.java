package com.june.service.simulation;

import com.june.model.simulation.Scene3D;
import org.springframework.stereotype.Service;

@Service
public class Render3DService {
    private final Scene3D scene;
    public Render3DService(Scene3D scene){ this.scene = scene; }
    public Scene3D getScene(){ return scene; }
}