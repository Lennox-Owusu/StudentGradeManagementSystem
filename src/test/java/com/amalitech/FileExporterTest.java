
package com.amalitech;

import com.amalitech.io.FileExporter;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FileExporterTest {

    @Test
    public void exports_content_to_file() throws Exception {
        FileExporter exporter = new FileExporter();
        Path path = Paths.get("test-exports", "sample.txt");

        String content = "Hello, export!";
        exporter.exportToFile(content, path);

        assertTrue(Files.exists(path));
        String readBack = Files.readString(path);
        assertEquals(content, readBack);
    }
}
