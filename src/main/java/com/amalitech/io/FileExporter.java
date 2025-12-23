
package com.amalitech.io;

import com.amalitech.exceptions.ExportFailedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileExporter {
    public void exportToFile(String content, Path path) throws ExportFailedException {
        if (path == null) throw new ExportFailedException("null", new NullPointerException("Path cannot be null"));
        try {
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            if (content == null) content = "";
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ioe) {
            throw new ExportFailedException(path.toAbsolutePath().toString(), ioe);
        }
    }
}
