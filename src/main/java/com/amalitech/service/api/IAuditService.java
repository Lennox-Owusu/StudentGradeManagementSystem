
package com.amalitech.service.api;

import java.util.List;

public interface IAuditService {
    List<String> tailLast(int n);
    List<String> filterByLevel(String level); // INFO/ERROR
    List<String> searchByKeyword(String keyword);
    void exportView(List<String> lines);
    void archiveAndTruncate();
}
