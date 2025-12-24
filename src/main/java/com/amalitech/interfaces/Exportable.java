
package com.amalitech.interfaces;

import com.amalitech.exceptions.ExportFailedException;
import java.nio.file.Path;

//Focused interface to export text to a path.
public interface Exportable {
    void export(String content, Path target) throws ExportFailedException;
}
