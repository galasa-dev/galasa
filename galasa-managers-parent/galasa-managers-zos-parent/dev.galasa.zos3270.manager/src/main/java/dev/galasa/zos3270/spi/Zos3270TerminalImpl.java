/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.SetContentType;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.IScreenUpdateListener;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.common.screens.FieldContents;
import dev.galasa.zos3270.common.screens.TerminalField;
import dev.galasa.zos3270.common.screens.TerminalImage;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.properties.ApplyConfidentialTextFiltering;
import dev.galasa.zos3270.internal.properties.LiveTerminalUrl;
import dev.galasa.zos3270.internal.properties.LogConsoleTerminals;
import dev.galasa.zos3270.internal.properties.TerminalDeviceTypes;

public class Zos3270TerminalImpl extends Terminal implements IScreenUpdateListener {

    private Log logger = LogFactory.getLog(getClass());

    private final GalasaGson gson = new GalasaGson();

    private final String terminalId;
    private int updateId;
    private final String runId;

    private final IConfidentialTextService cts;
    private final boolean applyCtf;

    private final ArrayList<TerminalImage> cachedImages = new ArrayList<>();

    private Path storedArtifactsRoot;
    private final Path terminalRasDirectory;
    private int rasTerminalSequence;
    private URL liveTerminalUrl;
    private int liveTerminalSequence;
    private boolean logConsoleTerminals;
    private boolean autoConnect;

    /**
     * @deprecated use the {@link #Zos3270TerminalImpl(String id, String host, int port, boolean tls, IFramework framework, boolean autoConnect, IZosImage image, TerminalSize primarySize, TerminalSize alternateSize, ITextScannerManagerSpi textScanner)}
     * constructor instead.
     */
    @Deprecated(since = "0.28.0", forRemoval = true)
    public Zos3270TerminalImpl(String id, String host, int port, boolean tls, IFramework framework, boolean autoConnect,
            IZosImage image, ITextScannerManagerSpi textScanner)
            throws Zos3270ManagerException, TerminalInterruptedException {
        this(id, host, port, tls, framework, autoConnect, image, 80, 24, 0, 0, textScanner);
    }

    /**
     * @deprecated use the {@link #Zos3270TerminalImpl(String id, String host, int port, boolean tls, IFramework framework, boolean autoConnect, IZosImage image, TerminalSize primarySize, TerminalSize alternateSize, ITextScannerManagerSpi textScanner)}
     * constructor instead.
     */
    @Deprecated(since = "0.28.0", forRemoval = true)
    public Zos3270TerminalImpl(String id, String host, int port, boolean tls, IFramework framework, boolean autoConnect,
            IZosImage image,
            int primaryColumns, int primaryRows, int alternateColumns, int alternateRows,
            ITextScannerManagerSpi textScanner)
            throws Zos3270ManagerException, TerminalInterruptedException {
        super(id, host, port, tls, primaryColumns, primaryRows, alternateColumns, alternateRows, textScanner);
        this.terminalId = id;
        this.runId = framework.getTestRunName();
        this.autoConnect = autoConnect;
        this.cts = framework.getConfidentialTextService();
        this.applyCtf = ApplyConfidentialTextFiltering.get();
        this.textScan = textScanner;

        getScreen().registerScreenUpdateListener(this);

        storedArtifactsRoot = framework.getResultArchiveStore().getStoredArtifactsRoot();
        terminalRasDirectory = storedArtifactsRoot.resolve("zos3270").resolve("terminals").resolve(this.terminalId);
        URL propLiveTerminalUrl = LiveTerminalUrl.get();
        if (propLiveTerminalUrl == null) {
            liveTerminalUrl = null;
        } else {
            try {
                // *** Register the terminal to the UI which will own the terminal view
                HttpURLConnection connection = (HttpURLConnection) propLiveTerminalUrl.openConnection();
                connection.setRequestMethod("HEAD");
                connection.addRequestProperty("zos3270-runid", this.runId);
                connection.addRequestProperty("zos3270-terminalid", this.terminalId);
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();
                if (connection.getResponseCode() != 200) {
                    logger.warn("Unable to activate live terminal due to " + connection.getResponseCode() + " - "
                            + connection.getResponseMessage());
                } else {
                    this.liveTerminalUrl = new URL(
                            propLiveTerminalUrl.toString() + "/" + this.runId + "/" + this.terminalId);
                }
            } catch (Exception e) {
                throw new Zos3270ManagerException("Unable to create the live terminal directory", e);
            }
        }

        setDeviceTypes(TerminalDeviceTypes.get(image));

        logConsoleTerminals = LogConsoleTerminals.get();
    }


