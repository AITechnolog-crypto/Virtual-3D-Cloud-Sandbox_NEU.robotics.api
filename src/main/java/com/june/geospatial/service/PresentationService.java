package com.june.geospatial.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Ein Service, der visuelle Präsentationen (z.B. PowerPoint) erstellt.
 */
@Service
public class PresentationService {

    private static final Logger logger = LoggerFactory.getLogger(PresentationService.class);

    public Path createPresentation(String title, String content) {
        logger.info("🎨 Erstelle Präsentation mit Titel: {}", title);

        // In einer echten Implementierung würde hier eine Python-Bibliothek (z.B. python-pptx) aufgerufen werden.
        // Wir simulieren die Erstellung einer Datei.

        try {
            Path path = Paths.get("presentations");
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            Path file = path.resolve(title.replaceAll("[^a-zA-Z0-9]", "_") + ".pptx");
            Files.write(file, content.getBytes());
            logger.info("✅ Präsentation erfolgreich erstellt: {}", file.toAbsolutePath());
            return file;
        } catch (IOException e) {
            logger.error("Fehler beim Erstellen der Präsentation: {}", e.getMessage(), e);
            return null;
        }
    }
}
