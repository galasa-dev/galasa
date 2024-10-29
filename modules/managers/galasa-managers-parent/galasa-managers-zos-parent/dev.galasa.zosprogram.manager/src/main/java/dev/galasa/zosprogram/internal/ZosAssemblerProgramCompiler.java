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
import dev.galasa.zosprogram.internal.properties.ProgramLanguageLinkSyslibs;

public class ZosAssemblerProgramCompiler extends AbstractZosProgramCompiler {

    public ZosAssemblerProgramCompiler(ZosProgramImpl zosProgram) throws ZosProgramException {
        super(zosProgram);
    }

    @Override
    public Map<String, Object> buildParameters() throws ZosProgramException {
        HashMap<String, Object> parameters = new HashMap<>();
        try {
            List<String> lePrefix = LanguageEnvironmentDatasetPrefix.get(zosProgram.getImage().getImageID());
            parameters.put("PROGRAM", zosProgram.getName());
            List<String> compileSyslibs = ProgramLanguageCompileSyslibs.get(zosProgram.getImage().getImageID(), zosProgram.getLanguage());
            List<String> linkSyslibs = ProgramLanguageLinkSyslibs.get(zosProgram.getImage().getImageID(), zosProgram.getLanguage());
            if (zosProgram.isCics()) {
                parameters.put("TYPE", zosProgram.getLanguage().toString() + "/CICS");
                parameters.put("ASM.PARM", "'NODECK,OBJECT,XREF(SHORT),LIST'");
                List<String> cicsPrefix = CICSDatasetPrefix.get(zosProgram.getImage().getImageID());
                parameters.put("TRN.STEPLIB", buildSteplib(cicsPrefix));
                parameters.put("ASM.SYSLIB", buildCompileSyslib(compileSyslibs, lePrefix, cicsPrefix));
                parameters.put("LKED.PARM", "'XREF,LET,LIST'");
                parameters.put("LKED.SYSLIB", buildLinkSyslib(linkSyslibs, lePrefix, cicsPrefix));
                StringBuilder sb = new StringBuilder();
                sb.append(SYSLIN);
                sb.append(NEWLINE);
                sb.append(DD_ASTERISK);
                sb.append(NEWLINE);
                sb.append("  ORDER DFHEAG\n");          
                sb.append("  INCLUDE SYSLIB(DFHEAG)\n");
                sb.append(LKED_SYSIN_ENTRY.replace("++NAME++", zosProgram.getName()));
                sb.append(NEWLINE);
                sb.append(LKED_SYSIN_NAME_REPLACE.replace("++NAME++", zosProgram.getName()));
                parameters.put("LKED.SYSIN", sb.toString());
            } else {
                parameters.put("TYPE", zosProgram.getLanguage().toString() + "/BATCH");
                parameters.put("ASM.PARM", "'NODECK,OBJECT,XREF(SHORT),LIST'");
                parameters.put("ASM.SYSLIB", buildCompileSyslib(compileSyslibs, lePrefix, Collections.emptyList()));
                parameters.put("LKED.PARM", "'XREF,LET,LIST'");
                parameters.put("LKED.SYSLIB", buildLinkSyslib(linkSyslibs, lePrefix, Collections.emptyList()));
                StringBuilder sb = new StringBuilder();
                sb.append(SYSLIN);
                sb.append(NEWLINE);
                sb.append(DD_ASTERISK);
                sb.append(NEWLINE);
                sb.append(LKED_SYSIN_ENTRY.replace("++NAME++", zosProgram.getName()));
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

    protected String buildSteplib(List<String> cicsPrefix) {
        List<String> datasetList = new LinkedList<>();
        Iterator<String> it = cicsPrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SDFHLOAD");
        }
        return formatDatasetConcatenation(datasetList);
    }

    protected String buildCompileSyslib(List<String> compileSyslibs, List<String> lePrefix, List<String> cicsPrefix) {
        List<String> datasetList = new LinkedList<>();
        datasetList.add("SYS1.MACLIB");
        datasetList.add("SYS1.MODGEN");
        datasetList.addAll(compileSyslibs);
        Iterator<String> it = lePrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SCEESAMP");
        }
        it = cicsPrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
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
        if (zosProgram.isCics()) {
            return "assemblerCICS.skel";
        }
        return "assemblerBatch.skel";
    }
}