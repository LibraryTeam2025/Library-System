package librarymanagement;

import librarymanagement.util.EnvLoader;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvLoaderTest {

    @TempDir
    Path tempDir;

    private Path envFile;

    @BeforeEach
    void setUp() {
        envFile = tempDir.resolve("pass.env");
    }

    @Test
    void testLoad_ValidEntries_ParsesCorrectly() throws IOException {
        Files.writeString(envFile, """
            EMAIL_USER=admin@example.com
            EMAIL_PASS=secret123
            SMTP_HOST=smtp.gmail.com
            SMTP_PORT=587
            """);

        Map<String, String> result = EnvLoader.load(envFile);

        assertAll(
                () -> assertEquals(4, result.size()),
                () -> assertEquals("admin@example.com", result.get("EMAIL_USER")),
                () -> assertEquals("secret123", result.get("EMAIL_PASS")),
                () -> assertEquals("smtp.gmail.com", result.get("SMTP_HOST")),
                () -> assertEquals("587", result.get("SMTP_PORT"))
        );
    }

    @Test
    void testLoad_MalformedLine_NoEquals_IgnoresLine() throws IOException {
        Files.writeString(envFile, """
            EMAIL_USER=good@example.com
            THIS_LINE_HAS_NO_EQUALS
            EMAIL_PASS=12345
            """);

        Map<String, String> result = EnvLoader.load(envFile);

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("good@example.com", result.get("EMAIL_USER")),
                () -> assertEquals("12345", result.get("EMAIL_PASS"))
        );
    }

    @Test
    void testLoad_FileDoesNotExist_ReturnsEmptyMap() {
        Path nonExistentFile = tempDir.resolve("nonexistent.env");
        Map<String, String> result = EnvLoader.load(nonExistentFile);
        assertTrue(result.isEmpty());
    }

    @Test
    void testLoad_EmptyFile_ReturnsEmptyMap() throws IOException {
        Files.createFile(envFile);
        Map<String, String> result = EnvLoader.load(envFile);
        assertTrue(result.isEmpty());
    }
}