    public Zos3270TerminalImpl(String id, String host, int port, boolean tls, IFramework framework, boolean autoConnect,
            IZosImage image, TerminalSize primarySize, TerminalSize alternateSize, ITextScannerManagerSpi textScanner)
            throws Zos3270ManagerException, TerminalInterruptedException, ZosManagerException {
        super(id, host, port, tls, primarySize, alternateSize, textScanner, image.getCodePage());
        this.terminalId = id;
        this.runId = framework.getTestRunName();
        this.autoConnect = autoConnect;
        this.cts = framework.getConfidentialTextService();
        this.applyCtf = ApplyConfidentialTextFiltering.get();
        this.textScan = textScanner;

        getScreen().registerScreenUpdateListener(this);

        storedArtifactsRoot = framework.getResultArchiveStore().getStoredArtifactsRoot();
        terminalRasDirectory = storedArtifactsRoot.resolve("zos3270").resolve("terminals").resolve(this.terminalId);
        URL propLiveTerminalUrl = LiveTerminalUrl.get();
        if (propLiveTerminalUrl == null) {
            liveTerminalUrl = null;
        } else {
            try {
                // *** Register the terminal to the UI which will own the terminal view
                HttpURLConnection connection = (HttpURLConnection) propLiveTerminalUrl.openConnection();
                connection.setRequestMethod("HEAD");
                connection.addRequestProperty("zos3270-runid", this.runId);
                connection.addRequestProperty("zos3270-terminalid", this.terminalId);
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();
                if (connection.getResponseCode() != 200) {
                    logger.warn("Unable to activate live terminal due to " + connection.getResponseCode() + " - "
                            + connection.getResponseMessage());
                } else {
                    this.liveTerminalUrl = new URL(
                            propLiveTerminalUrl.toString() + "/" + this.runId + "/" + this.terminalId);
                }
            } catch (Exception e) {
                throw new Zos3270ManagerException("Unable to create the live terminal directory", e);
            }
        }

        setDeviceTypes(TerminalDeviceTypes.get(image));

        logConsoleTerminals = LogConsoleTerminals.get();
    }

    public boolean doAutoConnect() {
        return this.autoConnect;
    }

