/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras.directory;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedFrom;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRequestor;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTestName;
import dev.galasa.framework.spi.teststructure.TestStructure;


public class RASGetRunsTest{
	
	@Before
	public void before() {
		
	}
	
	@Test
	public void testToCheckGetRunsImplementation() throws ResultArchiveStoreException, IOException {

		ArrayList<DirectoryRASRunResult> testArray = new ArrayList<>();
		
		Instant start = Instant.parse("2020-02-03T11:25:30.00Z");
		Instant end = Instant.parse("2020-10-03T11:26:30.00Z");
		
		TestStructure struc = new TestStructure();
		struc.setTestName("Test");
		struc.setRequestor("Bob");
		struc.setStartTime(start);
		struc.setEndTime(end);
		
		TestStructure struc2 = new TestStructure();
		struc2.setTestName("Test2");
		struc2.setRequestor("Jim");
		struc2.setStartTime(start);
		struc2.setEndTime(end);
		
		TestStructure struc3 = new TestStructure();
		struc3.setTestName("Test3");
		struc3.setRequestor("Simon");
		struc3.setStartTime(start);
		struc3.setEndTime(end);
		
		DirectoryRASRunResult res = new DirectoryRASRunResult() {
			@Override
			public TestStructure getTestStructure() {
				return struc;
			}
		};
		
		DirectoryRASRunResult res2 = new DirectoryRASRunResult() {
			@Override
			public TestStructure getTestStructure() {
				return struc2;
			}
		};
		
		DirectoryRASRunResult res3 = new DirectoryRASRunResult() {
			@Override
			public TestStructure getTestStructure() {
				return struc3;
			}
		};
		
		testArray.add(res);
		testArray.add(res2);
		testArray.add(res3);
		
		DirectoryRASDirectoryService dummy = new DirectoryRASDirectoryService(null, null) {
		
			@Override
			protected List<DirectoryRASRunResult> getAllRuns() throws ResultArchiveStoreException{
				return testArray;
			};
			
		};
		
		RasSearchCriteriaQueuedFrom from = new RasSearchCriteriaQueuedFrom(start);
		RasSearchCriteriaQueuedTo to = new RasSearchCriteriaQueuedTo(end);
		RasSearchCriteriaRequestor requestor = new RasSearchCriteriaRequestor("Bob");
		RasSearchCriteriaTestName testName = new RasSearchCriteriaTestName("Test");
		
		IRasSearchCriteria[] criteria = {from, to, requestor, testName};
		
		List<IRunResult> result = dummy.getRuns(criteria);
		
		assertThat(result.get(0).getTestStructure().getTestName().equals(struc.getTestName()));
		assertThat(result.get(0).getTestStructure().getRequestor().equals(struc.getRequestor()));
		assertThat(result.get(0).getTestStructure().getStartTime().equals(struc.getStartTime()));
		assertThat(result.get(0).getTestStructure().getEndTime().equals(struc.getEndTime()));
		
		assertThat(result.size() == 1);
	}
	
}


