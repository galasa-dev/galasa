/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.framework.spi.ras.ResultArchiveStorePath;

public class RASPathTest {

    @Test
    public void testNameElements() {
        ResultArchiveStorePath path = new ResultArchiveStorePath(FileSystems.getDefault(), "/this/is/a//path/");
        Assert.assertEquals("Incorrect name count", 4, path.getNameCount());

        Iterator<Path> pathElements = path.iterator();
        Assert.assertEquals("Incorrect segment 1", "this", pathElements.next().toString());
        Assert.assertEquals("Incorrect segment 2", "is", pathElements.next().toString());
        Assert.assertEquals("Incorrect segment 3", "a", pathElements.next().toString());
        Assert.assertEquals("Incorrect segment 4", "path", pathElements.next().toString());

        Assert.assertEquals("Incorrect FileName", "path", path.getFileName().toString());

        Assert.assertEquals("Incorrect Name for index 1", "is", path.getName(1).toString());
        Assert.assertNull("Incorrect Name for index -1, should be null", path.getName(-1));
        Assert.assertNull("Incorrect Name for index 4, should be null", path.getName(4));

        Assert.assertEquals("Incorrect Name for subpath 1,3", "is/a", path.subpath(1, 3).toString());
        Assert.assertFalse("Incorrect absolute for subpath 1,3", path.subpath(1, 3).isAbsolute());
        Assert.assertTrue("Incorrect absolute for subpath 0,2", path.subpath(0, 2).isAbsolute());

        Assert.assertNull("Incorrect Name for subpath 2,1, should be null", path.subpath(2, 1));
        Assert.assertNull("Incorrect Name for subpath -1,2, should be null", path.subpath(-1, 1));
        Assert.assertNull("Incorrect Name for subpath 4,5, should be null", path.subpath(4, 5));
        Assert.assertNull("Incorrect Name for subpath 3,4, should be null", path.subpath(3, 4));

        path = new ResultArchiveStorePath(FileSystems.getDefault(), "/");
        Assert.assertEquals("Incorrect name count", 0, path.getNameCount());
        pathElements = path.iterator();
        Assert.assertFalse("Should be no elements", pathElements.hasNext());
    }

    @Test
    public void testStartsWith() {
        final ResultArchiveStorePath path = new ResultArchiveStorePath(FileSystems.getDefault(), "/this/is/a/path");
        final ResultArchiveStorePath path1 = new ResultArchiveStorePath(FileSystems.getDefault(), "/this/is");
        final ResultArchiveStorePath path2 = new ResultArchiveStorePath(FileSystems.getDefault(), "this/is");
        final ResultArchiveStorePath path3 = new ResultArchiveStorePath(FileSystems.getDefault(), "/hello/there");
        final ResultArchiveStorePath path4 = new ResultArchiveStorePath(FileSystems.getDefault(),
                "/this/is/a/path/extra");

        Assert.assertTrue("same path, so should have been true", path.startsWith(path.toString()));
        Assert.assertTrue("valid path, so should have been true", path.startsWith(path1.toString()));
        Assert.assertFalse("different absolute, so should have been false", path.startsWith(path2.toString()));
        Assert.assertFalse("different path, so should have been false", path.startsWith(path3.toString()));
        Assert.assertFalse("longerpath, so should have been false", path.startsWith(path4.toString()));

        try {
            path.startsWith((Path) null);
            fail("null paths should caused an exception");
        } catch (final NullPointerException e) {
        }

        try {
            path.startsWith((String) null);
            fail("null paths should caused an exception");
        } catch (final NullPointerException e) {
        }

        try {
            path.startsWith(FileSystems.getDefault().getPath("bob"));
            fail("Different filesystem paths should caused an exception");
        } catch (final ProviderMismatchException e) {
        }

    }