    @Override
    public synchronized void screenUpdated(Direction direction, AttentionIdentification aid) {
        updateId++;
        String update = terminalId + "-" + (updateId);

        String aidString;
        String aidText = null;
        if (aid != null) {
            aidString = ", " + aid.toString();
            aidText = aid.toString();
        } else {
            aidString = " update";
        }

        int cursorPosition = getScreen().getCursor();
        int screenCols = getScreen().getNoOfColumns();
        int screenRows = getScreen().getNoOfRows();

        int cursorRow = cursorPosition / screenRows;
        int cursorCol = cursorPosition % screenCols;

        TerminalSize terminalSize = new TerminalSize(screenCols, screenRows); // TODO
        // sort
        // out
        // alt
        // sizes
        TerminalImage terminalImage = new TerminalImage(updateId, update, direction == Direction.RECEIVED, null,
                aidText, terminalSize, cursorCol, cursorRow);
        terminalImage.getFields().addAll(buildTerminalFields(getScreen()));
        cachedImages.add(terminalImage);
        if (cachedImages.size() >= 10) {
            writeRasOutput();
            flushTerminalCache();
        }

        if (liveTerminalUrl != null) {
            try {
                liveTerminalSequence++;
                dev.galasa.zos3270.common.screens.Terminal liveTerminal = new dev.galasa.zos3270.common.screens.Terminal(
                        this.terminalId, this.runId, liveTerminalSequence, terminalSize);
                TerminalImage newTerminalImage = removeConfidentialTextFromTerminalImage(terminalImage);
                liveTerminal.getImages().add(newTerminalImage);

                JsonObject intermediateJson = (JsonObject) gson.toJsonTree(liveTerminal);
                stripFalseBooleans(intermediateJson);
                String tempJson = gson.toJson(intermediateJson);

                HttpURLConnection connection = (HttpURLConnection) this.liveTerminalUrl.openConnection();
                connection.setRequestMethod("PUT");
                connection.addRequestProperty("Content-Type", "application/json");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.connect();
                try (OutputStream os = connection.getOutputStream()) {
                    IOUtils.write(tempJson, os, StandardCharsets.UTF_8);
                }
                if (connection.getResponseCode() != 200) {
                    logger.warn("Unable to write live terminal due to " + connection.getResponseCode() + " - "
                            + connection.getResponseMessage());
                    this.liveTerminalUrl = null;
                }
            } catch (Exception e) {
                logger.error("Failed to write live terminal image, image lost", e);
                this.liveTerminalUrl = null;
            }
        }

        if (logConsoleTerminals) {
            String screenData = getScreen().printScreenTextWithCursor();
            if (applyCtf) {
                screenData = cts.removeConfidentialText(screenData);
            }
            logger.debug(direction.toString() + aidString + " to 3270 terminal " + this.terminalId + ",  updateId="
                    + update + "\n" + screenData);
        } else {
            logger.debug(direction.toString() + aidString + " to 3270 terminal " + this.terminalId + ",  updateId="
                    + update);
        }
    }

    public synchronized void writeRasOutput() {
        rasTerminalSequence++;

        try {
            writeTerminalGzJson();
        } catch (Exception e) {
            logger.error("Unable to write terminal cache to the RAS", e);
            rasTerminalSequence--;
            return;
        }
    }

    /**
     * This method creates JSON representations of the Terminal screens and writes them to the RAS
     * @throws IOException
     */
    private synchronized void writeTerminalGzJson() throws IOException {
        if (this.cachedImages.isEmpty()) {
            return;
        }

        TerminalSize terminalSize = new TerminalSize(getScreen().getNoOfColumns(), getScreen().getNoOfRows());
        dev.galasa.zos3270.common.screens.Terminal rasTerminal = new dev.galasa.zos3270.common.screens.Terminal(
                this.terminalId, this.runId, rasTerminalSequence, terminalSize);

        for (TerminalImage terminalImage : this.cachedImages){

            TerminalImage newTerminalImage = removeConfidentialTextFromTerminalImage(terminalImage);
            rasTerminal.getImages().add(newTerminalImage);
        }

        JsonObject intermediateJson = (JsonObject) gson.toJsonTree(rasTerminal);
        stripFalseBooleans(intermediateJson);
        String tempJson = gson.toJson(intermediateJson);

        if (applyCtf) {
            tempJson = cts.removeConfidentialText(tempJson);
        }

        String terminalFilename = this.terminalId + "-" + String.format("%05d", rasTerminalSequence) + ".gz";
        Path terminalPath = terminalRasDirectory.resolve(terminalFilename);

        try (GZIPOutputStream gos = new GZIPOutputStream(Files.newOutputStream(terminalPath,
                new SetContentType(new ResultArchiveStoreContentType("application/zos3270terminal")),
                StandardOpenOption.CREATE))) {
            IOUtils.write(tempJson, gos, "utf-8");
            gos.flush();
            gos.close();
        }
    }

