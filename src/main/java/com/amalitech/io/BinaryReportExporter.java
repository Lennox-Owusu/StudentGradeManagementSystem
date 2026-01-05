
package com.amalitech.io;

import com.amalitech.reporting.StudentReport;
import com.amalitech.exceptions.ExportFailedException;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.*;

public final class BinaryReportExporter {

    public Path exportSerialized(StudentReport report, Path dir, String baseName) throws ExportFailedException {
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path target = dir.resolve(baseName + "_detailed.dat");

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(
                    target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                oos.writeObject(report);
            }
            return target;
        } catch (IOException ioe) {
            throw new ExportFailedException(dir.toAbsolutePath().toString(), ioe);
        }
    }
}
