/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.ErrorTextFoundException;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.IDatastreamListener;
import dev.galasa.zos3270.IDatastreamListener.DatastreamDirection;
import dev.galasa.zos3270.IScreenUpdateListener;
import dev.galasa.zos3270.IScreenUpdateListener.Direction;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.datastream.AbstractCommandCode;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.AbstractQueryReply;
import dev.galasa.zos3270.internal.datastream.AttributeBackgroundColour;
import dev.galasa.zos3270.internal.datastream.AttributeExtendedHighlighting;
import dev.galasa.zos3270.internal.datastream.AttributeForegroundColour;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.internal.datastream.CommandEraseWrite;
import dev.galasa.zos3270.internal.datastream.CommandEraseWriteAlternate;
import dev.galasa.zos3270.internal.datastream.CommandReadBuffer;
import dev.galasa.zos3270.internal.datastream.CommandReadModified;
import dev.galasa.zos3270.internal.datastream.CommandReadModifiedAll;
import dev.galasa.zos3270.internal.datastream.CommandWriteStructured;
import dev.galasa.zos3270.internal.datastream.OrderCarrageReturn;
import dev.galasa.zos3270.internal.datastream.OrderEndOfMedium;
import dev.galasa.zos3270.internal.datastream.OrderEraseUnprotectedToAddress;
import dev.galasa.zos3270.internal.datastream.OrderFormFeed;
import dev.galasa.zos3270.internal.datastream.OrderGraphicsEscape;
import dev.galasa.zos3270.internal.datastream.OrderInsertCursor;
import dev.galasa.zos3270.internal.datastream.OrderModifyField;
import dev.galasa.zos3270.internal.datastream.OrderNewLine;
import dev.galasa.zos3270.internal.datastream.OrderRepeatToAddress;
import dev.galasa.zos3270.internal.datastream.OrderSetAttribute;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderStartFieldExtended;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.QueryReplyCharactersets;
import dev.galasa.zos3270.internal.datastream.QueryReplyColor;
import dev.galasa.zos3270.internal.datastream.QueryReplyHighlite;
import dev.galasa.zos3270.internal.datastream.QueryReplyImplicitPartition;
import dev.galasa.zos3270.internal.datastream.QueryReplyNull;
import dev.galasa.zos3270.internal.datastream.QueryReplySummary;
import dev.galasa.zos3270.internal.datastream.QueryReplyUsableArea;
import dev.galasa.zos3270.internal.datastream.StructuredField;
import dev.galasa.zos3270.internal.datastream.StructuredField3270DS;
import dev.galasa.zos3270.internal.datastream.StructuredFieldReadPartition;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.internal.terminal.ScreenUpdateTextListener;

/**
 * Screen representation of the 3270 terminal
 * 
 *  
 *
 */
public class Screen {

    private static final String                     CANT_FIND_TEXT  = "Unable to find a field containing '";
    
    private final Charset                           codePage;

    private final Log                               logger          = LogFactory.getLog(getClass());

    private final Network                           network;

    private boolean                                 usingAlternate;
    private IBufferHolder[]                         buffer;
    private int                                     screenSize;
    private int                                     columns;
    private int                                     rows;

    private final boolean                           hasAlternate;
    private final int                               primaryColumns;
    private final int                               primaryRows;
    private final int                               alternateColumns;
    private final int                               alternateRows;

    // Determines whether the screen is displaying data for a session between a System Services Control Point (SSCP) and a Logical Unit (LU), forming an SSCP-LU session.
    // 
    // An SSCP manages network resources and coordinates the initiation and termination of sessions between applications, while an LU identifies a client session where
    // end users and applications can exchange data. See the 3270 manager's README for more details about SSCP-LU sessions.
    private boolean                                 isSSCPLUDisplay = false;

    private int                                     initialCursorPosition = 0;
    private int                                     workingCursor   = 0;
    private int                                     screenCursor    = 0;

    private Semaphore                               keyboardLock    = new Semaphore(1, true);
    private boolean                                 keyboardLockSet = false;

    private final LinkedList<IDatastreamListener> datastreamListeners =  new LinkedList<>();

    private final LinkedList<IScreenUpdateListener> updateListeners = new LinkedList<>();

    private AttentionIdentification                 lastAid = AttentionIdentification.NONE;
    
    private boolean                                 detectedSetAttribute = false;

    /**
     * @deprecated use the {@link #Screen(TerminalSize primarySize, TerminalSize alternateSize, Network network, Charset codePage)}
     * constructor instead.  
     */
    @Deprecated(since = "0.28.0", forRemoval = true)
    public Screen() throws TerminalInterruptedException {
        this(80, 24, null);
    }

    /**
     * @deprecated use the {@link #Screen(TerminalSize primarySize, TerminalSize alternateSize, Network network, Charset codePage)}
     * constructor instead.  
     */
    @Deprecated(since = "0.28.0", forRemoval = true)
    public Screen(int columns, int rows, Network network) throws TerminalInterruptedException {
        this(columns, rows, 0, 0, network);
    }

    /**
     * @deprecated use the {@link #Screen(TerminalSize primarySize, TerminalSize alternateSize, Network network, Charset codePage)}
     * constructor instead.  
     */
    @Deprecated(since = "0.28.0", forRemoval = true)
    public Screen(int columns, int rows, int alternateColumns, int alternateRows, Network network) throws TerminalInterruptedException {
        this.codePage = Charset.forName("Cp037");
        this.network = network;
        this.primaryColumns = columns;
        this.primaryRows = rows;
        this.usingAlternate = false;
        if (alternateRows < 1 || alternateColumns < 1) {
            this.hasAlternate = false;
            this.alternateColumns = 0;
            this.alternateRows    = 0;
        } else {
            this.hasAlternate = true;
            this.alternateColumns = alternateColumns;
            this.alternateRows    = alternateRows;
        }

        erase();
        lockKeyboard();
    }


    public Screen(TerminalSize primarySize, TerminalSize alternateSize, Network network, Charset codePage) throws TerminalInterruptedException {
        this.codePage = codePage;
        this.network = network;
        this.primaryColumns = primarySize.getColumns();
        this.primaryRows = primarySize.getRows();
        this.usingAlternate = false;

        int alternateRows = alternateSize.getRows();
        int alternateColumns = alternateSize.getColumns();
        if (alternateRows < 1 || alternateColumns < 1) {
            this.hasAlternate = false;
            this.alternateColumns = 0;
            this.alternateRows    = 0;
        } else {
            this.hasAlternate = true;
            this.alternateColumns = alternateColumns;
            this.alternateRows    = alternateRows;
        }

        erase();
        lockKeyboard();
    }

    public synchronized void lockKeyboard() throws TerminalInterruptedException {
        if (!keyboardLockSet) {
            logger.trace("Locking keyboard");
            keyboardLockSet = true;
            try {
                keyboardLock.acquire();
            } catch (InterruptedException e) {
                throw new TerminalInterruptedException("Lock keyboard was interrupted", e);
            }
        }
    }

    public void networkClosed() throws TerminalInterruptedException {
        lockKeyboard();
    }

    private synchronized void unlockKeyboard() {
        if (keyboardLockSet) {
            logger.trace("Unlocking keyboard");
            keyboardLockSet = false;
            keyboardLock.release();
        }
    }