    /**
     * Creates a copy of the original TerminalImage, iterates through it's TerminalFields and FieldContents,
     * and creates a new TerminalImage with confidential text removed.
     * @param terminalImage
     * @return
     */
    private TerminalImage removeConfidentialTextFromTerminalImage(TerminalImage terminalImage){
        // Create a new TerminalImage based on the one we are iterating on
        TerminalImage newTerminalImage = new TerminalImage(terminalImage.getSequence(), terminalImage.getId(),
        terminalImage.isInbound(), terminalImage.getType(), terminalImage.getAid(),terminalImage.getImageSize(),
        terminalImage.getCursorColumn(), terminalImage.getCursorRow());

        for (TerminalField terminalField : terminalImage.getFields()){

            // Create a new TerminalField based on the one we are iterating on
            TerminalField newTerminalField = new TerminalField(terminalField.getRow(), terminalField.getColumn(),
            terminalField.isUnformatted(), terminalField.isFieldProtected(), terminalField.isFieldNumeric(),
            terminalField.isFieldDisplay(), terminalField.isFieldIntenseDisplay(), terminalField.isFieldSelectorPen(),
            terminalField.isFieldModifed(), terminalField.getForegroundColour(), terminalField.getBackgroundColour(), terminalField.getHighlight());

            StringBuilder sb = new StringBuilder();
            for (FieldContents contents : terminalField.getContents()) {

                // Converting FieldContents to Strings and removing confidential text if required
                for (Character c : contents.getChars()) {
                    if (c == null) {
                        sb.append(" ");
                    } else {
                        sb.append(c);
                    }
                }
                String fieldText = applyCtf ? cts.removeConfidentialText(sb.toString()) : sb.toString();

                char[] fieldTextCharArray = fieldText.toCharArray();
                Character[] newArray = new Character[fieldTextCharArray.length];
                for (int i = 0; i < fieldTextCharArray.length; i++){
                    newArray[i] = Character.valueOf(fieldTextCharArray[i]);
                }

                // Create new FieldContents with the new Character[] with confidential text removed
                FieldContents newFieldContents = new FieldContents(newArray);
                newTerminalField.getContents().add(newFieldContents);
            }
            newTerminalImage.getFields().add(newTerminalField);
        }
        return newTerminalImage;
    }

    public synchronized void flushTerminalCache() {
        this.cachedImages.clear();
    }

    private static List<TerminalField> buildTerminalFields(Screen screen) {
        ArrayList<TerminalField> terminalFields = new ArrayList<>();

        Field[] screenFields = screen.calculateFields();
        for (Field screenField : screenFields) {
            int row = screenField.getStart() / screen.getNoOfColumns();
            int column = screenField.getStart() % screen.getNoOfColumns();

            Character cForegroundColour = null;
            Character cBackgroundColour = null;
            Character cHighlight = null;

            Colour foregroundColour = screenField.getForegroundColour();
            Colour backgroundColour = screenField.getBackgroundColour();
            Highlight highlight = screenField.getHighlight();

            if (foregroundColour != null) {
                cForegroundColour = foregroundColour.getLetter();
            }
            if (backgroundColour != null) {
                cBackgroundColour = backgroundColour.getLetter();
            }
            if (highlight != null) {
                cHighlight = highlight.getLetter();
            }

            TerminalField terminalField = new TerminalField(row, column, screenField.isUnformatted(),
                    screenField.isProtected(), screenField.isNumeric(), screenField.isDisplay(),
                    screenField.isIntenseDisplay(), screenField.isSelectorPen(), screenField.isFieldModifed(),
                    cForegroundColour, cBackgroundColour, cHighlight);

            Character[] chars = screenField.getFieldCharsWithNulls();
            terminalField.getContents().add(new FieldContents(chars)); // TODO, needs modifying when we know how to
                                                                       // support SetAttribute order
            terminalFields.add(terminalField);
        }

        return terminalFields;
    }

    public String getId() {
        return this.terminalId;
    }

    public static void stripFalseBooleans(JsonObject json) {

        ArrayList<Entry<String, JsonElement>> entries = new ArrayList<>();
        entries.addAll(json.entrySet());

        for (Entry<String, JsonElement> entry : entries) {
            JsonElement element = entry.getValue();

            if (element.isJsonPrimitive() && ((JsonPrimitive) element).isBoolean()
                    && !((JsonPrimitive) element).getAsBoolean()) {
                json.remove(entry.getKey());
            } else if (element.isJsonObject()) {
                stripFalseBooleans((JsonObject) element);
            } else if (element.isJsonArray()) {
                JsonArray array = (JsonArray) element;
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i).isJsonObject()) {
                        stripFalseBooleans((JsonObject) array.get(i));
                    }
                }
            }
        }
    }

}