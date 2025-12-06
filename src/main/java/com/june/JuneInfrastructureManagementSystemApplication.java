package com.june;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JuneApplication {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   🚀 INFRASTRUCTURE MANAGEMENT SYSTEM 3D VR     ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
        
        SpringApplication.run(JuneApplication.class, args);
        
        System.out.println("\n✅ System erfolgreich gestartet!");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📊 REST API:        http://localhost:8080/api");
        System.out.println("🌐 Web Interface:   http://localhost:8080/index.html");
        System.out.println("🗄️  H2 Console:      http://localhost:8080/h2-console");
        System.out.println("🔌 WebSocket:       ws://localhost:8080/ws");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("\n📡 API Endpoints:");
        System.out.println("  • Robotics:       /api/robotics/*");
        System.out.println("  • Drones:         /api/drones/*");
        System.out.println("  • Financial:      /api/financial/*");
        System.out.println("  • Infrastructure: /api/infrastructure/*");
        System.out.println("  • Blueprints:     /api/blueprints/*");
        System.out.println("  • Dashboard:      /api/dashboard/overview");
        System.out.println("\n🥽 VR/3D Features:");
        System.out.println("  • 3D Visualization mit Three.js");
        System.out.println("  • WebXR VR Support (Meta Quest, etc.)");
        System.out.println("  • Live-Daten Integration");
        System.out.println("  • Solar Walk Style Navigation");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
}
