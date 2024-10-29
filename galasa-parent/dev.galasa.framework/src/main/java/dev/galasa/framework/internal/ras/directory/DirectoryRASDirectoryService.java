/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras.directory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasRunResultPage;
import dev.galasa.framework.spi.ras.RasSortField;
import dev.galasa.framework.spi.ras.RasTestClass;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;

public class DirectoryRASDirectoryService implements IResultArchiveStoreDirectoryService {

    public final static String ID_PREFIX = "local-";

    private final Path baseDirectory;
    private final GalasaGson gson;

    protected DirectoryRASDirectoryService(@NotNull Path baseDirectory, GalasaGson gson) {
        this.baseDirectory = baseDirectory;
        this.gson = gson;
    }

    @Override
    public @NotNull List<IRunResult> getRuns(@NotNull IRasSearchCriteria... searchCriteria) throws ResultArchiveStoreException{

        ArrayList<IRunResult> runs = new ArrayList<>();

        List<DirectoryRASRunResult> allRuns = getAllRuns();

        boolean matched = true;

        for(DirectoryRASRunResult run : allRuns) {
            matched = true;
            for(IRasSearchCriteria criteria : searchCriteria) {
                if(!criteria.criteriaMatched(run.getTestStructure())) {
                    matched = false;
                    break;
                }
            }
            if(matched) {
                runs.add(run);
            }
        }

        return runs;
    }

    @Override
    public @NotNull RasRunResultPage getRunsPage(int maxResults, RasSortField primarySort, String pageToken, @NotNull IRasSearchCriteria... searchCriteria)
            throws ResultArchiveStoreException {
        return new RasRunResultPage(getRuns(searchCriteria), null);
    }

    @Override
    public @NotNull String getName() {
        return "Local " + this.baseDirectory.toString();
    }

    @Override
    public boolean isLocal() {
        return true;
    }


    @Override
    public @NotNull List<String> getRequestors() throws ResultArchiveStoreException {
        HashSet<String> requestors = new HashSet<>();

        for (DirectoryRASRunResult result : getAllRuns()) {
            if(result!=null) {
                TestStructure testStructure = result.getTestStructure();
                if(testStructure != null && testStructure.getTestName()!=null) {
                    requestors.add(testStructure.getRequestor());
                }

            }
        }

        return new ArrayList<>(requestors);
    }

    @Override
    public @NotNull List<RasTestClass> getTests() throws ResultArchiveStoreException {
        HashMap<String,RasTestClass> tests = new HashMap<>();
        String key;
        for (DirectoryRASRunResult result : getAllRuns()) {
            if(result != null) {

                TestStructure testStructure = result.getTestStructure();
                if(testStructure != null && testStructure.getTestName()!=null) {
                    key = testStructure.getBundle()+"/"+testStructure.getTestName();
                    if(!tests.containsKey(key)){
                        tests.put(key,new RasTestClass(testStructure.getTestName(), testStructure.getBundle()));
                    }
                }
            }
        }

        return new ArrayList<>(tests.values());
    }

    @Override
    public @NotNull List<String> getResultNames() throws ResultArchiveStoreException {
        HashSet<String> results = new HashSet<>();

        for (DirectoryRASRunResult result : getAllRuns()) {
            if(result!=null) {
                TestStructure testStructure = result.getTestStructure();
                if(testStructure != null  ) {
                    if(testStructure.getResult()==null) {
                        results.add("UNKNOWN");
                    }else { 
                        results.add(testStructure.getResult());
                    }
                }

            }
        }

        return new ArrayList<>(results);
    }

    protected @NotNull List<DirectoryRASRunResult> getAllRuns() throws ResultArchiveStoreException {

        try {
            ArrayList<DirectoryRASRunResult> runs = new ArrayList<>();

            try (Stream<Path> stream = Files.list(Paths.get(baseDirectory.toUri()))) {
                stream.forEach(new ConsumeRuns(baseDirectory, runs, gson));
            }

            return runs;

        } catch (Throwable t) {
            throw new ResultArchiveStoreException("Unable to obtain runs", t);
        }

    }

    private static class ConsumeRuns implements Consumer<Path> {

        private final Path                        base;
        private final List<DirectoryRASRunResult> results;
        private final GalasaGson                        gson;
        private final Encoder                     encoder;

        private final Log                  logger = LogFactory.getLog(ConsumeRuns.class);

        public ConsumeRuns(Path base, List<DirectoryRASRunResult> results, GalasaGson gson) {
            this.base    = base;
            this.results = results;
            this.gson    = gson;
            this.encoder = Base64.getEncoder();
        }

        @Override
        public void accept(Path path) {
            if (!Files.isDirectory(path)) {
                return;
            }

            Path structureFile = path.resolve("structure.json");
            if (Files.exists(structureFile)) {
                try {
                    Path relativePath = base.relativize(path);
                    String id = ID_PREFIX + this.encoder.encodeToString(relativePath.toString().getBytes(StandardCharsets.UTF_8));

                    results.add(new DirectoryRASRunResult(path, gson, id));
                } catch (Throwable t) {
                    logger.trace("Unable to create a run result from " + structureFile.toString());
                }
            }

        }

    }

    @Override
    public IRunResult getRunById(@NotNull String runId) throws ResultArchiveStoreException {
        if (!runId.startsWith(ID_PREFIX)) {
            return null;
        }

        // runId = runId.substring(ID_PREFIX.length());

        try {
            String runSubPath = new String(Base64.getDecoder().decode(runId.substring(ID_PREFIX.length())), StandardCharsets.UTF_8);

            Path runPath = this.baseDirectory.resolve(runSubPath);

            if (!Files.exists(runPath)) {
                return null;
            }

            Path structureFile = runPath.resolve("structure.json");
            if (!Files.exists(structureFile)) {
                return null;
            }

            return new DirectoryRASRunResult(runPath, gson, runId);
        } catch(Exception e) {
            return null; // Ignore errors as this run id may not belong to this RAS  
        }
    }
}