    @Test
    public void testEndsWith() {
        final ResultArchiveStorePath path = new ResultArchiveStorePath(FileSystems.getDefault(), "/this/is/a/path");
        final ResultArchiveStorePath path1 = new ResultArchiveStorePath(FileSystems.getDefault(), "a/path");
        final ResultArchiveStorePath path2 = new ResultArchiveStorePath(FileSystems.getDefault(), "this/is/a/path");
        final ResultArchiveStorePath path3 = new ResultArchiveStorePath(FileSystems.getDefault(), "b/path");
        final ResultArchiveStorePath path4 = new ResultArchiveStorePath(FileSystems.getDefault(),
                "/this/is/a/path/extra");

        Assert.assertTrue("same path, so should have been true", path.endsWith(path.toString()));
        Assert.assertTrue("valid path, so should have been true", path.endsWith(path1.toString()));
        Assert.assertFalse("different absolute, so should have been false", path.endsWith(path2.toString()));
        Assert.assertFalse("different path, so should have been false", path.endsWith(path3.toString()));
        Assert.assertFalse("longerpath, so should have been false", path.endsWith(path4.toString()));

        try {
            path.endsWith((Path) null);
            fail("null paths should caused an exception");
        } catch (final NullPointerException e) {
        }

        try {
            path.endsWith((String) null);
            fail("null paths should caused an exception");
        } catch (final NullPointerException e) {
        }

        try {
            path.endsWith(FileSystems.getDefault().getPath("bob"));
            fail("Different filesystem paths should caused an exception");
        } catch (final ProviderMismatchException e) {
        }

    }

    @Test
    public void testSimpleStuff() throws IOException {
        final FileSystem fs = FileSystems.getDefault();

        final ResultArchiveStorePath path = new ResultArchiveStorePath(fs, "/this/is/a/path");
        Assert.assertEquals("Filesystem is different", fs, path.getFileSystem());
        Assert.assertEquals("Parent is incorrect", "/this/is/a", path.getParent().toString());

        final Path root = path.getRoot();
        Assert.assertEquals("Should have return root path", "/", root.toString());
        Assert.assertNull("Should have returned null for a root filename", root.getFileName());
        Assert.assertNull("Should have returned null for a root parent", root.getParent());

        Assert.assertTrue("Should be return the same path", path.normalize() == path);

        Assert.assertEquals("Incorrect URI", "file:" + path.toString(), path.toUri().toString());

        Assert.assertNull("Should have returned null for a real path", path.toRealPath());

        try {
            path.toFile();
            fail("Should have failed as unsupported");
        } catch (final UnsupportedOperationException e) {
            Assert.assertEquals("Incorrect exception message", "Unable to translate to a java.ioFile", e.getMessage());
        }

        try {
            path.register(null);
            fail("Should have failed as unsupported");
        } catch (final UnsupportedOperationException e) {
            Assert.assertEquals("Incorrect exception message", "Watching is not supported with this filesystem",
                    e.getMessage());
        }

        try {
            path.register(null, (Kind<?>[]) null, (Modifier) null);
            fail("Should have failed as unsupported");
        } catch (final UnsupportedOperationException e) {
            Assert.assertEquals("Incorrect exception message", "Watching is not supported with this filesystem",
                    e.getMessage());
        }

        try {
            new ResultArchiveStorePath(FileSystems.getDefault(), "/{}");
            fail("Should have failed as invalid uri");
        } catch (final AssertionError e) {
        }
    }

    @Test
    public void testRelativize() {
        final FileSystem fs = FileSystems.getDefault();

        final ResultArchiveStorePath path = new ResultArchiveStorePath(fs, "/this/is/a/path");
        final ResultArchiveStorePath path1 = new ResultArchiveStorePath(fs, "/this/is/a/path/with/extra");

        final ResultArchiveStorePath path2 = new ResultArchiveStorePath(fs, "this/is/a/path");
        final ResultArchiveStorePath path3 = new ResultArchiveStorePath(fs, "this/is/a/path/with/extra");
        final ResultArchiveStorePath path4 = new ResultArchiveStorePath(fs, "is/a/path/with/extra");

        Assert.assertEquals("incorrect sub path", "with/extra", path.relativize(path1).toString());
        Assert.assertEquals("incorrect sub path, should be empty for same path", "", path.relativize(path).toString());

        Assert.assertEquals("incorrect sub path", path.toString(), path2.relativize(path).toString());
        Assert.assertNull("incorrect sub path", path.relativize(path2));
        Assert.assertNull("incorrect sub path", path3.relativize(path4));

    }

