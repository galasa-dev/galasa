/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.internal.properties.InputClass;
import dev.galasa.zosbatch.internal.properties.MsgClass;
import dev.galasa.zosbatch.internal.properties.MsgLevel;

/**
 * Provides overrides for the Jobcard for jobs submitted via the Batch Manager.
 * 
 * No validation is performed on the values that are set
 * 
 *  
 *
 */
public class ZosBatchJobcard {
    
    public enum Typrun {
        COPY, HOLD, JCLHOLD, SCAN
    }
    
    private String inputClass;
    private String msgClass;
    private String msgLevel;
    private String region;
    private String memlimit;
    private Typrun typrun;
    private String user;
    private String password;
    private String cond;
    private String time;
    private String account;
    private String progname;

    public ZosBatchJobcard() {}
    
    /**
     * @param inputClass value for CLASS=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setInputClass(String inputClass) {
        this.inputClass = inputClass;
        return this;
    }

    /**
     * @param msgClass value for MSGCLASS=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setMsgClass(String msgClass) {
        this.msgClass = msgClass;
        return this;
    }

    /**
     * @param msgLevel value for MSGLEVEL=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setMsgLevel(String msgLevel) {
        this.msgLevel = msgLevel;
        return this;
    }

    /**
     * @param region value for REGION=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setRegion(String region) {
        this.region = region;
        return this;
    }

    /**
     * @param memlimit value for MEMLIMIT=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setMemlimit(String memlimit) {
        this.memlimit = memlimit;
        return this;
    }

    /**
     * @param typrun value for TYPRUN=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setTyprun(Typrun typrun) {
        this.typrun = typrun;
        return this;
    }

    /**
     * @param user value for USER=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setUser(String user) {
        this.user = user;
        return this;
    }

    /**
     * @param password value for PASSWORD=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * @param cond value for COND=
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setCond(String cond) {
        this.cond = cond;
        return this;
    }

    /**
     * @param time value for time field on JOB
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setTime(String time) {
        this.time = time;
        return this;
    }

    /**
     * @param account value for account field on JOB
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setAccount(String account) {
        this.account = account;
        return this;
    }

    /**
     * @param progname value for programmer field on JOB
     * @return this class for Fluent calls
     */
    public ZosBatchJobcard setProgrammerName(String progname) {
        this.progname = progname;
        return this;
    }

    /**
     * @return CLASS= value
     */
    public String getInputClass() {
        return nulled(inputClass);
    }

    /**
     * @return MSGCLASS= value
     */
    public String getMsgClass() {
        return nulled(msgClass);
    }

    /**
     * @return MSGLEVEL= value
     */
    public String getMsgLevel() {
        return nulled(msgLevel);
    }

    /**
     * @return REGION= value
     */
    public String getRegion() {
        return nulled(region);
    }

    /**
     * @return MEMLIMIT= value
     */
    public String getMemlimit() {
        return nulled(memlimit);
    }

    /**
     * @return TYPRUN= value
     */
    public Typrun getTyprun() {
        return typrun;
    }

    /**
     * @return USER= value
     */
    public String getUser() {
        return nulled(user);
    }

    /**
     * @return PASSWORD= value
     */
    public String getPassword() {
        return nulled(password);
    }

    /**
     * @return COND= value
     */
    public String getCond() {
        return nulled(cond);
    }

    /**
     * @return TIME= value
     */
    public String getTime() {
        return nulled(time);
    }

    /**
     * @return JOB account value
     */
    public String getAccount() {
        return nulled(account);
    }

    /**
     * @return JOB programmer value
     */
    public String getProgrammerName() {
        return nulled(progname);
    }
    
    /**
     * @return the built job card
     * @throws ZosBatchManagerException 
     */
    public String getJobcard(String jobname, IZosImage image) throws ZosBatchManagerException {
        StringBuilder jobCard = new StringBuilder();
        jobCard.append("//");
        jobCard.append(jobname);
        jobCard.append(" JOB ");
        String acct = getAccount();
        String prog = getProgrammerName();
        if (acct != null || prog != null) {
            if (acct != null) {
            	if (!acct.startsWith("(")) {
            		jobCard.append("(");
            	}
            	jobCard.append(acct);
            	if (!acct.endsWith(")")) {
                   	jobCard.append(")");
                }
            }
            if (prog != null) {
                jobCard.append(",");
                jobCard.append("'");
                jobCard.append(prog);
                jobCard.append("'");
            }
        }
        jobCard.append(",\n");
        
        if (inputClass == null) {
            inputClass = InputClass.get(image);
        }
        
        if (msgClass == null) {
            msgClass = MsgClass.get(image);
        }
        
        if (msgLevel == null) {
            msgLevel = MsgLevel.get(image);
        }
        
        if (region != null) {
            jobCard.append("//         REGION=");
            jobCard.append(region);
            jobCard.append(",\n");
        }
        
        if (memlimit != null) {
            jobCard.append("//         MEMLIMIT=");
            jobCard.append(memlimit);
            jobCard.append(",\n");
        }
        
        if (typrun != null) {
            jobCard.append("//         TYPRUN=");
            jobCard.append(typrun.toString());
            jobCard.append(",\n");
        }
        
        if (user != null) {
            jobCard.append("//         USER=");
            jobCard.append(user);
            jobCard.append(",\n");
        }
        
        if (password != null) {
            jobCard.append("//         PASSWORD=");
            jobCard.append(password);
            jobCard.append(",\n");
        }
        
        if (cond != null) {
            jobCard.append("//         COND=");
            jobCard.append(cond);
            jobCard.append(",\n");
        }
        
        if (time != null) {
            jobCard.append("//         TIME=");
            jobCard.append(time);
            jobCard.append(",\n");
        }
        
        jobCard.append("//         CLASS=");
        jobCard.append(inputClass);
        jobCard.append(",\n");
        jobCard.append("//         MSGCLASS=");
        jobCard.append(msgClass);
        jobCard.append(",\n");
        jobCard.append("//         MSGLEVEL=");
        jobCard.append(msgLevel);
        jobCard.append("\n");
        
        return jobCard.toString();
    }

	protected String nulled(String value) {
	    if (value == null) {
	        return null;
	    }
	    value = value.trim();
	    if (value.isEmpty()) {
	        return null;
	    }
	    return value;
	}
}
