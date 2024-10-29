/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras.directory;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.ResultArchiveStoreFileAttributeView;

/**
 * Test the Directory based Result Archive Store
 *
 *  
 *
 */
public class DirectoryRASSTest {

    private Path                     rasDirectory;

    private IFramework               framework;
    private IConfidentialTextService cts;

    private static final String runname = "BOB1";

    @Before
    public void before() throws IOException {
        this.rasDirectory = Files.createTempDirectory("galasa_junit_ras_");

        this.framework = mock(IFramework.class);
        this.cts = mock(IConfidentialTextService.class);

        when(this.framework.getTestRunName()).thenReturn(runname);
        when(this.framework.getConfidentialTextService()).thenReturn(cts);

        when(cts.removeConfidentialText(anyString())).thenAnswer(new Answer<String>(){
                public String answer(InvocationOnMock arg) {
                    return arg.getArgument(0);
                }
        });

    }

    @After
    public void after() throws IOException {
        if (this.rasDirectory != null) {
            if (Files.exists(this.rasDirectory)) {
                FileUtils.deleteDirectory(this.rasDirectory.toFile());
            }
        }
    }

    @Test
    public void testInitialise() throws ResultArchiveStoreException, IOException {
        final ArrayList<URI> rasURIs = new ArrayList<>();
        rasURIs.add(this.rasDirectory.toUri());

        final DirectoryResultArchiveStoreRegistration drass = new DirectoryResultArchiveStoreRegistration();

        IFrameworkInitialisation frameworkInit = mock(IFrameworkInitialisation.class);
        when(frameworkInit.getResultArchiveStoreUris()).thenReturn(rasURIs);
        when(frameworkInit.getFramework()).thenReturn(this.framework);

        drass.initialise(frameworkInit);

        verify(frameworkInit).registerResultArchiveStoreService(Mockito.any(DirectoryResultArchiveStoreService.class));
    }

    @Test
    public void testStoredArtifacts() throws ResultArchiveStoreException, IOException {
        DirectoryResultArchiveStoreService drass = new DirectoryResultArchiveStoreService(framework,
                this.rasDirectory.toUri());

        Assert.assertTrue("Run RAS directory should have been created",
                Files.exists(this.rasDirectory.resolve(runname)));
        Assert.assertTrue("Run RAS artifacts directory should have been created",
                Files.exists(this.rasDirectory.resolve(runname).resolve("artifacts")));

        final Path root = drass.getStoredArtifactsRoot();
        Assert.assertNotNull("RASS did not return a Stored Artifacts Root", root);

        final Path testFile = root.resolve("test.xml");

        Files.createFile(testFile, ResultArchiveStoreContentType.XML);
        Assert.assertTrue("text.xml should have been created",
                Files.exists(this.rasDirectory.resolve(runname).resolve("artifacts").resolve("test.xml")));

        Assert.assertEquals("Should be marked as XML", ResultArchiveStoreContentType.XML,
                Files.getFileAttributeView(testFile, ResultArchiveStoreFileAttributeView.class).getContentType());
    }

    @Test
    public void testTestStructure() throws ResultArchiveStoreException, UnsupportedEncodingException, IOException {
        DirectoryResultArchiveStoreService drass = new DirectoryResultArchiveStoreService(framework,
                this.rasDirectory.toUri());

        final TestStructure writeStructure = new TestStructure();
        writeStructure.setTestName("hello everyone");

        final Path pathStructure = this.rasDirectory.resolve(runname).resolve("structure.json");
        Assert.assertFalse("test structure file should not exist before write", Files.exists(pathStructure));

        drass.updateTestStructure(writeStructure);

        Assert.assertTrue("test structure file should now exist", Files.exists(pathStructure));

        final String json = new String(Files.readAllBytes(pathStructure), "utf-8");

        final GalasaGson gson = new GalasaGson();
        final TestStructure readStructure = gson.fromJson(json, TestStructure.class);

        Assert.assertEquals("Test structure data different", writeStructure.getTestName(), readStructure.getTestName());
    }

    @Test
    public void testRunLog() throws ResultArchiveStoreException, IOException {
        final String message1 = "1st message\n";
        final String message2 = "2nd message";
        final ArrayList<String> messages = new ArrayList<>();
        messages.add("3nd Message\n");
        messages.add("All done");

        DirectoryResultArchiveStoreService drass = new DirectoryResultArchiveStoreService(framework,
                this.rasDirectory.toUri());

        drass.writeLog(message1);
        drass.writeLog(message2);
        drass.writeLog(messages);

        final List<String> readMessages = Files.readAllLines(this.rasDirectory.resolve(runname).resolve("run.log"));

        Assert.assertEquals("message 1 wrong", message1, readMessages.get(0) + "\n");
        Assert.assertEquals("message 1 wrong", message2, readMessages.get(1));
        Assert.assertEquals("message 1 wrong", messages.get(0), readMessages.get(2) + "\n");
        Assert.assertEquals("message 1 wrong", messages.get(1), readMessages.get(3));
    }

}
