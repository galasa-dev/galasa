/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras.directory;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessMode;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.ResultArchiveStoreFileAttributeView;

public class DirectoryFileSystemTest {

    private static final byte[] testData = new byte[] { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 };

    private Path                runDirectory;
    private Path                artifactDirectory;

    @Before
    public void before() throws IOException {
        this.runDirectory = Files.createTempDirectory("galasa_junit_rasdirfs_");
        this.artifactDirectory = this.runDirectory.resolve("artifacts");
        Files.createDirectory(this.artifactDirectory);
    }

    @After
    public void after() throws IOException {
        if (this.runDirectory != null) {
            if (Files.exists(this.runDirectory)) {
                FileUtils.deleteDirectory(this.runDirectory.toFile());
            }
        }
    }

    @Test
    public void testSetup() throws IOException {
        final FileSystemProvider fsp = new DirectoryRASFileSystemProvider(this.runDirectory);
        final FileSystem fs = fsp.getFileSystem(this.runDirectory.toUri());

        Assert.assertEquals("Invalid scheme", "rasdir", fsp.getScheme());

        Assert.assertNotNull("File system missing", fs);
        Assert.assertFalse("Must not be read only", fs.isReadOnly());
        Assert.assertTrue("Must be open", fs.isOpen());
        Assert.assertEquals("Invalid separater", "/", fs.getSeparator());
        Assert.assertNotNull("Did not return a list of filestores", fs.getFileStores());
    }

    @Test
    public void testSimpleWrite() throws IOException {
        final FileSystemProvider fsp = new DirectoryRASFileSystemProvider(this.runDirectory);
        final FileSystem fs = fsp.getFileSystem(this.runDirectory.toUri());

        final Path rasTestArtifact = fs.getPath("arty1");
        final Path realTestArtifact = this.artifactDirectory.resolve("arty1");

        try (OutputStream os = Files.newOutputStream(rasTestArtifact)) {
            os.write(testData);
        }

        Assert.assertTrue("test artifact does not exist", Files.exists(realTestArtifact));

        final byte[] storedData = Files.readAllBytes(realTestArtifact);
        Assert.assertArrayEquals("data different", testData, storedData);
    }

    @Test
    public void testSimpleRead() throws IOException {
        final FileSystemProvider fsp = new DirectoryRASFileSystemProvider(this.runDirectory);
        final FileSystem fs = fsp.getFileSystem(this.runDirectory.toUri());

        final Path rasTestArtifact = fs.getPath("arty1");
        final Path realTestArtifact = this.artifactDirectory.resolve("arty1");
        Files.write(realTestArtifact, testData);

        final byte[] readData = Files.readAllBytes(rasTestArtifact);

        Assert.assertArrayEquals("data different", testData, readData);
    }

    @Test
    public void testDirectory() throws IOException {
        final FileSystemProvider fsp = new DirectoryRASFileSystemProvider(this.runDirectory);
        final FileSystem fs = fsp.getFileSystem(this.runDirectory.toUri());

        final Path rasTestDirectory = fs.getPath("/dir1/dir2");
        final Path realTestDirectory = this.artifactDirectory.resolve("dir1/dir2");

        Assert.assertFalse("Should not exist yet", Files.exists(rasTestDirectory));

        Files.createDirectories(rasTestDirectory);

        Assert.assertTrue("Should now exist in RAS", Files.exists(rasTestDirectory));
        Assert.assertTrue("Should now exist in Real", Files.exists(realTestDirectory));

        Assert.assertTrue("Should be a directory in RAS", Files.isDirectory(rasTestDirectory));
        Assert.assertTrue("Should be a directory in Real", Files.isDirectory(realTestDirectory));

        Assert.assertFalse("Should not be a file RAS", Files.isRegularFile(rasTestDirectory));
    }

    @Test
    public void testDirectoryStream() throws IOException {
        final FileSystemProvider fsp = new DirectoryRASFileSystemProvider(this.runDirectory);
        final FileSystem fs = fsp.getFileSystem(this.runDirectory.toUri());

        final Path rasTestDirectory = fs.getPath("/dir1/dir2");

        Files.createDirectories(rasTestDirectory);

        final Path fileA = rasTestDirectory.resolve("fileA");
        final Path fileB = rasTestDirectory.resolve("fileB");
        final Path fileC = rasTestDirectory.resolve("fileC");
        final Path fileD = rasTestDirectory.resolve("fileD");
        final Path dirA = rasTestDirectory.resolve("dirA");
        final Path dirB = rasTestDirectory.resolve("dirB");

        Files.write(fileA, testData);
        Files.write(fileB, testData);
        Files.write(fileC, testData);
        Files.write(fileD, testData);
        Files.createDirectory(dirA);
        Files.createDirectory(dirB);

        final HashSet<String> files = new HashSet<>();
        files.add(fileA.toString());
        files.add(fileB.toString());
        files.add(fileC.toString());
        files.add(fileD.toString());
        files.add(dirA.toString());
        files.add(dirB.toString());

        int count = 0;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(rasTestDirectory)) {
            for (final Path entry : ds) {
                count++;
                files.remove(entry.toString());
            }
        }

