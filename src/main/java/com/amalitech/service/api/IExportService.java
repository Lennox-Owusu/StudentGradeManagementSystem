
package com.amalitech.service.api;

import com.amalitech.service.ExportResult;

public interface IExportService {
    ExportResult exportReport(String content, String baseName);
}