    public synchronized void processInboundMessage(Inbound3270Message inbound) throws DatastreamException {
        AbstractCommandCode commandCode = inbound.getCommandCode();
        if (commandCode instanceof CommandWriteStructured) {
            processStructuredFields(inbound.getStructuredFields());
        } else if (commandCode instanceof CommandReadBuffer) {
            processReadBuffer();
        } else if (commandCode instanceof CommandReadModified) {
            processReadModified(false);
        } else if (commandCode instanceof CommandReadModifiedAll) {
            processReadModified(true);
        } else {
            WriteControlCharacter writeControlCharacter = inbound.getWriteControlCharacter();
            List<AbstractOrder> orders = inbound.getOrders();

            if (commandCode instanceof CommandEraseWrite) {
                erase();
            } else if (commandCode instanceof CommandEraseWriteAlternate) {
                eraseAlternate();
            }

            if (writeControlCharacter.isResetMDT()) {
                resetMdt();
            }

            this.workingCursor = this.screenCursor;

            processOrders(orders, writeControlCharacter);

            // Set the initial cursor position after processing the inbound data stream's orders
            // in case the cursor has moved
            this.initialCursorPosition = this.screenCursor;
        }

    }

    private void resetMdt() {
        for(int i = 0; i < this.screenSize; i++) {
            IBufferHolder bh = this.buffer[i];
            if (bh instanceof BufferStartOfField) {
                BufferStartOfField sof = (BufferStartOfField) bh;
                sof.clearFieldModified();
            }
        }
    }

    private synchronized void processReadBuffer() throws DatastreamException {
        try {
            ByteArrayOutputStream outboundBuffer = new ByteArrayOutputStream();

            outboundBuffer.write(this.lastAid.getKeyValue());

            BufferAddress cursor = new BufferAddress(this.screenCursor);
            outboundBuffer.write(cursor.getCharRepresentation());

            for(IBufferHolder bh : this.buffer) {
                if (bh == null) {
                    outboundBuffer.write(0);
                } else if (bh instanceof BufferGraphicsEscape) {
                    BufferGraphicsEscape bc = (BufferGraphicsEscape) bh;
                    outboundBuffer.write(OrderGraphicsEscape.ID);
                    outboundBuffer.write(bc.getFieldEbcdic(this.codePage));
                } else if (bh instanceof BufferChar) {
                    BufferChar bc = (BufferChar) bh;
                    outboundBuffer.write(bc.getFieldEbcdic(this.codePage));
                } else if (bh instanceof BufferStartOfField) {
                    BufferStartOfField sf = (BufferStartOfField) bh;
                    OrderStartField osf = new OrderStartField(sf.isProtected(), sf.isNumeric(), sf.isDisplay(), sf.isIntenseDisplay(), sf.isSelectorPen(), sf.isFieldModifed());
                    outboundBuffer.write(osf.getBytes());
                } else {
                    throw new DatastreamException("Unrecognised Buffer Holder - " + bh.getClass().getName());
                }
            }
            String outboundHex = new String(Hex.encodeHex(outboundBuffer.toByteArray()));
            setDatastreamListenersDirection(outboundHex, DatastreamDirection.OUTBOUND);

            this.network.sendDatastream(outboundBuffer.toByteArray());
        } catch(Exception e) {
            throw new DatastreamException("Error whilst processing READ BUFFER", e);
        }

    }

    public void setDatastreamListenersDirection(String datastreamHex, DatastreamDirection direction) {
        if (logger.isTraceEnabled()) {
            logger.trace(direction.toString() + "=" + datastreamHex);
        }

        for (IDatastreamListener listener : this.datastreamListeners) {
            listener.datastreamUpdate(direction, datastreamHex);
        }
    }

    
    private synchronized void processReadModified(boolean all) throws DatastreamException {
        try {
            ByteArrayOutputStream outboundBuffer = new ByteArrayOutputStream();

            outboundBuffer.write(this.lastAid.getKeyValue());

            if (!all && (this.lastAid == AttentionIdentification.CLEAR 
                    || this.lastAid == AttentionIdentification.PA1
                    || this.lastAid == AttentionIdentification.PA2
                    || this.lastAid == AttentionIdentification.PA3)) {
                //  dont send anything other than the aid key
            } else {
                BufferAddress cursor = new BufferAddress(this.screenCursor);
                outboundBuffer.write(cursor.getCharRepresentation());

                readModifiedBuffer(outboundBuffer);
            }

            String outboundHex = new String(Hex.encodeHex(outboundBuffer.toByteArray()));
            setDatastreamListenersDirection(outboundHex, DatastreamDirection.OUTBOUND);

            this.network.sendDatastream(outboundBuffer.toByteArray());
        } catch(Exception e) {
            throw new DatastreamException("Error whilst processing READ BUFFER", e);
        }

    }

    private void readModifiedBuffer(ByteArrayOutputStream outboundBuffer) throws IOException {
        boolean fieldModified = false;

        // *** Locate the first StartOfField in the buffer, if absent, then unformatted,
        // send everything back.

        int start = 0;
        int end = 0;
        for (; start < buffer.length; start++) {
            if (buffer[start] instanceof BufferStartOfField) {
                break;
            }
        }

        if (start >= buffer.length) { // indicates unfromatted, send it all
            start = 0;
            end = buffer.length - 1;

            // OrderSetBufferAddress sba = new OrderSetBufferAddress(new BufferAddress(0));
            // outboundBuffer.write(sba.getCharRepresentation());
            fieldModified = true;
        } else { // formatted
            end = start - 1;
            if (end < 0) {
                end = buffer.length - 1;
            }
        }

        int pos = start;
        while (true) {
            IBufferHolder bh = buffer[pos];
            if (bh instanceof BufferStartOfField) {
                BufferStartOfField bsf = (BufferStartOfField) bh;
                fieldModified = bsf.isFieldModifed();

                if (fieldModified) { // Send whether unprotected or not
                    OrderSetBufferAddress sba = new OrderSetBufferAddress(new BufferAddress(pos + 1));
                    outboundBuffer.write(sba.getCharRepresentation());
                }
            } else if (bh instanceof BufferGraphicsEscape) {
                BufferGraphicsEscape bc = (BufferGraphicsEscape) bh;
                if (fieldModified) {
                    outboundBuffer.write(OrderGraphicsEscape.ID);
                    byte value = bc.getFieldEbcdic(this.codePage);
                    outboundBuffer.write(value);
                }
            } else if (bh instanceof BufferChar) {
                BufferChar bc = (BufferChar) bh;
                if (fieldModified) {
                    byte value = bc.getFieldEbcdic(this.codePage);
                    if (value != 0) {
                        outboundBuffer.write(value);
                    }
                }
            }

            if (pos == end) {
                break;
            }

            pos++;
            if (pos >= buffer.length) {
                pos = 0;
            }
        }
    }

    private synchronized void processStructuredFields(List<StructuredField> structuredFields)
            throws DatastreamException {
        for (StructuredField structuredField : structuredFields) {
            if (structuredField instanceof StructuredFieldReadPartition) {
                processReadPartition((StructuredFieldReadPartition) structuredField);
            } else if (structuredField instanceof StructuredField3270DS) {
                processInboundMessage(((StructuredField3270DS) structuredField).getInboundMessage());
            } else {
                throw new DatastreamException("Unsupported Structured Field - " + structuredField.getClass().getName());
            }
        }
    }

    private synchronized void processReadPartition(StructuredFieldReadPartition readPartition)
            throws DatastreamException {
        switch (readPartition.getType()) {
        case QUERY:
            processReadPartitionQuery();
            return;
        case QUERY_LIST:
            processReadPartitionQueryList(readPartition);
            return;
        default:
            throw new DatastreamException(
                    "Unsupported Read Partition Type - " + readPartition.getType().toString());
        }

    }

