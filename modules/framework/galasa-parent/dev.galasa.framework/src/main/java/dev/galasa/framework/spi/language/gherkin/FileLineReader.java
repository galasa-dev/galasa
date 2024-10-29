/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import java.net.URI;
import java.util.List;
import java.io.IOException;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.TestRunException;

public class FileLineReader implements IFileLineReader {

    private IFileSystem fs ;

    FileLineReader(IFileSystem fs) {
        this.fs = fs ;
    }
    
    @Override
    public List<String> readLines(URI uri) throws TestRunException {
        List<String> lines ;
        try {
            lines = fs.readLines(uri);
            
        } catch (IOException e) {
            throw new TestRunException("Unable to read the gherkin test file", e);
        }
        return lines ;
    }
}
