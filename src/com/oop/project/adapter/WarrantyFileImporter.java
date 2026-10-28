
package com.oop.project.adapter;

import com.oop.project.model.WarrantyStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class WarrantyFileImporter {

    public List<WarrantyResult> importFromFile(Path filePath) throws IOException {
        Objects.requireNonNull(filePath, "Warranty file path is required.");
        List<WarrantyResult> results = new ArrayList<>();
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // Skip empty lines
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\|", 3);

            if (parts.length != 3) {
                throw new IllegalArgumentException(
                        "Invalid warranty data at line " + (i + 1));
            }

            try {
                int deviceId = Integer.parseInt(parts[0].trim());
                if (deviceId <= 0) {
                    throw new IllegalArgumentException("device ID must be positive");
                }
                WarrantyStatus status = WarrantyStatus.valueOf(
                        parts[1].trim().toUpperCase(Locale.ROOT));
                String description = parts[2].trim();

                LegacyWarrantyRecord record = new LegacyWarrantyRecord(deviceId, status, description);
                results.add(new WarrantyAdapter(record));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid warranty data at line " + (i + 1) + ": " + e.getMessage(),
                        e);
            }
        }

        return results;
    }
}
