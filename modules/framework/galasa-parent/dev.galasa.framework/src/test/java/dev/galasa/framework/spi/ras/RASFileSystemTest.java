/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessMode;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.framework.spi.ras.ResultArchiveStoreBasicAttributes;
import dev.galasa.framework.spi.ras.ResultArchiveStoreBasicAttributesView;
import dev.galasa.framework.spi.ras.ResultArchiveStoreByteChannel;
import dev.galasa.framework.spi.ras.ResultArchiveStoreFileStore;
import dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystem;
import dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider;

public class RASFileSystemTest {

    @Test
    public void testProvider() throws IOException, URISyntaxException {
        final ResultArchiveStoreFileStore fileStore = new ResultArchiveStoreFileStore();
        final ResultArchiveStoreFileSystemProvider fsp = new ResultArchiveStoreFileSystemProvider(fileStore);
        final FileSystem fs = fsp.getActualFileSystem();

        Assert.assertEquals("Incorrect Scheme", "rasdummy", fsp.getScheme());
        Assert.assertEquals("Incorrect File System", fs, fsp.getFileSystem(null));
        Assert.assertEquals("Incorrect File System", fs, fsp.newFileSystem((URI) null, null));
        Assert.assertEquals("Incorrect File Store", fileStore, fsp.getActualFileStore());
        Assert.assertEquals("Incorrect File Store", fileStore, fsp.getFileStore(null));

        final URI uri = new URI("rasdummy:/dir1/testy.xml");
        final Path path = fsp.getPath(uri);
        final Path path2 = fsp.getPath(uri).resolve("boo");
        Assert.assertEquals("Incorrect Path", "/dir1/testy.xml", path.toString());

        Assert.assertEquals("Incorrect byte channel", ResultArchiveStoreByteChannel.class,
                fsp.newByteChannel(null, null).getClass());

        try {
            fsp.newDirectoryStream(null, null);
            fail("should have thrown an exception");
        } catch (final UnsupportedOperationException e) {
        }

        try {
            fsp.delete(null);
            fail("should have thrown an exception");
        } catch (final UnsupportedOperationException e) {
        }

        try {
            fsp.copy(null, null);
            fail("should have thrown an exception");
        } catch (final UnsupportedOperationException e) {
        }

        try {
            fsp.move(null, null);
            fail("should have thrown an exception");
        } catch (final UnsupportedOperationException e) {
        }

        Assert.assertTrue("Path should have been the same", fsp.isSameFile(path, path));
        Assert.assertFalse("Path should not have been the same", fsp.isSameFile(path, path2));
        Assert.assertFalse("should be not hidden", fsp.isHidden(null));
        fsp.createDirectory(null);

        try {
            fsp.checkAccess(path, AccessMode.EXECUTE);
            fail("should have thrown an exception");
        } catch (final UnsupportedOperationException e) {
        }

        try {
            fsp.checkAccess(path, AccessMode.READ);
            fail("should have thrown an exception");
        } catch (final UnsupportedOperationException e) {
        }

        fsp.checkAccess(path, AccessMode.WRITE);

        Assert.assertEquals("Should have return a view", ResultArchiveStoreBasicAttributesView.class,
                fsp.getFileAttributeView(path, BasicFileAttributeView.class).getClass());
        Assert.assertEquals("Should have return attributes", ResultArchiveStoreBasicAttributes.class,
                fsp.readAttributes(path, ResultArchiveStoreBasicAttributes.class).getClass());
        Assert.assertNull("return null", fsp.readAttributes(path, "lastAccess"));

        try {
            fsp.setAttribute(null, null, null);
            fail("should have thrown an exception");
        } catch (final UnsupportedOperationException e) {
        }
    }

    @Test
    public void testSystem() throws IOException, URISyntaxException {
        final ResultArchiveStoreFileStore fileStore = new ResultArchiveStoreFileStore();
        final ResultArchiveStoreFileSystemProvider fsp = new ResultArchiveStoreFileSystemProvider(fileStore);
        final ResultArchiveStoreFileSystem fs = (ResultArchiveStoreFileSystem) fsp.getActualFileSystem();
        fs.close(); // for CC

        final Iterator<Path> roots = fs.getRootDirectories().iterator();
        Assert.assertEquals("One root directory", "/", roots.next().toString());
        Assert.assertFalse("Only 1 root directory", roots.hasNext());

        Assert.assertEquals("Separator must be /", "/", fs.getSeparator());
        Assert.assertTrue("always open", fs.isOpen());
        Assert.assertFalse("initially not readonly", fs.isReadOnly());

        final Iterator<FileStore> stores = fs.getFileStores().iterator();
        Assert.assertEquals("One store", fileStore, stores.next());
        Assert.assertFalse("Only 1 store", stores.hasNext());

        Assert.assertEquals("2 supported Views", 2, fs.supportedFileAttributeViews().size());
        Assert.assertTrue("basic supported Views", fs.supportedFileAttributeViews().contains("basic"));
        Assert.assertTrue("ras supported Views", fs.supportedFileAttributeViews().contains("ras"));

        Assert.assertEquals("incorrect path conversion", "/one/two/three",
                fs.getPath("/one", "two", "three").toString());
        Assert.assertEquals("incorrect path conversion", "/one/two/three",
                fs.getPath(null, "/one", "two", null, "three").toString());
        Assert.assertEquals("incorrect path conversion", "/one/two/three", fs.getPath("/one/two/three").toString());

        Assert.assertNull("Dont support principals", fs.getUserPrincipalLookupService());

        Assert.assertFalse("initially not readonly", fs.isReadOnly());
        fs.setReadOnly(true);
        Assert.assertTrue("now readonly", fs.isReadOnly());
        fs.setReadOnly(false);

        try {
            fs.newWatchService();
            fail("should have thrown an exception");
        } catch (final UnsupportedOperationException e) {
        }
    }

    @Test
    public void testRegexGlob() {
        final String regex = ResultArchiveStoreFileSystem.createRegexFromGlob("\\*.x?l");
        Assert.assertEquals("glob coversion incorrect", "^\\\\.*\\.x.l$", regex);
    }

    @Test
    public void testPatternMatcher() {
        final ResultArchiveStoreFileStore fileStore = new ResultArchiveStoreFileStore();
        final ResultArchiveStoreFileSystemProvider fsp = new ResultArchiveStoreFileSystemProvider(fileStore);
        final ResultArchiveStoreFileSystem fs = (ResultArchiveStoreFileSystem) fsp.getActualFileSystem();

        Assert.assertEquals("incorrect pattern", "\\w+\\.xml", fs.getPathMatcher("regex:\\w+\\.xml").toString());
        Assert.assertEquals("incorrect pattern", "^.*\\.xml$", fs.getPathMatcher("glob:*.xml").toString());

        try {
            fs.getPathMatcher(":*.xml");
            fail("should have thrown an exception");
        } catch (final IllegalArgumentException e) {
        }

        try {
            fs.getPathMatcher("regex:");
            fail("should have thrown an exception");
        } catch (final IllegalArgumentException e) {
        }

        try {
            fs.getPathMatcher("eeek:xxx");
            fail("should have thrown an exception");
        } catch (final UnsupportedOperationException e) {
        }

        final PathMatcher matcher = fs.getPathMatcher("glob:*.xml");
        Assert.assertTrue("should have matched", matcher.matches(fs.getPath("test.xml")));
        Assert.assertFalse("should not have matched", matcher.matches(fs.getPath("test.png")));

    }

}