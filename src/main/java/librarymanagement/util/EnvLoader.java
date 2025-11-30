package librarymanagement.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class EnvLoader {
    public static Map<String, String> load() {
        Map<String, String> map = new HashMap<>();
        Path path = Paths.get("pass.env");

        if (!Files.exists(path)) {
            System.err.println("Warning: pass.env not found!");
            return map;
        }

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    map.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading pass.env");
        }
        return map;
    }
}