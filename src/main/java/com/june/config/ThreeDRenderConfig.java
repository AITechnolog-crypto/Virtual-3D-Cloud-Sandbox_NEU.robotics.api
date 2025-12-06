package com.june.config;

import com.june.model.simulation.Scene3D;
import com.june.model.simulation.Asset3D;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreeDRenderConfig {
    @Bean
    public Scene3D demoScene3D(){
        Scene3D scene = new Scene3D();
        scene.getAssets().add(new Asset3D("earth", "/static/models/earth.glb", 0,0,0, 1.0));
        scene.getAssets().add(new Asset3D("drone", "/static/models/drone.glb", 10,5,2, 0.8));
        scene.getAssets().add(new Asset3D("robot", "/static/models/robot.glb", -6,2,1, 0.6));
        return scene;
    }
}