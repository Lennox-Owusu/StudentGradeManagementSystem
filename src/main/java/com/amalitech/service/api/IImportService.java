
package com.amalitech.service.api;

import com.amalitech.service.ImportResult;
import java.nio.file.Path;

public interface IImportService {
    ImportResult importCsv(Path csvPath, boolean autoSkipHeader);
}
