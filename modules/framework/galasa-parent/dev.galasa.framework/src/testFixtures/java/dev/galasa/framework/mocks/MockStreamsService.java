/*
* Copyright contributors to the Galasa project
*
* SPDX-License-Identifier: EPL-2.0
*/
package dev.galasa.framework.mocks;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.StreamsException;

public class MockStreamsService implements IStreamsService {

    List<IStream> streams = new ArrayList<>();
    private boolean throwException = false;

    public MockStreamsService(List<IStream> streams) {
        this.streams = streams;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    private void throwStreamsException() throws StreamsException {
        throw new StreamsException("simulating an unexpected failure!");
    }

    @Override
    public List<IStream> getStreams() throws StreamsException {
        if(throwException) {
            throwStreamsException();
        }
        return streams;
    }

    @Override
    public IStream getStreamByName(String streamName) throws StreamsException {

        if(throwException) {
            throwStreamsException();
        }

        IStream streamToReturn = null;
        for (IStream stream : streams) {
            if (stream.getName().equals(streamName)) {
                streamToReturn = stream;
            }
        }

        return streamToReturn;

    }

}
