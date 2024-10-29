/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.comms;

import java.util.List;

import dev.galasa.zos3270.internal.datastream.AbstractCommandCode;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.StructuredField;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;

public class Inbound3270Message {

    private final AbstractCommandCode   commandCode;
    private final WriteControlCharacter writeControlCharacter;
    private final List<AbstractOrder>   orders;
    private final List<StructuredField> structuredFields;

    public Inbound3270Message(AbstractCommandCode commandCode, WriteControlCharacter writeControlCharacter,
            List<AbstractOrder> orders) {
        this.commandCode = commandCode;
        this.writeControlCharacter = writeControlCharacter;
        this.orders = orders;
        this.structuredFields = null;
    }

    public Inbound3270Message(AbstractCommandCode commandCode, List<StructuredField> structuredFields) {
        this.commandCode = commandCode;
        this.writeControlCharacter = null;
        this.orders = null;
        this.structuredFields = structuredFields;
    }

    public AbstractCommandCode getCommandCode() {
        return commandCode;
    }

    public WriteControlCharacter getWriteControlCharacter() {
        return writeControlCharacter;
    }

    public List<AbstractOrder> getOrders() {
        return orders;
    }

    public List<StructuredField> getStructuredFields() {
        return structuredFields;
    }
}
