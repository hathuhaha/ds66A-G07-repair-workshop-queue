
package com.oop.project.adapter;

import com.oop.project.model.WarrantyStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WarrantyFileImporter {

    public List<WarrantyResult> importFromFile(Path filePath) throws IOException {
        List<WarrantyResult> results = new ArrayList<>();
        List<String> lines = Files.readAllLines(filePath);

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
                WarrantyStatus status = WarrantyStatus.valueOf(
                        parts[1].trim().toUpperCase());
                String description = parts[2].trim();

                LegacyWarrantyRecord record = new LegacyWarrantyRecord(deviceId, status, description);

                WarrantyResult result = new WarrantyAdapter(record);
                results.add(result);

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid warranty data at line " + (i + 1) + ": " + line,
                        e);
            }
        }

        return results;
    }
}
