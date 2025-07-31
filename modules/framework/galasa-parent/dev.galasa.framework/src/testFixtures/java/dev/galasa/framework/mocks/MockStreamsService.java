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
                break;
            }
        }

        return streamToReturn;

    }

    @Override
    public void deleteStream(String streamName) throws StreamsException {
        if(throwException) {
            throwStreamsException();
        }

        for (IStream stream : streams) {
            if (stream.getName().equals(streamName)) {
                streams.remove(stream);
                break;
            }
        }
    }

    @Override
    public void setStream(IStream stream) throws StreamsException {

        IStream streamToReplace = null;
        for (IStream existingStream : this.streams) {
            if (existingStream.getName().equals(stream.getName())) {
                streamToReplace = existingStream;
                break;
            }
        }

        // If there's an existing stream with the same name, simulate an update by replacing it with the new stream
        if (streamToReplace != null) {
            this.streams.remove(streamToReplace);
        }
        this.streams.add(stream);
    }

}