        Assert.assertEquals("Incorrect number of files returned", 6, count);
        Assert.assertTrue("Some files were missing from stream " + files, files.isEmpty());
    }

    @Test
    public void testDirectoryStreamClose() throws IOException {
        final FileSystemProvider fsp = new DirectoryRASFileSystemProvider(this.runDirectory);
        final FileSystem fs = fsp.getFileSystem(this.runDirectory.toUri());

        final Path rasTestDirectory = fs.getPath("/");
        final DirectoryStream<Path> ds = Files.newDirectoryStream(rasTestDirectory);
        ds.close();

        try {
            ds.iterator();
            fail("Should have received an exception after close");
        } catch (final ClosedDirectoryStreamException e) {
        }

    }

    @Test
    public void testEmptyDirectoryStream() throws IOException {
        final FileSystemProvider fsp = new DirectoryRASFileSystemProvider(this.runDirectory);
        final FileSystem fs = fsp.getFileSystem(this.runDirectory.toUri());

        final Path rasTestDirectory = fs.getPath("/dir1/dir2");

        Files.createDirectories(rasTestDirectory);

        Assert.assertFalse("There should be no files", Files.newDirectoryStream(rasTestDirectory).iterator().hasNext());
    }

    @Test
    public void testBasicFileAttributes() throws IOException {
        final FileSystemProvider fsp = new DirectoryRASFileSystemProvider(this.runDirectory);
        final FileSystem fs = fsp.getFileSystem(this.runDirectory.toUri());

        final Path rasTestArtifact = fs.getPath("arty1");

        Files.write(rasTestArtifact, testData);

        final BasicFileAttributeView attrsView = Files.getFileAttributeView(rasTestArtifact,
                BasicFileAttributeView.class);
        Assert.assertNotEquals("Missing basic attributes view", attrsView);

        final BasicFileAttributes attrs = Files.readAttributes(rasTestArtifact, BasicFileAttributes.class);
        Assert.assertNotEquals("Missing basic attributes", attrs);

        final Map<String, Object> attrs2 = Files.readAttributes(rasTestArtifact, "*");
        Assert.assertNotEquals("Missing basic attributes", attrs2);
    }

    @Test
    public void testRASFileAttributes() throws IOException {
        final FileSystemProvider fsp = new DirectoryRASFileSystemProvider(this.runDirectory);
        final FileSystem fs = fsp.getFileSystem(this.runDirectory.toUri());

        final Path rasTestArtifact = fs.getPath("/arty1.xml");

        Files.createFile(rasTestArtifact, ResultArchiveStoreContentType.XML);

        final ResultArchiveStoreFileAttributeView view = Files.getFileAttributeView(rasTestArtifact,
                ResultArchiveStoreFileAttributeView.class);
        Assert.assertNotNull("RAS Attributes View missing", view);

        final ResultArchiveStoreContentType type = view.getContentType();
        Assert.assertNotNull("Content type missing from view", type);
        Assert.assertEquals("Type not XML", ResultArchiveStoreContentType.XML, type);

        Map<String, Object> attrs = Files.readAttributes(rasTestArtifact, "ras:contentType");
        Assert.assertEquals("Content type missing/incorrect from attributes map",
                ResultArchiveStoreContentType.XML.value(), attrs.get("ras:contentType"));

        attrs = Files.readAttributes(rasTestArtifact, "ras:*");
        Assert.assertEquals("Content type missing/incorrect from attributes map",
                ResultArchiveStoreContentType.XML.value(), attrs.get("ras:contentType"));

        attrs = Files.readAttributes(rasTestArtifact, "*");
        Assert.assertEquals("Content type missing/incorrect from attributes map",
                ResultArchiveStoreContentType.XML.value(), attrs.get("ras:contentType"));

        attrs = Files.readAttributes(rasTestArtifact, "size,ras:contentType, lastAccessTime");
        Assert.assertEquals("Content type missing/incorrect from attributes map",
                ResultArchiveStoreContentType.XML.value(), attrs.get("ras:contentType"));
        Assert.assertEquals("Incorrect number of attributes return", 3, attrs.size());
    }

    @Test
    public void testRASFileAttributesPreload() throws IOException {
        final Properties contentTypes = new Properties();
        contentTypes.setProperty("/arty1.png", "image/png");
        final Path contentTypesFile = this.runDirectory.resolve("artifacts.properties");
        OutputStream os = Files.newOutputStream(contentTypesFile);
        contentTypes.store(os, null);
        os.close();

        final FileSystemProvider fsp = new DirectoryRASFileSystemProvider(this.runDirectory);
        final FileSystem fs = fsp.getFileSystem(this.runDirectory.toUri());

        final Path rasTestArtifact = fs.getPath("/arty1.png");

        final ResultArchiveStoreFileAttributeView view = Files.getFileAttributeView(rasTestArtifact,
                ResultArchiveStoreFileAttributeView.class);
        Assert.assertNotNull("RAS Attributes View missing", view);

        final ResultArchiveStoreContentType type = view.getContentType();
        Assert.assertNotNull("Content type missing from view", type);
        Assert.assertEquals("Type not PNG", ResultArchiveStoreContentType.PNG, type);
    }

    @Test
    public void testAccess() throws IOException {
        final FileSystemProvider fsp = new DirectoryRASFileSystemProvider(this.runDirectory);
        final DirectoryRASFileSystem fs = (DirectoryRASFileSystem) fsp.getFileSystem(this.runDirectory.toUri());

        final Path rasTestArtifact = fs.getPath("/arty1.xml");
        Files.createFile(rasTestArtifact);

        try {
            fsp.checkAccess(rasTestArtifact, AccessMode.EXECUTE);
            fail("Should have blocked execute");
        } catch (final IOException e) {
        }

        fsp.checkAccess(rasTestArtifact, AccessMode.READ);
        fsp.checkAccess(rasTestArtifact, AccessMode.WRITE);

        fs.setReadOnly(true);
        try {
            fsp.checkAccess(rasTestArtifact, AccessMode.WRITE);
            fail("Should have blocked write");
        } catch (final IOException e) {
        }

        try {
            fsp.checkAccess(fs.getPath("/missing.xml"), AccessMode.READ);
            fail("Should have blocked write");
        } catch (final IOException e) {
        }

    }

}
