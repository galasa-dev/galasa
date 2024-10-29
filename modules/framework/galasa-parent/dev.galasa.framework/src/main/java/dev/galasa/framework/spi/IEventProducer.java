package dev.galasa.framework.spi;

import dev.galasa.framework.spi.events.IEvent;

public interface IEventProducer {

    void sendEvent(IEvent event);

    void close();
    
}
