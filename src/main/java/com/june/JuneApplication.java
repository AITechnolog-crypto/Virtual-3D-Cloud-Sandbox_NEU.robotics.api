package com.june;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JuneApplication {
    public static void main(String[] args) {
        SpringApplication.run(JuneApplication.class, args);
        System.out.println("✓ JuneApplication gestartet – Virtuelle 3D CLOUD Sandbox (Skeleton)");
    }
}