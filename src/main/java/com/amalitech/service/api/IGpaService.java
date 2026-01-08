
package com.amalitech.service.api;

import com.amalitech.service.GPAData;

public interface IGpaService {
    GPAData computeFor(String studentId);
    String toConsoleString(GPAData data);
}
