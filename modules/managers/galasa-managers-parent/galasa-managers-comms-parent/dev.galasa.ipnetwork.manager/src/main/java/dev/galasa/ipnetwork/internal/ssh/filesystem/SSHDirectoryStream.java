/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.internal.ssh.filesystem;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SSHDirectoryStream implements DirectoryStream<Path> {

    private final ArrayList<Path> paths = new ArrayList<>();

    public SSHDirectoryStream(Path path, SSHFileSystem fileSystem, Filter<? super Path> filter) throws IOException {
        path = path.toAbsolutePath();

        ChannelSftp channel = null;
        try {
            channel = fileSystem.getFileChannel();

            Vector<?> ls = channel.ls(path.toString());
            for (Object entry : ls) {
                if (entry instanceof LsEntry) {
                    LsEntry lsEntry = (LsEntry) entry;

                    String fileName = lsEntry.getFilename();
                    if (".".equals(fileName) || "..".equals(fileName)) {
                        continue;
                    }

                    Path child = path.resolve(fileName);
                    if (filter.accept(child)) {
                        paths.add(child);
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("Unable to get directory listing", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }

    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public Iterator<Path> iterator() {
        return paths.iterator();
    }

}
