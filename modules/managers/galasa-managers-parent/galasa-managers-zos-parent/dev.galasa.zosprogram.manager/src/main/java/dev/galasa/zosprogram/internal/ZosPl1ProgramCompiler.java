/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dev.galasa.zosprogram.ZosProgramException;
import dev.galasa.zosprogram.ZosProgramManagerException;
import dev.galasa.zosprogram.internal.properties.CICSDatasetPrefix;
import dev.galasa.zosprogram.internal.properties.LanguageEnvironmentDatasetPrefix;
import dev.galasa.zosprogram.internal.properties.ProgramLanguageCompileSyslibs;
import dev.galasa.zosprogram.internal.properties.ProgramLanguageDatasetPrefix;
import dev.galasa.zosprogram.internal.properties.ProgramLanguageLinkSyslibs;

public class ZosPl1ProgramCompiler extends AbstractZosProgramCompiler {

    public ZosPl1ProgramCompiler(ZosProgramImpl zosProgram) throws ZosProgramException {
        super(zosProgram);
    }

    @Override
    public Map<String, Object> buildParameters() throws ZosProgramException {
        HashMap<String, Object> parameters = new HashMap<>();
        try {
            parameters.put("PROGRAM", zosProgram.getName());
            List<String> languagePrefix = ProgramLanguageDatasetPrefix.get(zosProgram.getImage().getImageID(), zosProgram.getLanguage());
            List<String> lePrefix = LanguageEnvironmentDatasetPrefix.get(zosProgram.getImage().getImageID());
            List<String> compileSyslibs = ProgramLanguageCompileSyslibs.get(zosProgram.getImage().getImageID(), zosProgram.getLanguage());
            List<String> linkSyslibs = ProgramLanguageLinkSyslibs.get(zosProgram.getImage().getImageID(), zosProgram.getLanguage());
            if (zosProgram.isCics()) {
                parameters.put("TYPE", zosProgram.getLanguage().toString() + "/CICS");
                parameters.put("PL1.PARM", "'OBJECT,OPTIONS,XREF(FULL),SOURCE,SYSTEM(CICS),PP(CICS)'");
                List<String> cicsPrefix = CICSDatasetPrefix.get(zosProgram.getImage().getImageID());
                parameters.put("PL1.STEPLIB", buildSteplib(languagePrefix, lePrefix, cicsPrefix));
                parameters.put("PL1.SYSLIB", buildCompileSyslib(compileSyslibs, lePrefix, cicsPrefix));
                parameters.put("LKED.PARM", "'LIST,XREF,RENT,MAP'");
                parameters.put("LKED.SYSLIB", buildLinkSyslib(linkSyslibs, lePrefix, cicsPrefix));
                StringBuilder sb = new StringBuilder();
                sb.append("*\n");
                sb.append("  INCLUDE SYSLIB(DFHELII)\n");
                sb.append(DD);
                sb.append(SYSLIN);
                sb.append(NEWLINE);
                sb.append(DD_ASTERISK);
                sb.append(NEWLINE);
                sb.append(LKED_SYSIN_NAME_REPLACE.replace("++NAME++", zosProgram.getName()));
                parameters.put("LKED.SYSIN", sb.toString());
            } else {
                parameters.put("TYPE", zosProgram.getLanguage().toString() + "/BATCH");
                parameters.put("PL1.PARM", "'OBJECT,OPTIONS,XREF(FULL),SOURCE'");
                parameters.put("PL1.STEPLIB", buildSteplib(languagePrefix, lePrefix, Collections.emptyList()));
                parameters.put("PL1.SYSLIB", buildCompileSyslib(compileSyslibs, lePrefix, Collections.emptyList()));
                parameters.put("LKED.PARM", "'LIST,XREF,RENT,MAP'");
                parameters.put("LKED.SYSLIB", buildLinkSyslib(linkSyslibs, lePrefix, Collections.emptyList()));
                StringBuilder sb = new StringBuilder();
                sb.append(SYSLIN);
                sb.append(NEWLINE);
                sb.append(DD_ASTERISK);
                sb.append(NEWLINE);
                sb.append(LKED_SYSIN_NAME_REPLACE.replace("++NAME++", zosProgram.getName()));
                parameters.put("LKED.SYSIN", sb.toString());
            }
            parameters.put("SYSLMOD", zosProgram.getLoadlib().getName());
            parameters.put("SOURCE", zosProgram.getProgramSource());
        } catch (ZosProgramManagerException e) {
            throw new ZosProgramException("Problem building compile JCL for " + zosProgram.getLanguage() + " program " + zosProgram.getName(), e);
        }
        return parameters;        
    }

    protected String buildSteplib(List<String> languagePrefix, List<String> lePrefix, List<String> cicsPrefix) {
        List<String> datasetList = new LinkedList<>();
        Iterator<String> it = languagePrefix.iterator();
        while (it.hasNext()) {
            datasetList.add(it.next() + ".SIBMZCMP");
        }
        it = lePrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SCEERUN");
            datasetList.add(pfx + ".SCEERUN2");
        }
        it = cicsPrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SDFHLOAD");
        }        
        return formatDatasetConcatenation(datasetList);
    }

    protected String buildCompileSyslib(List<String> compileSyslibs, List<String> lePrefix, List<String> cicsPrefix) {
        List<String> datasetList = new LinkedList<>();
        datasetList.addAll(compileSyslibs);
        Iterator<String> it = lePrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SCEESAMP");
        }
        it = cicsPrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SDFHPL1");
            datasetList.add(pfx + ".SDFHMAC");
            datasetList.add(pfx + ".SDFHSAMP");
        }
        return formatDatasetConcatenation(datasetList);
    }

    protected String buildLinkSyslib(List<String> linkSyslibs, List<String> lePrefix, List<String> cicsPrefix) {
        List<String> datasetList = new LinkedList<>();
        datasetList.addAll(linkSyslibs);
        Iterator<String> it = cicsPrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SDFHLOAD");
        }
        it = lePrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SCEELKEX");
            datasetList.add(pfx + ".SCEELKED");
        }
        return formatDatasetConcatenation(datasetList);
    }
    
    @Override
    protected String getSkelName() {
        return "pl1.skel";
    }
}