    private synchronized void processReadPartitionQuery() throws DatastreamException {
        List<AbstractQueryReply> replies = getAllSupportedReplies();
        QueryReplySummary summary = new QueryReplySummary(replies);

        sendQueryReplies(summary, replies);
    }

    private synchronized void processReadPartitionQueryList(StructuredFieldReadPartition readPartition) throws DatastreamException {
        switch (readPartition.getRequestType()) {
        case StructuredFieldReadPartition.REQTYP_LIST:
            List<AbstractQueryReply> supportedReplies = getAllSupportedReplies();
            ArrayList<AbstractQueryReply> replies = prepareQueryListResponse(supportedReplies, readPartition.getQcodes());

            sendQueryReplies(new QueryReplySummary(supportedReplies), replies);
            return;
        case StructuredFieldReadPartition.REQTYP_ALL:
        case StructuredFieldReadPartition.REQTYP_EQUIVALENT:
            processReadPartitionQuery();
            return;
        default:
            throw new DatastreamException(
                    "Unsupported Read Partition Request Type code = " + readPartition.getRequestType());
        }
    }

    private ArrayList<AbstractQueryReply> prepareQueryListResponse(List<AbstractQueryReply> supportedReplies, Set<Byte> requestedQcodes) {
        ArrayList<AbstractQueryReply> replies = new ArrayList<>();
        for (AbstractQueryReply reply : supportedReplies) {
            if (requestedQcodes.contains(reply.getID())) {
                replies.add(reply);
            }
        }
        if (replies.isEmpty()) {
            replies.add(new QueryReplyNull());
        }
        return replies;
    }

    private List<AbstractQueryReply> getAllSupportedReplies() {
        ArrayList<AbstractQueryReply> replies = new ArrayList<>();

        replies.add(new QueryReplyUsableArea(this));
        replies.add(new QueryReplyImplicitPartition(this));
        replies.add(new QueryReplyCharactersets());
        replies.add(new QueryReplyColor());
        replies.add(new QueryReplyHighlite());
        return replies;
    }

    private void sendQueryReplies(QueryReplySummary summary, List<AbstractQueryReply> replies) throws DatastreamException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(AttentionIdentification.STRUCTURED_FIELD.getKeyValue());
            baos.write(summary.toByte());

            for (AbstractQueryReply reply : replies) {
                baos.write(reply.toByte());
            }

            if (logger.isTraceEnabled() || !this.datastreamListeners.isEmpty()) {
                String hex = new String(Hex.encodeHex(baos.toByteArray()));
                if (logger.isTraceEnabled()) {
                    logger.trace("outbound sf=" + hex);
                }

                for(IDatastreamListener listener : this.datastreamListeners) {
                    listener.datastreamUpdate(DatastreamDirection.OUTBOUND, hex);
                }
            }

