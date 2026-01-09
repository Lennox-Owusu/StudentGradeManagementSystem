
package com.amalitech.service.api;

public interface ICacheAdminService {
    void warm();
    void benchmark();
    void clear();
    void resetCounters();
    String status();
}
