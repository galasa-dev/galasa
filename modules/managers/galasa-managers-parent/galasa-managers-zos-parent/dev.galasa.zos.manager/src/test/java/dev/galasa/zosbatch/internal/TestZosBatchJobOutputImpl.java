/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosBatchJobOutputImpl {

    @Mock
    private IZosBatchJob zosBatchJobMock;

    private ZosBatchJobOutputImpl zosBatchJobOutput;

    private static final String JOBNAME = "jobname";

    private static final String JOBID = "jobid";

    private static final String STEPNAME = "stepname";

    private static final String PROCSTEP = "procstep";

    private static final String DDNAME = "ddname";

    private static final String ID = "id";

    private static final String RECORDS = "records";

    @Before
    public void setup() throws ZosBatchManagerException {
        zosBatchJobOutput = new ZosBatchJobOutputImpl(zosBatchJobMock, JOBNAME, JOBID);
    }

    @Test
    public void testAddJclCreatesSpoolFileWithGivenRecords() throws ZosBatchException {
        zosBatchJobOutput.addJcl(RECORDS);

        List<IZosBatchJobOutputSpoolFile> spoolFiles = zosBatchJobOutput.getSpoolFiles();
        assertThat(spoolFiles).hasSize(1);

        IZosBatchJobOutputSpoolFile expectedSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock, JOBNAME,
                JOBID, "", "", "JESJCLIN", "JCL", RECORDS);
        assertThat(spoolFiles.get(0)).usingRecursiveComparison().isEqualTo(expectedSpoolFile);
    }

    @Test
    public void testAddSingleSpoolFileAppendsNewSpoolFile() throws ZosBatchException {
        zosBatchJobOutput.addSpoolFile(STEPNAME, PROCSTEP, DDNAME, ID, RECORDS);

        List<IZosBatchJobOutputSpoolFile> spoolFiles = zosBatchJobOutput.getSpoolFiles();
        assertThat(spoolFiles).hasSize(1);

        IZosBatchJobOutputSpoolFile expectedSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock, JOBNAME,
                JOBID, STEPNAME, PROCSTEP, DDNAME, ID, RECORDS);
        assertThat(spoolFiles.get(0)).usingRecursiveComparison().isEqualTo(expectedSpoolFile);
    }

    @Test
    public void testAddDifferentSpoolFilesAppendsMultipleSpoolFiles() throws ZosBatchException {
        zosBatchJobOutput.addSpoolFile("STEP1", "PROCSTEP1", "DD1", "ID1", RECORDS);
        zosBatchJobOutput.addSpoolFile("STEP2", "PROCSTEP2", "DD2", "ID2", RECORDS);

        List<IZosBatchJobOutputSpoolFile> spoolFiles = zosBatchJobOutput.getSpoolFiles();
        assertThat(spoolFiles).hasSize(2);

        IZosBatchJobOutputSpoolFile expectedFirstSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock,
                JOBNAME, JOBID, "STEP1", "PROCSTEP1", "DD1", "ID1", RECORDS);
        assertThat(spoolFiles.get(0)).usingRecursiveComparison().isEqualTo(expectedFirstSpoolFile);

        IZosBatchJobOutputSpoolFile expectedSecondSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock,
                JOBNAME, JOBID, "STEP2", "PROCSTEP2", "DD2", "ID2", RECORDS);
        assertThat(spoolFiles.get(1)).usingRecursiveComparison().isEqualTo(expectedSecondSpoolFile);
    }

    @Test
    public void testAddSpoolFilesWithSameDDNameAppendsSpoolFiles() throws ZosBatchException {
        zosBatchJobOutput.addSpoolFile("STEP1", PROCSTEP, DDNAME, ID, RECORDS);
        zosBatchJobOutput.addSpoolFile("STEP2", PROCSTEP, DDNAME, ID, RECORDS);

        List<IZosBatchJobOutputSpoolFile> spoolFiles = zosBatchJobOutput.getSpoolFiles();
        assertThat(spoolFiles).hasSize(2);

        IZosBatchJobOutputSpoolFile expectedFirstSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock,
                JOBNAME, JOBID, "STEP1", PROCSTEP, DDNAME, ID, RECORDS);
        assertThat(spoolFiles.get(0)).usingRecursiveComparison().isEqualTo(expectedFirstSpoolFile);

        IZosBatchJobOutputSpoolFile expectedSecondSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock,
                JOBNAME, JOBID, "STEP2", PROCSTEP, DDNAME, ID, RECORDS);
        assertThat(spoolFiles.get(1)).usingRecursiveComparison().isEqualTo(expectedSecondSpoolFile);
    }

    @Test
    public void testAddSpoolFilesWithSameDDNameAndStepNameUpdatesExistingSpoolFile() throws ZosBatchException {
        zosBatchJobOutput.addSpoolFile(STEPNAME, PROCSTEP, DDNAME, ID, "");
        zosBatchJobOutput.addSpoolFile(STEPNAME, PROCSTEP, DDNAME, ID, RECORDS);

        List<IZosBatchJobOutputSpoolFile> spoolFiles = zosBatchJobOutput.getSpoolFiles();
        assertThat(spoolFiles).hasSize(1);

        IZosBatchJobOutputSpoolFile expectedSpoolFile = new ZosBatchJobOutputSpoolFileImpl(zosBatchJobMock, JOBNAME,
                JOBID, STEPNAME, PROCSTEP, DDNAME, ID, RECORDS);
        assertThat(spoolFiles.get(0)).usingRecursiveComparison().isEqualTo(expectedSpoolFile);
    }

    @Test
    public void testToListReturnsAListOfAllSpoolFileContents() throws ZosBatchException {
        zosBatchJobOutput.addJcl("JCL");
        assertThat(zosBatchJobOutput.toList()).isNotNull();
    }

    @Test
    public void testIteratorCreatesAnIteratorOverExistingSpoolFiles() throws ZosBatchException {
        zosBatchJobOutput.addJcl("JCL");
        Iterator<IZosBatchJobOutputSpoolFile> iterator = zosBatchJobOutput.iterator();

        assertThat(iterator.hasNext()).isTrue();

        assertThat(iterator.next()).isNotNull();

        String expectedMessage = "Object cannot be updated";
        assertThatThrownBy(() -> iterator.remove())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    public void testSizeReturnsTheNumberOfSpoolFilesThatExist() throws ZosBatchException {
        zosBatchJobOutput.addJcl("JCL");
        assertThat(zosBatchJobOutput.size()).isEqualTo(1);
    }

    @Test
    public void testIsEmptyShouldReturnTrueWhenNoSpoolFilesExist() throws ZosBatchException {
        assertThat(zosBatchJobOutput.isEmpty()).isTrue();
    }

    @Test
    public void testIsEmptyShouldReturnFalseWhenSpoolFilesExist() throws ZosBatchException {
        zosBatchJobOutput.addJcl("JCL");
        assertThat(zosBatchJobOutput.isEmpty()).isFalse();
    }
}