    @Test
    public void testResolve() {
        final FileSystem fs = FileSystems.getDefault();

        final ResultArchiveStorePath path = new ResultArchiveStorePath(fs, "/this/is/a/path");
        final ResultArchiveStorePath path1 = new ResultArchiveStorePath(fs, "with/extra");
        final ResultArchiveStorePath path2 = new ResultArchiveStorePath(fs, "/already/absolute");
        final ResultArchiveStorePath path3 = new ResultArchiveStorePath(fs, "");

        Assert.assertEquals("Combined name is wrong", "/this/is/a/path/with/extra",
                path.resolve(path1.toString()).toString());
        Assert.assertTrue("absolute should have returned itself", path2 == path.resolve(path2));

        Assert.assertEquals("Combined name is wrong", "/this/is/a/with/extra",
                path.resolveSibling(path1.toString()).toString());
        Assert.assertTrue("absolute should have returned itself", path2 == path.resolveSibling(path2));
        Assert.assertTrue("empty should have returned itself", path1 == path3.resolveSibling(path1));

    }

    @Test
    public void testAbsolute() {
        String pathName = "/this/is/a/path";
        ResultArchiveStorePath path = new ResultArchiveStorePath(FileSystems.getDefault(), pathName);
        Assert.assertTrue("Should be absolute path", path.isAbsolute());
        Assert.assertEquals("path name changed", pathName, path.toString());

        pathName = "this/is/a/path";
        path = new ResultArchiveStorePath(FileSystems.getDefault(), "this/is/a/path");
        Assert.assertFalse("Should not be absolute path", path.isAbsolute());
        Assert.assertEquals("path name changed", pathName, path.toString());

        Assert.assertEquals("Incorrect URI", "file:/" + path.toString(), path.toUri().toString());

        Assert.assertEquals("should stay as non absolute", pathName, path.unAbsolute().toString());
    }

    @Test
    public void testInvalidElements() {
        String path = "/this/./a/path";
        try {
            new ResultArchiveStorePath(FileSystems.getDefault(), path);
            fail("Should have failed as . is not allowed");
        } catch (final InvalidPathException e) {
            Assert.assertEquals("Incorrect message", "Path parts of '.' are not allowed: " + path, e.getMessage());
        }

        path = "/this/../a/path";
        try {
            new ResultArchiveStorePath(FileSystems.getDefault(), path);
            fail("Should have failed as .. is not allowed");
        } catch (final InvalidPathException e) {
            Assert.assertEquals("Incorrect message", "Path parts of '..' are not allowed: " + path, e.getMessage());
        }

        path = "/this/a~a/a/path";
        try {
            new ResultArchiveStorePath(FileSystems.getDefault(), path);
            fail("Should have failed as ~ is not allowed");
        } catch (final InvalidPathException e) {
            Assert.assertEquals("Incorrect message", "Path parts with '~' are not allowed: " + path, e.getMessage());
        }

        path = "/this/a=a/a/path";
        try {
            new ResultArchiveStorePath(FileSystems.getDefault(), path);
            fail("Should have failed as ~ is not allowed");
        } catch (final InvalidPathException e) {
            Assert.assertEquals("Incorrect message", "Path parts with '=' are not allowed: " + path, e.getMessage());
        }
    }

    @Test
    public void testCompareTo() {
        final FileSystem fs = FileSystems.getDefault();
        "boo".compareTo("eek");

        final ResultArchiveStorePath patha = new ResultArchiveStorePath(fs, "/this/is/a/path");
        final ResultArchiveStorePath pathb = new ResultArchiveStorePath(fs, "/this/is/b/path");
        final ResultArchiveStorePath pathc = new ResultArchiveStorePath(fs, "/this/is/c/path");
        final ResultArchiveStorePath pathbshort = new ResultArchiveStorePath(fs, "/this/is/b");
        final ResultArchiveStorePath pathroot = new ResultArchiveStorePath(fs, "/");

        Assert.assertEquals("Should have been equal", 0, pathb.compareTo(pathb));
        Assert.assertEquals("Should have been before", -1, pathb.compareTo(pathc));
        Assert.assertEquals("Should have been after", 1, pathb.compareTo(patha));
        Assert.assertEquals("Should have been after", 1, pathb.compareTo(pathbshort));
        Assert.assertEquals("Should have been before", -1, pathbshort.compareTo(pathb));
        Assert.assertEquals("Should have been after", 1, pathb.compareTo(pathroot));
        Assert.assertEquals("Should have been before", -1, pathroot.compareTo(pathb));
        Assert.assertEquals("Should have been equal", 0, pathroot.compareTo(pathroot));
        try {
            pathb.compareTo(fs.getPath("bob"));
            fail("Should have failed as different filesystem");
        } catch (final ProviderMismatchException e) {
        }

    }

}