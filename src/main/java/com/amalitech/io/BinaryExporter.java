
package com.amalitech.io;

import com.amalitech.reporting.StudentReport;

import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;

public final class BinaryExporter implements Exporter<StudentReport> {

    @Override
    public ExportResult exportTo(StudentReport report, Path baseDir) throws ImportExportException {
        try {
            Path dir = NioFileUtils.ensureDir(baseDir.resolve("binary"));
            String name = NioFileUtils.safeFileName(report.getName()) + "_" + report.getReportType().name().toLowerCase() + ".dat";
            Path file = dir.resolve(name);

            long start = System.nanoTime();
            try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                 GZIPOutputStream gzip = new GZIPOutputStream(bos);
                 ObjectOutputStream oos = new ObjectOutputStream(gzip)) {
                oos.writeObject(report);
                oos.flush();
            }
            long millis = (System.nanoTime() - start) / 1_000_000;
            long bytes = NioFileUtils.fileSize(file);
            return new ExportResult(bytes, millis, file.getFileName().toString(), "./reports/binary/");
        } catch (Exception e) {
            throw new ImportExportException("Binary export failed", e);
        }
    }
}
