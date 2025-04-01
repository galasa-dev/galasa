/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.streams.internal.common;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.beans.generated.Stream;
import dev.galasa.framework.api.streams.internal.routes.StreamsTransform;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.utils.GalasaGson;

public class StreamsJsonTransformer {
    
    protected static final GalasaGson gson = new GalasaGson();

    public String getStreamsAsJsonString(List<IStream> listOfStreams, String baseServletUrl) {

        List<Stream> streams = new ArrayList<>();
        StreamsTransform streamsTransform = new StreamsTransform();

        for(IStream stream : listOfStreams) {
            Stream newStream = streamsTransform.createStreamBean(stream, baseServletUrl);
            streams.add(newStream);
        }

        return gson.toJson(streams);

    }

}
