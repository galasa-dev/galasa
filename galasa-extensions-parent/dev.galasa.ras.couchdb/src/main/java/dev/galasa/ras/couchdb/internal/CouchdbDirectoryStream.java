/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class CouchdbDirectoryStream implements DirectoryStream<Path> {

    private ArrayList<Path> children = new ArrayList<>();

    public CouchdbDirectoryStream(Path dir, Filter<? super Path> filter, HashSet<CouchdbArtifactPath> paths)
            throws IOException {
        dir = dir.toAbsolutePath();

        for (CouchdbArtifactPath path : paths) {
            CouchdbArtifactPath parent = path.getParent();
            if (parent != null && path.getParent().equals(dir)) {
                if (filter.accept(path)) {
                    children.add(path);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public Iterator<Path> iterator() {
        return children.iterator();
    }

}
