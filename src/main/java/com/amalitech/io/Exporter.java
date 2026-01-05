
package com.amalitech.io;

import java.nio.file.Path;

public interface Exporter<T> {
    ExportResult exportTo(T data, Path targetDir) throws ImportExportException;
}
