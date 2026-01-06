
package com.amalitech.io;

import com.amalitech.reporting.StudentReport;
import com.amalitech.exceptions.ImportFailedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.*;
import java.nio.file.StandardOpenOption;

public final class BinaryReportImporter implements Importer<StudentReport> {
    @Override
    public StudentReport importFrom(Path source) throws ImportFailedException {
        if (source == null || !Files.exists(source) || !Files.isRegularFile(source)) {
            throw new ImportFailedException("Binary file not found: " + source);
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                Files.newInputStream(source, StandardOpenOption.READ))) {
            Object o = ois.readObject();
            if (!(o instanceof StudentReport sr)) {
                throw new ImportFailedException("Unexpected binary content (not StudentReport).");
            }
            return sr;
        } catch (IOException | ClassNotFoundException ex) {
            throw new ImportFailedException("Failed reading binary: " + source.toAbsolutePath(), ex);
        }
    }
}
