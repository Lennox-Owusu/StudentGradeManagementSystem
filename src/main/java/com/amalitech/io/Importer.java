
package com.amalitech.io;

import com.amalitech.exceptions.DomainException;
import java.nio.file.Path;

//Minimal contract for importing a single object from a file.
public interface Importer<T> {
    T importFrom(Path source) throws DomainException;
}