            network.sendDatastream(baos.toByteArray());
        } catch (Exception e) {
            throw new DatastreamException("Unable able to write Query Reply", e);
        }
    }

    public synchronized void processOrders(List<AbstractOrder> orders, WriteControlCharacter writeControlCharacter) throws DatastreamException {
        logger.trace("Processing orders");
        for (AbstractOrder order : orders) {
            if (order instanceof OrderSetBufferAddress) {
                processSBA((OrderSetBufferAddress) order);
            } else if (order instanceof OrderRepeatToAddress) {
                processRA((OrderRepeatToAddress) order);
            } else if (order instanceof OrderText) {
                processText((OrderText) order);
            } else if (order instanceof OrderStartField) {
                processSF((OrderStartField) order);
            } else if (order instanceof OrderStartFieldExtended) {
                processSFE((OrderStartFieldExtended) order);
            } else if (order instanceof OrderSetAttribute) {
                processSA((OrderSetAttribute) order);
            } else if (order instanceof OrderModifyField) {
                processMF((OrderModifyField) order);
            } else if (order instanceof OrderInsertCursor) {
                this.screenCursor = this.workingCursor;
            } else if (order instanceof OrderEraseUnprotectedToAddress) {
                processEUA((OrderEraseUnprotectedToAddress)order);
            } else if (order instanceof OrderNewLine) {
                processNewLine();
            } else if (order instanceof OrderFormFeed) {
                processFormFeed();
            } else if (order instanceof OrderCarrageReturn) {
                processCarrageReturn();
            } else if (order instanceof OrderEndOfMedium) {
                processEndOfMedium();
            } else if (order instanceof OrderGraphicsEscape) {
                processGraphicsEscape((OrderGraphicsEscape) order);
            } else {
                throw new DatastreamException("Unsupported Order - " + order.getClass().getName());
            }
        }


        if (writeControlCharacter.isKeyboardReset()) {
            this.lastAid = AttentionIdentification.NONE;
            unlockKeyboard();
            this.workingCursor = 0;
        }

        synchronized (updateListeners) {
            for (IScreenUpdateListener listener : updateListeners) {
                listener.screenUpdated(Direction.RECEIVED, null);
            }
        }
    }

    public synchronized void erase() {

        if (this.usingAlternate || this.buffer == null) {
            this.columns = primaryColumns;
            this.rows    = primaryRows;
            allocateBuffer();

            this.usingAlternate = false;
        }

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = null;
        }

        this.screenCursor  = 0;
        this.workingCursor = 0;
        this.initialCursorPosition = 0;
    }

    public synchronized void eraseAlternate() {
        if (!hasAlternate) {
            erase();
            return;
        }


        if (!this.usingAlternate || this.buffer == null) {
            this.columns = alternateColumns;
            this.rows    = alternateRows;
            allocateBuffer();

            this.usingAlternate = true;
        }

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = null;
        }

        this.screenCursor  = 0;
        this.workingCursor = 0;
    }

    private void allocateBuffer() {
        this.screenSize = this.columns * this.rows;
        this.buffer = new IBufferHolder[this.screenSize];        
    }

    /**
     * Process a Set Buffer Address order
     * 
     * @param order - the order to process
     */
    private synchronized void processSBA(OrderSetBufferAddress order) {
        this.workingCursor = order.getBufferAddress();
        if (this.workingCursor >= this.screenSize) {
            this.workingCursor = this.workingCursor - this.screenSize;
        }
    }

    /**
     * Process the Report to Address order
     * 
     * @param order - the order to process
     * @throws DatastreamException
     */
    private synchronized void processRA(OrderRepeatToAddress order) throws DatastreamException {
        int endOfRepeat = order.getBufferAddress();

        if (endOfRepeat > this.screenSize || endOfRepeat < 0) {
            throw new DatastreamException(
                    "Impossible RA end address " + endOfRepeat + ", screen size is " + screenSize);
        }

        boolean firstPosition = true;
        while (firstPosition || this.workingCursor != endOfRepeat) {
            this.buffer[this.workingCursor] = new BufferChar(order.getChar());
            if (endOfRepeat == this.screenSize && this.workingCursor == (this.screenSize - 1)) {
                endOfRepeat = 0;
                break;
            }
            firstPosition = false;
            incrementWorkingCursor();
        }

        this.workingCursor = endOfRepeat;
    }

    private void incrementWorkingCursor() {
        this.workingCursor++;
        if (this.workingCursor >= this.screenSize) {
            this.workingCursor = 0;
        }
    }

    private void processSF(OrderStartField order) {
        this.buffer[this.workingCursor] = new BufferStartOfField(this.workingCursor, order.isFieldProtected(),
                order.isFieldNumeric(), order.isFieldDisplay(), order.isFieldIntenseDisplay(),
                order.isFieldSelectorPen(), order.isFieldModifed());
        incrementWorkingCursor();
    }

    private void processSFE(OrderStartFieldExtended order) {
        OrderStartField sf = order.getOrderStartField();
        BufferStartOfField bsf = null;

        if (sf != null) {
            bsf = new BufferStartOfField(this.workingCursor, sf.isFieldProtected(), sf.isFieldNumeric(),
                    sf.isFieldDisplay(), sf.isFieldIntenseDisplay(), sf.isFieldSelectorPen(), sf.isFieldModifed(),
                    order.getHighlight(), order.getForegroundColour(), order.getBackgroundColor());
        }

        if (bsf == null) {
            bsf = new BufferStartOfField(this.workingCursor, false, false, true, false, false, false);
        }

        this.buffer[this.workingCursor] = bsf;
        incrementWorkingCursor();
    }

    private void processMF(OrderModifyField order) {
        OrderStartField sf = order.getOrderStartField();
        BufferStartOfField bsf = null;

        if (sf != null) {
            bsf = new BufferStartOfField(this.workingCursor, sf.isFieldProtected(), sf.isFieldNumeric(),
                    sf.isFieldDisplay(), sf.isFieldIntenseDisplay(), sf.isFieldSelectorPen(), sf.isFieldModifed(),
                    order.getHighlight(), order.getForegroundColour(), order.getBackgroundColor());
        }

        if (bsf == null) {
            bsf = new BufferStartOfField(this.workingCursor, false, false, true, false, false, false);
        }

        this.buffer[this.workingCursor] = bsf;
        incrementWorkingCursor();
    }

    private void processEUA(OrderEraseUnprotectedToAddress order) {
        boolean charProtected = true;
        IBufferHolder bh = this.buffer[this.workingCursor];
        // are we on a SF, if so take the protected setting
        if (bh instanceof BufferStartOfField) {
            charProtected = ((BufferStartOfField)bh).isProtected();
        } else {
            // we have to go looking backwards for it
            int searchCursor = this.workingCursor - 1;
            if (searchCursor < 0) {
                searchCursor = this.screenSize - 1;
            }
            boolean found = false;
            while(searchCursor != this.workingCursor) {
                bh = this.buffer[searchCursor];

                if (bh instanceof BufferStartOfField) {
                    charProtected = ((BufferStartOfField)bh).isProtected();
                    found = true;
                    break;
                }

                searchCursor--;
                if (searchCursor < 0) {
                    searchCursor = this.screenSize - 1;
                }
            }

            if (!found) {
                // assume no fields, so unprotected;
                charProtected = false;
            }
        }


        int toAddress = order.getBufferAddress();
        if (toAddress >= this.screenSize) {
            toAddress = this.screenSize - 1;
        }
        if (toAddress < 0) {
            toAddress = 0;
        }

        int eraseCursor = this.workingCursor;
        while(true) {
            bh = this.buffer[eraseCursor];
            if (bh instanceof BufferStartOfField) {
                charProtected = ((BufferStartOfField)bh).isProtected();
            } else {
                if (!charProtected) {
                    this.buffer[eraseCursor] = null;
                }
            }

            eraseCursor++;
            if (eraseCursor >= this.screenSize) {
                eraseCursor = 0;
            }

            if (eraseCursor == toAddress) {
                break;
            }
        }

    }



    private void processSA(OrderSetAttribute order) {
        if (!detectedSetAttribute) {
            detectedSetAttribute = true;
            
            logger.warn("SetAttribute order has been received, please send a trace to the Galasa team");
        }
    }

    private void processNewLine() {
        this.buffer[this.workingCursor] = new BufferNewLine();
        incrementWorkingCursor();
    }

    private void processFormFeed() {
        this.buffer[this.workingCursor] = new BufferFormFeed();
        incrementWorkingCursor();
    }

    private void processCarrageReturn() {
        this.buffer[this.workingCursor] = new BufferCarrageReturn();
        incrementWorkingCursor();
    }

    private void processEndOfMedium() {
        this.buffer[this.workingCursor] = new BufferEndOfMedium();
        incrementWorkingCursor();
    }

    private void processGraphicsEscape(OrderGraphicsEscape order) {
        this.buffer[this.workingCursor] = new BufferGraphicsEscape(order.getByte());
        incrementWorkingCursor();
    }

    private void processText(OrderText order) {
        String text = order.getText();
        for (int i = 0; i < text.length(); i++) {
            this.buffer[this.workingCursor] = new BufferChar(text.charAt(i));
            incrementWorkingCursor();
        }

    }

    private String buildRawScreenStringFromBuffer() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.buffer.length; i++) {
            IBufferHolder currentBufferHolder = this.buffer[i];
            String currentBufferStr = null;
            if (currentBufferHolder == null) {
                currentBufferStr = " ";
            } else {
                currentBufferStr = currentBufferHolder.getStringWithoutNulls();
            }

            sb.append(currentBufferStr);
        }

        return sb.toString();
    }

    public String printScreen() {
        StringBuilder screenSB = new StringBuilder();
        String screenString = buildRawScreenStringFromBuffer();
        for (int i = 0; i < this.screenSize; i += this.columns) {
            screenSB.append(screenString.substring(i, i + this.columns));
            screenSB.append('\n');
        }
        return screenSB.toString();
    }

    public String printScreenTextWithCursor() {
        int cursorRow = screenCursor / columns;
        int cursorCol = screenCursor % columns;

        StringBuilder screenSB = new StringBuilder();
        String screenString = buildRawScreenStringFromBuffer();
        int row = 0;
        for (int i = 0; i < this.screenSize; i += this.columns) {
            screenSB.append("=|");
            screenSB.append(screenString.substring(i, i + this.columns));
            screenSB.append("|");
            screenSB.append('\n');
            if (row == cursorRow) {
                screenSB.append("^|");
                for (int j = 0; j < cursorCol; j++) {
                    screenSB.append(" ");
                }
                screenSB.append("^");
                screenSB.append('\n');
            }

            row++;
        }


        screenSB.append("!| ");
        screenSB.append(reportOperator());
        screenSB.append("\n");


        return screenSB.toString();
    }

    public String printExtendedScreen(boolean printCursor, boolean printColour, boolean printHighlight, boolean printIntensity, boolean printProtected, boolean printNumeric, boolean printModified) throws Zos3270Exception {
        int cursorRow = screenCursor / columns;
        int cursorCol = screenCursor % columns;

        StringBuilder screenBuffer   = new StringBuilder();
        StringBuilder intensityLine  = new StringBuilder();
        StringBuilder protectedLine  = new StringBuilder();
        StringBuilder numericLine    = new StringBuilder();
        StringBuilder modifiedLine   = new StringBuilder();
        StringBuilder foregroundLine = new StringBuilder(); 
        StringBuilder backgroundLine = new StringBuilder();
        StringBuilder highlightLine  = new StringBuilder();

        int row = 0;
        int col = 0;

        // *** Check to see if the screen is wrapped or unformatted
        BufferStartOfField currentBufferStartOfField = new BufferStartOfField(0, false, false, true, false, false, false);
        if (!(this.buffer[0] instanceof BufferStartOfField)) {
            for (int i = this.buffer.length - 1; i >= 0; i--) {
                IBufferHolder bh = this.buffer[i];
                if (bh instanceof BufferStartOfField) {
                    currentBufferStartOfField = (BufferStartOfField) bh;
                    break;
                }
            }
        }  // no need for else as it will be picked up in the loop

        for (int i = 0; i < this.buffer.length; i++) {
            // print row header
            if (col == 0) {
                screenBuffer.append("=");
                screenBuffer.append(String.format("%03d", row+1));
                screenBuffer.append("|");
            }

            // Print actual text
            IBufferHolder bufferHolder = this.buffer[i];
            if (bufferHolder == null) {
                screenBuffer.append(" ");
            } else {
                screenBuffer.append(bufferHolder.getStringWithoutNulls());

                if (bufferHolder instanceof BufferStartOfField) {
                    currentBufferStartOfField = (BufferStartOfField)bufferHolder;
                }
            }


            if (bufferHolder == null || bufferHolder == currentBufferStartOfField) {
                foregroundLine.append(" ");
                backgroundLine.append(" ");
                highlightLine.append(" ");
                intensityLine.append(" ");
                protectedLine.append(" ");
                numericLine.append(" ");
                modifiedLine.append(" ");
            } else {
                AttributeForegroundColour foregroundColour = currentBufferStartOfField.getAttributeForegroundColour();
                if (foregroundColour == null) {
                    foregroundLine.append(" ");
                } else {
                    foregroundLine.append(foregroundColour.getColour().getLetter());
                }
                
                AttributeBackgroundColour backgroundColour = currentBufferStartOfField.getAttributeBackgroundColour();
                if (backgroundColour == null) {
                    backgroundLine.append(" ");
                } else {
                    backgroundLine.append(backgroundColour.getColour().getLetter());
                }
                




                // Calculate Highlight
                AttributeExtendedHighlighting extendedHighlighting = currentBufferStartOfField.getAttributeExtendedHighlighting();
                if (extendedHighlighting == null) {
                    highlightLine.append(" ");
                } else {
                    switch(extendedHighlighting.getHighlight()) {
                    case BLINK:
                        highlightLine.append("b");
                        break;
                    case NORMAL:
                        highlightLine.append("n");
                        break;
                    case REVERSE:
                        highlightLine.append("r");
                        break;
                    case UNDERSCORE:
                        highlightLine.append("u");
                        break;
                    case DEFAULT:
                        highlightLine.append("d");
                        break;
                    default:
                        highlightLine.append("?");
                        break;
                    }
                }

                // Calculate intensity
                if (currentBufferStartOfField.isIntenseDisplay()) {
                    intensityLine.append("i");
                } else {
                    intensityLine.append(" ");
                }

                // Calculate Protected
                if (currentBufferStartOfField.isProtected()) {
                    protectedLine.append("p");
                } else {
                    protectedLine.append("u");
                }

                // Calculate Numeric
                if (currentBufferStartOfField.isNumeric()) {
                    numericLine.append("n");
                } else {
                    numericLine.append(" ");
                }

                // Calculate Modified
                if (currentBufferStartOfField.isFieldModifed()) {
                    modifiedLine.append("m");
                } else {
                    modifiedLine.append(" ");
                }
            }

            // NOT doing selectable as very unlikely to be used


            col++;
            if (col >= columns) {
                screenBuffer.append("|\n");

                // Check this is the cursor row
                if (printCursor) {
                    if (row == cursorRow) {
                        screenBuffer.append("^   |");
                        for (int j = 0; j < cursorCol; j++) {
                            screenBuffer.append(" ");
                        }
                        screenBuffer.append("^");
                        screenBuffer.append('\n');
                    }
                }
                // if requested, print colour
                if (printColour) {
                    screenBuffer.append("f   |");
                    screenBuffer.append(foregroundLine.toString());
                    screenBuffer.append('\n');
                    screenBuffer.append("b   |");
                    screenBuffer.append(backgroundLine.toString());
                    screenBuffer.append('\n');
                }
                // if requested, print intensity
                if (printIntensity) {
                    screenBuffer.append("i   |");
                    screenBuffer.append(intensityLine.toString());
                    screenBuffer.append('\n');
                }
                // if requested, print highlight
                if (printHighlight) {
                    screenBuffer.append("h   |");
                    screenBuffer.append(highlightLine.toString());
                    screenBuffer.append('\n');
                }
                // if requested, print protected
                if (printProtected) {
                    screenBuffer.append("p   |");
                    screenBuffer.append(protectedLine.toString());
                    screenBuffer.append('\n');
                }
                // if requested, print numeric
                if (printNumeric) {
                    screenBuffer.append("n   |");
                    screenBuffer.append(numericLine.toString());
                    screenBuffer.append('\n');
                }
                // if requested, print modifles
                if (printModified) {
                    screenBuffer.append("m   |");
                    screenBuffer.append(modifiedLine.toString());
                    screenBuffer.append('\n');
                }


                // Reset for next row
                col = 0;
                row++;

                // Reset the report lines
                intensityLine  = new StringBuilder();
                protectedLine  = new StringBuilder();
                numericLine    = new StringBuilder();
                modifiedLine   = new StringBuilder();
                foregroundLine = new StringBuilder();
                backgroundLine = new StringBuilder();
                highlightLine  = new StringBuilder();
            }
        }

        screenBuffer.append("!   | ");
        screenBuffer.append(reportOperator());
        screenBuffer.append("\n");

        return screenBuffer.toString();
    }


    private String reportOperator() {
        int cursorRow = (screenCursor / columns) + 1;
        int cursorCol = screenCursor % columns;

        StringBuilder operator = new StringBuilder();

        if (network.isConnected()) {
            operator.append("Connected-");
        } else {
            operator.append("Disconnected-");
        }
        if (network.isTls() || network.isSwitchedSSL()) {
            operator.append("SSL ");
        } else {
            operator.append("Plain");
        }
        operator.append(" Size=" + this.rows + "x" + this.columns);
        operator.append(" Cursor=" + screenCursor + "," + cursorRow + "x" + cursorCol);

        if (this.keyboardLockSet) {
            operator.append(" Keyboard Locked");
        } else {
            operator.append(" Keyboard Unlocked");
        }

        return operator.toString();
    }

    public String retrieveFlatScreen() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.buffer.length; i++) {
            if (this.buffer[i] == null) {
                sb.append(" ");
            } else {
                sb.append(this.buffer[i].getStringWithoutNulls());
            }
        }
        return sb.toString();
    }



    public synchronized @NotNull Field[] calculateFields() {
        ArrayList<Field> fields = new ArrayList<>();

        Field currentField = null;

        // *** Check to see if the screen is wrapped or unformatted
        if (!(this.buffer[0] instanceof BufferStartOfField)) {
            BufferStartOfField wrapSoField = null;
            for (int i = this.buffer.length - 1; i >= 0; i--) {
                IBufferHolder bh = this.buffer[i];
                if (bh instanceof BufferStartOfField) {
                    wrapSoField = (BufferStartOfField) bh;
                    break;
                }
            }

            if (wrapSoField == null) {
                currentField = new Field();
            } else {
                currentField = new Field(-1, wrapSoField);
            }
        }

        for (int i = 0; i < this.buffer.length; i++) {
            IBufferHolder bh = this.buffer[i];
            if (bh == null) {
                currentField.appendChar((char) 0x00); // NOSONAR, can't be null
            } else if (bh instanceof BufferStartOfField) {
                if (currentField != null) {
                    fields.add(currentField);
                }
                currentField = new Field(i, (BufferStartOfField) bh);
            } else if (bh instanceof BufferChar) {
                currentField.appendChar(((BufferChar) bh).getChar());// NOSONAR, can't be null
            } else {
                throw new UnsupportedOperationException("Unrecognised buffer type " + bh.getClass().getName());
            }
        }
        if (currentField != null) {
            fields.add(currentField);
        }

        // *** If the SBA were not in order, possibility that the safeguard first field
        // was left there
        if (fields.size() >= 2) {
            if (fields.get(0).getStart() == 0 && fields.get(1).getStart() == 0) {
                fields.remove(0);
            }
        }

        return fields.toArray(new Field[fields.size()]);
    }

    public void searchFieldContaining(String text) throws TextNotFoundException {
        for (Field field : calculateFields()) {
            if (field.containsText(text)) {
                return;
            }
        }

        throw new TextNotFoundException(CANT_FIND_TEXT + text + "'");
    }

    public int searchFieldContaining(@NotNull String[] okText, String[] errorText) throws TextNotFoundException, ErrorTextFoundException {
        if (errorText != null) {
            for(int i = 0; i < errorText.length; i++) {
                for (Field field : calculateFields()) {
                    if (field.containsText(errorText[i])) {
                        throw new ErrorTextFoundException("Found error text '" + errorText[i] + "' on screen", i);
                    }
                }
            }
        }

        for(int i = 0; i < okText.length; i++) {
            for (Field field : calculateFields()) {
                if (field.containsText(okText[i])) {
                    return i;
                }
            }
        }

        throw new TextNotFoundException("Unable to locate text on sreen");
    }

    public boolean isTextInField(String text) {
        for (Field field : calculateFields()) {
            if (field.containsText(text)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Wait on the keyboard being free
     * 
     * @param maxWait - time in milliseconds
     * @throws KeyboardLockedException
     * @throws InterruptedException
     */
    public void waitForKeyboard(int maxWait) throws TimeoutException, TerminalInterruptedException {
        try {
            if (!keyboardLock.tryAcquire(maxWait, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("Wait for keyboard took longer than " + maxWait + "ms");
            }
        } catch(InterruptedException e) {
            throw new TerminalInterruptedException("Wait for keyboard was interrupted", e);
        }
        keyboardLock.release();
    }

    public int waitForTextInField(String text, long maxWait) throws TerminalInterruptedException, TextNotFoundException, Zos3270Exception {
        return waitForTextInField(new String[] {text}, null, maxWait);
    }

    public int waitForTextInField(String[] ok, String[] error, long timeoutInMilliseconds) throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, Zos3270Exception {
        int foundIndex = -1;
        try {
            foundIndex = ScreenUpdateTextListener.waitForText(this, ok, error, timeoutInMilliseconds);
            if (foundIndex < 0) {
                if (ok != null && ok.length == 1 && error == null) {
                    throw new TextNotFoundException(CANT_FIND_TEXT + ok[0] + "'");
                }
                throw new TextNotFoundException("Unable to find a field containing any of the request text");
            }
        } catch(InterruptedException e) {
            throw new TerminalInterruptedException("Wait for text was interrupted", e);
        }
        return foundIndex;
    }



    public synchronized void positionCursorToFieldContaining(@NotNull String text)
            throws KeyboardLockedException, TextNotFoundException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        for (Field field : calculateFields()) {
            if (field.containsText(text)) {
                this.screenCursor = field.getStart();
                return;
            }
        }

        throw new TextNotFoundException(CANT_FIND_TEXT + text + "'");
    }

    public synchronized void eraseEof() throws KeyboardLockedException, FieldNotFoundException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        if (buffer[screenCursor] != null && !(buffer[screenCursor] instanceof BufferChar)) {
            throw new FieldNotFoundException("Unable to type where the cursor is pointing to - " + this.screenCursor);
        }

        BufferStartOfField sf = null;
        int sfPos = screenCursor - 1;
        if (sfPos < 0) {
            sfPos = buffer.length - 1;
        }
        while(sfPos != screenCursor) {
            if (buffer[sfPos] instanceof BufferStartOfField) {
                sf = (BufferStartOfField) buffer[sfPos];
                break;
            }

            sfPos--;
            if (sfPos < 0) {
                sfPos = buffer.length - 1;
            }
        }

        // *** if no field found, assume unprotected
        if (sf != null && sf.isProtected()) {
            throw new FieldNotFoundException("Unable to type where the cursor is pointing to - " + screenCursor);
        }

        //*** Set this and following characters to null
        int pos = this.screenCursor;
        while(true) {
            if (!(buffer[pos] instanceof BufferChar)) {
                break;
            }

            buffer[pos] = new BufferChar((char) 0);
            pos++;
            if (pos >= this.screenSize) {
                pos = 0;
            }

            if (pos == this.screenCursor) {
                break;
            }
        }

        if (sf != null) {
            sf.setFieldModified();
        }
    }


    public synchronized void eraseInput() throws KeyboardLockedException, FieldNotFoundException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to erase input as keyboard is locked");
        }

        boolean unprotected = false;
        BufferStartOfField startOfFieldUnprotected = null;

        // *** Check to see if the screen is wrapped or unformatted
        if (!(this.buffer[0] instanceof BufferStartOfField)) {
            BufferStartOfField wrapSoField = null;
            for (int i = this.buffer.length - 1; i >= 0; i--) {
                IBufferHolder bh = this.buffer[i];
                if (bh instanceof BufferStartOfField) {
                    wrapSoField = (BufferStartOfField) bh;
                    break;
                }
            }

            if (wrapSoField == null) {
                unprotected = true;  // unformatted, screen, so all unprotected
            } else {
                unprotected = !wrapSoField.isProtected();
                startOfFieldUnprotected = wrapSoField;
            }
        }



        for(int i = 0; i < this.screenSize; i++) {
            IBufferHolder bh = this.buffer[i];

            if (bh instanceof BufferStartOfField) {
                BufferStartOfField sof = (BufferStartOfField) bh;
                unprotected = !sof.isProtected();
                if (unprotected) {
                    startOfFieldUnprotected = sof;
                } else {
                    startOfFieldUnprotected = null;
                }
            } else {
                if (unprotected) {
                    this.buffer[i] = null;
                    if (startOfFieldUnprotected != null) {
                        startOfFieldUnprotected.setFieldModified();
                    }
                }
            }
        }

    }


    public synchronized void tab() throws KeyboardLockedException, FieldNotFoundException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        int startPosition = this.screenCursor;
        boolean foundUnprotectedField = false;

        IBufferHolder sfCheck = this.buffer[this.screenCursor];    
        if (sfCheck instanceof BufferStartOfField) {
            foundUnprotectedField = !((BufferStartOfField)sfCheck).isProtected();
        }
        while(true) {
            // advance the cursor
            this.screenCursor++;
            if (this.screenCursor >= this.screenSize) {
                this.screenCursor = 0;
            }

            // Get the entry at this position
            IBufferHolder previousBuffer = this.buffer[this.screenCursor];
            if (previousBuffer == null || previousBuffer instanceof BufferChar) {
                // if this is a character and we are in an unprotected field, use it
                if (foundUnprotectedField) {
                    return;
                }
            } else  if (previousBuffer instanceof BufferStartOfField) {
                // we have a start of field
                BufferStartOfField sof = (BufferStartOfField) previousBuffer;
                // record if it is unprotected or not
                foundUnprotectedField = !sof.isProtected();
            } else {
                throw new FieldNotFoundException("Unrecognised buffer type at pos " + this.screenCursor);
            }

            if (this.screenCursor == startPosition) {
                // we have completely wrapped, so no unprotected chars position to zero
                this.screenCursor = 0;
                return;
            }
        }

    }

    public synchronized void backTab() throws KeyboardLockedException, FieldNotFoundException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        int startPosition = this.screenCursor;
        int lastCharField = -1;
        boolean foundUnprotectedField = false;
        while(true) {
            // Get the previous position in buffer, wrapped if necessary
            int previousPositionInBuffer = this.screenCursor - 1;
            if (previousPositionInBuffer < 0) {
                previousPositionInBuffer = this.screenSize - 1;
            }

            // Get the entry in the previous position
            IBufferHolder previousBuffer = this.buffer[previousPositionInBuffer];
            if (previousBuffer == null || previousBuffer instanceof BufferChar) {
                // if it is null or a character, mark position as the last valid position whether unprotected or not
                lastCharField = previousPositionInBuffer;
            } else  if (previousBuffer instanceof BufferStartOfField) {
                // we have a start of field
                BufferStartOfField sof = (BufferStartOfField) previousBuffer;
                //if it is protected, invalidate the last valid char position
                if (sof.isProtected()) {
                    lastCharField = -1;
                } else {
                    // as unprotected field,  indicate that there is atleast one on the screen
                    foundUnprotectedField = true;
                    // if we have found a valid char position then use it
                    if (lastCharField != -1) {
                        this.screenCursor = lastCharField;
                        return;
                    }
                }
            } else {
                throw new FieldNotFoundException("Unrecognised buffer type at pos " + previousPositionInBuffer);
            }

            this.screenCursor = previousPositionInBuffer;
            if (this.screenCursor == startPosition) {
                // we have completely wrapped, either was original at the only unprotected field
                // or there was no unprotected fields, so move to origin.
                if (!foundUnprotectedField) {
                    this.screenCursor = 0;
                }
                return;
            }
        }

    }

    public synchronized void cursorUp() throws KeyboardLockedException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        this.screenCursor = this.screenCursor - this.columns;
        if (this.screenCursor < 0) {
            this.screenCursor = this.screenSize +this.screenCursor;
        }
    }

    public synchronized void cursorDown() throws KeyboardLockedException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        this.screenCursor = this.screenCursor + this.columns;
        if (this.screenCursor >= this.screenSize) {
            this.screenCursor = this.screenCursor - this.screenSize;
        }
    }

    public synchronized void cursorLeft() throws KeyboardLockedException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        this.screenCursor--;
        if (this.screenCursor < 0) {
            this.screenCursor = this.screenSize - this.screenCursor;
        }
    }

    public synchronized void cursorRight() throws KeyboardLockedException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        this.screenCursor++;
        if (this.screenCursor >= this.screenSize) {
            this.screenCursor = this.screenCursor - this.screenSize;
        }

    }

    public synchronized void home() throws KeyboardLockedException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        Field[] fields = calculateFields();

        if (fields == null || fields.length == 0) {
            this.screenCursor = 0;
            return;
        }

        //*** find first unprotected field
        for(Field field : fields) {
            if (!field.isProtected() && field.length() > 1) {
                if (field.isDummyField()) {
                    this.screenCursor = 0;
                } else {
                    this.screenCursor = field.getStart() + 1;
                }
                return;
            }
        }

        this.screenCursor = 0;
        return;
    }


    public synchronized void newLine() throws KeyboardLockedException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        Field[] fields = calculateFields();


        int newCursor = ((this.screenCursor / this.columns) + 1) * this.columns;
        if (newCursor >= this.screenSize) {
            newCursor = 0;
        }

        if (fields == null || fields.length == 0) {
            this.screenCursor = newCursor;
            return;
        }

        // locate the field that contains the new position
        int fieldPos = 0;
        Field startField = null;
        for(; fieldPos < fields.length; fieldPos++) {
            if (fields[fieldPos].containsPosition(newCursor)) {
                startField = fields[fieldPos];
                break;
            }
        }

        if (!startField.isProtected() && startField.length() > 1) {
            if (newCursor == startField.getStart() && !startField.isDummyField()) {
                newCursor++;
            }
            this.screenCursor = newCursor;
            return;
        }

        // This field is protected, so locate the next unprotected field
        while(true) {
            fieldPos++;
            if (fieldPos >= fields.length) {
                fieldPos = 0;
            }

            Field nextField = fields[fieldPos];

            if (!nextField.isProtected() && nextField.length() > 1) {
                if (nextField.isDummyField()) {
                    this.screenCursor = nextField.getStart();
                } else {
                    this.screenCursor = nextField.getStart() + 1;
                }
                return;
            }

            if (nextField == startField) {
                break;
            }
        }

        this.screenCursor = newCursor;
        return;
    }

    public void backSpace() throws KeyboardLockedException, FieldNotFoundException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to type as keyboard is locked");
        }

        int position = this.screenCursor;

        if (buffer[position] != null && !(buffer[position] instanceof BufferChar)) {
            throw new FieldNotFoundException("Unable to type where the cursor is pointing to - " + this.screenCursor);
        }

        BufferStartOfField sf = null;
        int sfPos = position - 1;
        if (sfPos < 0) {
            sfPos = buffer.length - 1;
        }
        while(sfPos != position) {
            if (buffer[sfPos] instanceof BufferStartOfField) {
                sf = (BufferStartOfField) buffer[sfPos];
                break;
            }

            sfPos--;
            if (sfPos < 0) {
                sfPos = buffer.length - 1;
            }
        }

        // *** if no field found, assume unprotected
        if (sf != null && sf.isProtected()) {
            throw new FieldNotFoundException("Unable to type where the cursor is pointing to - " + position);
        }

        if (position == 0) {
            return;  // NOOP, as dont go beyond the start or wrap or through an error
        }

        if (sfPos == (position - 1)) {
            throw new FieldNotFoundException("Unable to backspace where the cursor is pointing to - " + position + ", start of field");
        }

        while(true) {
            this.buffer[position - 1] = this.buffer[position];
            this.buffer[position] = null;

            position++;
            if (position >= this.screenSize) {
                break;
            }

            if (buffer[position] != null && !(buffer[position] instanceof BufferChar)) {
                break;
            }
        }

        this.screenCursor--;

    }


    public int getNoOfColumns() {
        return this.columns;
    }

    public int getNoOfRows() {
        return this.rows;
    }

    public synchronized void registerScreenUpdateListener(IScreenUpdateListener listener) {
        synchronized (updateListeners) {
            updateListeners.add(listener);
        }
    }

    public synchronized void unregisterScreenUpdateListener(IScreenUpdateListener listener) {
        synchronized (updateListeners) {
            updateListeners.remove(listener);
        }
    }

    public int getCursor() {
        return this.screenCursor;
    }

    public Field locateFieldAt(int cursorPos) {
        Field[] fields = calculateFields();

        int fieldPosition = 0;
        for (; fieldPosition < fields.length; fieldPosition++) {
            Field field = fields[fieldPosition];
            if (field.containsPosition(cursorPos)) {
                return field;
            }
        }
        return null;
    }

    public String getValueFromFieldContaining(String text) throws TextNotFoundException {
        Boolean foundHeader = false;
        for (Field field : calculateFields()) {
            if (!foundHeader) {
                if (field.containsText(text)) {
                    foundHeader = true;
                }
            } else {
                String foundText = field.getFieldWithoutNulls();
                if (foundText.length() > 0) {
                    return foundText;
                }
            }
        }

        throw new TextNotFoundException(CANT_FIND_TEXT + text + "'");
    }

    public synchronized void type(String text) throws KeyboardLockedException, FieldNotFoundException {
        this.screenCursor = type(text, this.screenCursor);
    }

    public synchronized int type(String text, int column, int row) throws KeyboardLockedException, FieldNotFoundException {
        int position = column + (row * columns);

        return type(text, position);
    }

    public synchronized int type(String text, int position) throws KeyboardLockedException, FieldNotFoundException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to type as keyboard is locked");
        }

        if (buffer[position] != null && !(buffer[position] instanceof BufferChar)) {
            throw new FieldNotFoundException("Unable to type where the cursor is pointing to - " + position);
        }

        BufferStartOfField sf = null;
        int sfPos = position - 1;
        if (sfPos < 0) {
            sfPos = buffer.length - 1;
        }
        while(sfPos != position) {
            if (buffer[sfPos] instanceof BufferStartOfField) {
                sf = (BufferStartOfField) buffer[sfPos];
                break;
            }

            sfPos--;
            if (sfPos < 0) {
                sfPos = buffer.length - 1;
            }
        }

        // *** if no field found, assume unprotected
        if (sf != null && sf.isProtected()) {
            throw new FieldNotFoundException("Unable to type where the cursor is pointing to - " + position);
        }

        if (text.length() == 0) {
            return position;
        }

        for (int i = 0; i < text.length(); i++) {
            IBufferHolder bh = buffer[position];
            if (bh != null && !(bh instanceof BufferChar)) {
                throw new FieldNotFoundException(
                        "Unable to type where the cursor is pointing to - " + position);
            }

            buffer[position] = new BufferChar(text.charAt(i));

            if (sf != null) {
                sf.setFieldModified();
            }

            // We have successfully typed a character, so make sure the cursor is positioned
            // at the next unprotected char, even if it is the position we were just at

            boolean unprotected = true;
            while(true) {
                position++;
                if (position >= screenSize) {
                    position = 0;
                }

                this.screenCursor = position;
                bh = buffer[position];

                if (unprotected && (bh == null || (bh instanceof BufferChar))) {
                    break;
                }

                if (bh != null && (bh instanceof BufferStartOfField)) {
                    BufferStartOfField sof = (BufferStartOfField) bh;
                    unprotected = !sof.isProtected();
                    if (unprotected) {
                        sf = sof;
                    }
                }
            }
        }

        return position;
    }

    private void buildSSCPLUOutboundBuffer(ByteArrayOutputStream outboundBuffer) {
        // The input area for SSCP-LU sessions starts from the initial cursor position and continues through the
        // following 255 screen positions or through to the last position on the screen (max 256 outbound bytes).
        // If the screen was cleared, the initial cursor position will be at very first position on the screen.
        int start = this.initialCursorPosition;
        int end = start + 255;
        if (end >= buffer.length) {
            end = buffer.length - 1;
        }

        for (int currentPosition = start; currentPosition <= end; currentPosition++) {
            IBufferHolder bufferHolder = buffer[currentPosition];
            if (bufferHolder instanceof BufferChar) {
                BufferChar bufferChar = (BufferChar) bufferHolder;
                byte ebcdicCharAsByte = bufferChar.getFieldEbcdic(this.codePage);
                if (ebcdicCharAsByte != 0) {
                    outboundBuffer.write(ebcdicCharAsByte);
                }
            }
        }
    }

    public synchronized byte[] aid(AttentionIdentification aid) throws DatastreamException, TerminalInterruptedException {
        lockKeyboard();

        try {
            ByteArrayOutputStream outboundBuffer = new ByteArrayOutputStream();

            outboundBuffer.write(aid.getKeyValue());


            if (aid == AttentionIdentification.CLEAR) {
                erase();
            } else if (aid == AttentionIdentification.PA1) {
            } else if (aid == AttentionIdentification.PA2) {
            } else if (aid == AttentionIdentification.PA3) {
            } else {
                BufferAddress cursor = new BufferAddress(this.screenCursor);
                outboundBuffer.write(cursor.getCharRepresentation());

                if (this.isSSCPLUDisplay) {
                    buildSSCPLUOutboundBuffer(outboundBuffer);
                } else {
                    readModifiedBuffer(outboundBuffer);
                }
            }

            String outboundHex = new String(Hex.encodeHex(outboundBuffer.toByteArray()));
            setDatastreamListenersDirection(outboundHex, DatastreamDirection.OUTBOUND);

            for (IScreenUpdateListener listener : updateListeners) {
                listener.screenUpdated(Direction.SENDING, aid);
            }

            this.lastAid = aid;

            return outboundBuffer.toByteArray();
        } catch (IOException e) {
            throw new DatastreamException("Unable to generate outbound datastream", e);
        }
    }

    public int getScreenSize() {
        return this.screenSize;
    }

    public String printFields() {
        Field[] fields = calculateFields();

        StringBuilder sb = new StringBuilder();
        for (Field field : fields) {
            sb.append(field.toString());
            sb.append("\n");
        }

        return sb.toString();
    }

    public void setBuffer(IBufferHolder[] newBuffer) {
        for (int i = 0; i < this.buffer.length && i < newBuffer.length; i++) {
            this.buffer[i] = newBuffer[i];
        }
    }

    public void setBuffer(int col, int row, String text) {
        int pos = (row * columns) + col;
        for (int i = 0; i < text.length(); i++) {
            buffer[pos] = new BufferChar(text.charAt(i));
            pos++;
        }
    }

    public void nullify(int col, int row, int len) {
        int pos = (row * columns) + col;
        for (int i = 0; i < len; i++) {
            buffer[pos] = null;
            pos++;
        }
    }

    public Field getFieldAt(int col, int row) {
        int pos = (row * columns) + col;

        Field[] fields = calculateFields();
        Field currentField = fields[0];
        for (int i = 1; i < fields.length; i++) {
            if (fields[i].getStart() > pos) {
                return currentField;
            }
            currentField = fields[i];
        }

        return currentField;
    }

    public void setCursorPosition(int newPosition) {
        this.screenCursor = newPosition;
    }

    public void setCursorPosition(int column, int row) {
        this.screenCursor = (row * this.columns) + column;
    }

    public synchronized void registerDatastreamListener(IDatastreamListener listener) {
        if (listener == null) {
            return;
        }

        if (!this.datastreamListeners.contains(listener)) {
            this.datastreamListeners.add(listener);
        }
    }

    public synchronized void unregisterDatastreamListener(IDatastreamListener listener) {
        this.datastreamListeners.remove(listener);
    }

    public List<IDatastreamListener> getDatastreamListeners() {
        return this.datastreamListeners;
    }

    public int getPrimaryColumns() {
        return this.primaryColumns;
    }

    public int getPrimaryRows() {
        return this.primaryRows;
    }

    public int getAlternateColumns() {
        return this.alternateColumns;
    }

    public int getAlternateRows() {
        return this.alternateRows;
    }

    public void testingSetLastAid(AttentionIdentification aid) {
        this.lastAid = aid;
    }

    public synchronized boolean isClearScreen() {
        for(IBufferHolder bh : buffer) {
            if (bh != null) {
                return false;
            }
        }

        return true;
    }

    public Colour getColourAtPosition(int pos) {
        
        Field[] fields = calculateFields();
        Field currentField = fields[0];
        for (int i = 1; i < fields.length; i++) {
            if (fields[i].getStart() > pos) {
                break;
            }
            currentField = fields[i];
        }
        
        return currentField.getForegroundColour();
    }

    public Highlight getHighlightAtPosition(int pos) {
        Field[] fields = calculateFields();
        Field currentField = fields[0];
        for (int i = 1; i < fields.length; i++) {
            if (fields[i].getStart() > pos) {
                break;
            }
            currentField = fields[i];
        }
        
        return currentField.getHighlight();
    }

    public Charset getCodePage() {
        return codePage;
    }

    public boolean isSSCPLUDisplay() {
        return isSSCPLUDisplay;
    }

    public void setIsSSCPLUDisplay(boolean isSSCPLUDisplay) {
        this.isSSCPLUDisplay = isSSCPLUDisplay;
    }
}
