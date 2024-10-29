/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;

import dev.galasa.textscan.ILogScanner;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosliberty.IZosLibertyServerLog;
import dev.galasa.zosliberty.IZosLibertyServerLogs;
import dev.galasa.zosliberty.ZosLibertyManagerException;
import dev.galasa.zosliberty.ZosLibertyServerException;

public class ZosLibertyServerLogsImpl implements IZosLibertyServerLogs {

    private IZosUNIXFile logsDirectory;
    private ZosLibertyManagerImpl zosLibertyManager;
    private HashMap<String, IZosLibertyServerLog> logs;
    private boolean hasFFDC;
    private Iterator<Entry<String, IZosLibertyServerLog>> logsIterator;
    private String currentLogName;
    
    public ZosLibertyServerLogsImpl(IZosUNIXFile logsDirectory, ZosLibertyManagerImpl zosLibertyManager) throws ZosLibertyServerException {
        this.logsDirectory = logsDirectory;
        this.zosLibertyManager = zosLibertyManager;
        this.logs = listServerLogsDirectory();
    }

    @Override
    public boolean hasFfdcs() {
        return this.hasFFDC;
    }

    @Override
    public int numberOfFiles() {
        return this.logs.size();
    }

    @Override
    public IZosLibertyServerLog getLog(String fileName) {
        return this.logs.get(fileName);
    }

    @Override
    public IZosLibertyServerLog getMessagesLog() {
        return getLog("messages.log");
    }

    @Override
    public IZosLibertyServerLog getTraceLog() {
        return getLog("trace.log");
    }

    @Override
    public IZosLibertyServerLog getNext() {
        if (this.logsIterator.hasNext()) {
            Entry<String, IZosLibertyServerLog> logsEntry = this.logsIterator.next();
            this.currentLogName = logsEntry.getKey();
            return getLog(this.currentLogName);
        }
        return null;
    }

    @Override
    public IZosLibertyServerLog getNextFfdc() {
        while (this.logsIterator.hasNext()) {
            Entry<String, IZosLibertyServerLog> logsEntry = this.logsIterator.next();
            this.currentLogName = logsEntry.getKey();
            if (isFfdc(logsEntry.getKey())) {
                return getLog(this.currentLogName);
            }
        }
        return null;
    }

    @Override
    public String getCurrentLogName() {
        return this.currentLogName;
    }

    @Override
    public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException {
        for (Entry<String, IZosLibertyServerLog> entry : listServerLogsDirectory().entrySet()) {
            String logsRasPath = rasPath + "/logs/";
            logsRasPath = logsRasPath + entry.getKey().substring(0, entry.getKey().length()-entry.getValue().getName().length());
            entry.getValue().saveToResultsArchive(logsRasPath);
        }
    }

    @Override
    public void refresh() throws ZosLibertyServerException {
        listServerLogsDirectory();
    }

    @Override
    public void checkpoint() throws ZosLibertyServerException {
        for (Entry<String, IZosLibertyServerLog> entry : listServerLogsDirectory().entrySet()) {
            entry.getValue().checkpoint();
        }
    }

    @Override
    public void delete() throws ZosLibertyServerException {
        try {
            this.logsDirectory.directoryDeleteNonEmpty();
            this.logsDirectory.create();
            refresh();
        } catch (ZosUNIXFileException | ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Problem deleting content content of logs directory", e);
        }
    }

    private HashMap<String, IZosLibertyServerLog> listServerLogsDirectory() throws ZosLibertyServerException {
        this.hasFFDC = false;
        this.logs = new HashMap<>();
        try {
            if (this.logsDirectory.exists()) {
                SortedMap<String, IZosUNIXFile> sortedMap = this.logsDirectory.directoryListRecursive();
                for (Entry<String, IZosUNIXFile> entry : sortedMap.entrySet()) {
                    IZosUNIXFile zosUnixFile = entry.getValue();
                    if (!zosUnixFile.isDirectory()) {
                        zosUnixFile.setDataType(UNIXFileDataType.BINARY);
                        String fileName = zosUnixFile.getUnixPath().substring(this.logsDirectory.getUnixPath().length());
                        if (!fileName.startsWith("state/")) {
                            this.logs.put(fileName, new ZosLibertyServerLogImpl(zosUnixFile, newLogScanner()));
                        }
                        if (isFfdc(zosUnixFile.getUnixPath())) {
                            this.hasFFDC = true;
                        }
                    }
                }
            }
        } catch (ZosUNIXFileException | ZosLibertyManagerException e) {
            throw new ZosLibertyServerException("Unable to list content of logs directory", e);
        }
        this.logsIterator = this.logs.entrySet().iterator();
        return this.logs;
    }

    private boolean isFfdc(String file) {
        return file.matches(".*/ffdc/ffdc_.*\\.log$") || file.matches("ffdc/ffdc_.*\\.log$");
    }
    
    protected ILogScanner newLogScanner() throws ZosLibertyManagerException {
        return this.zosLibertyManager.getLogScanner();
    }
}
