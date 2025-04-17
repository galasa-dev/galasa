/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.streams.internal.routes;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.beans.generated.Stream;
import dev.galasa.framework.api.beans.generated.StreamMetadata;
import dev.galasa.framework.api.beans.generated.StreamOBRData;
import dev.galasa.framework.api.beans.generated.StreamRepository;
import dev.galasa.framework.api.beans.generated.StreamTestCatalog;
import dev.galasa.framework.api.beans.generated.StreamData;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.IStream;

public class StreamsTransform {

    public Log logger = LogFactory.getLog(StreamsTransform.class);

    /**
     * Creates a list with a single Stream bean based on the provided properties.
     */
    public Stream createStreamBean(IStream stream, String apiServerUrl) {

        Stream streamBean = new Stream();
        streamBean.setApiVersion(GalasaResourceValidator.DEFAULT_API_VERSION);
        streamBean.setkind("GalasaStream");

        // Build metadata (name, url, description)
        StreamMetadata streamMetadata = createStreamMetadata(stream, apiServerUrl);
        streamBean.setmetadata(streamMetadata);

        // Build data section (isEnabled, repository, obrs, testCatalog)
        StreamData streamData = createStreamData(stream);
        streamBean.setdata(streamData);

        return streamBean;
    }

    /**
     * Creates the metadata section of the Stream bean.
     * It looks for keys that start with "test.stream." and are not part of the data
     * section.
     * For example: "test.stream.mystream.description" and
     * "test.stream.mystream.url".
     */
    private StreamMetadata createStreamMetadata(IStream stream, String apiServerUrl) {
        logger.info("Building stream metadata");

        String streamName = stream.getName();
        StreamMetadata metadata = new StreamMetadata();
        metadata.setname(streamName);

        String streamDescription = stream.getDescription();
        if (streamDescription != null && !streamDescription.isBlank()) {
            metadata.setdescription(stream.getDescription());
        }

        metadata.seturl(apiServerUrl + "/streams/" + streamName);
        return metadata;
    }

    /**
     * Creates the data section of the Stream bean.
     * It processes keys for isEnabled, repository, obrs and testCatalog.
     */
    private StreamData createStreamData(IStream stream) {

        StreamData data = new StreamData();
        URL testCatalogUrl = stream.getTestCatalogUrl();
        URL mavenRepositoryUrl = stream.getMavenRepositoryUrl();
        List<IOBR> obrs = stream.getObrs();

        StreamRepository streamRepository = new StreamRepository();
        if(mavenRepositoryUrl != null) {
            streamRepository.seturl(mavenRepositoryUrl.toString());
        }

        StreamTestCatalog testCatalog = new StreamTestCatalog();
        if(testCatalogUrl != null){
            testCatalog.seturl(testCatalogUrl.toString());
        }

        List<StreamOBRData> streamObrData = transformObrsIntoStreamObrBeanList(obrs);

        data.setobrs(streamObrData.toArray(new StreamOBRData[0]));
        data.setTestCatalog(testCatalog);
        data.setrepository(streamRepository);
        data.setIsEnabled(true);

        return data;

    }

    private List<StreamOBRData> transformObrsIntoStreamObrBeanList(List<IOBR> obrs) {

        List<StreamOBRData> streamObrData = new ArrayList<>();

        for (IOBR obr : obrs) {
            StreamOBRData obrData = new StreamOBRData();
            obrData.setGroupId(obr.getGroupId());
            obrData.setArtifactId(obr.getArtifactId());
            obrData.setversion(obr.getVersion());

            streamObrData.add(obrData);
        }

        return streamObrData;
    }
}
