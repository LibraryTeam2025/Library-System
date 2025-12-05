package librarymanagement.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {

    public static Map<String, String> load() {
        return load(Path.of("pass.env"));
    }

    public static Map<String, String> load(Path filePath) {
        Map<String, String> map = new HashMap<>();
        if (!Files.exists(filePath)) return map;

        try {
            for (String line : Files.readAllLines(filePath)) {
                if (!line.contains("=")) continue;
                String[] parts = line.split("=", 2);
                map.put(parts[0], parts[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
