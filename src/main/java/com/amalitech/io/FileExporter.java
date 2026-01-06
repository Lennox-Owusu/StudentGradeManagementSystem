
package com.amalitech.io;

import com.amalitech.interfaces.Exportable;
import com.amalitech.exceptions.ExportFailedException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileExporter implements Exportable {

    // Existing method retained for backward compat
    public void exportToFile(String content, Path path) throws ExportFailedException {
        export(content, path);
    }

    @Override
    public void export(String content, Path path) throws ExportFailedException {
        if (path == null) throw new ExportFailedException("null", new NullPointerException("Path cannot be null"));
        try {
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            if (content == null) content = "";
            Files.writeString(path, content);
        } catch (IOException ioe) {
            throw new ExportFailedException(path.toAbsolutePath().toString(), ioe);
        }
    }
}
