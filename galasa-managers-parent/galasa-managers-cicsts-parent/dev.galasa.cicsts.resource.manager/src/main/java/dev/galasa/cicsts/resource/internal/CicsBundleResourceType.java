/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.resource.internal;

/**
 * Enumeration of resource types of CICS bundle components
 */
public enum CicsBundleResourceType {

    /**
     * ATOMSERVICE
     */
    ATOMSERVICE(false),
    /**
     * CAPTURESPEC
     */
    CAPTURESPEC,
    /**
     * DB2CONN
     */
    DB2CONN,
    /**
     * DB2ENTRY
     */
    DB2ENTRY,
    /**
     * DB2TRAN
     */
    DB2TRAN,
    /**
     * DOCTEMPLATE
     */
    DOCTEMPLATE,
    /**
     * ENQMODEL
     */
    ENQMODEL,
    /**
     * EPADAPTER
     */
    EPADAPTER,
    /**
     * EPADAPTERSET
     */
    EPADAPTERSET,
    /**
     * EVENTBINDING
     */
    EVENTBINDING(true),
    /**
     * FILE
     */
    FILE(false),
    /**
     * JOURNALMODEL
     */
    JOURNALMODEL,
    /**
     * JSONTRANSFRM
     */
    JSONTRANSFRM,
    /**
     * JVMSERVER
     */
    JVMSERVER(true),
    /**
     * LIBRARY
     */
    LIBRARY(false),
    /**
     * NODEPROFILE - not a real bundle part, but needed to ensure it's transferred in EBCDIC
     */
    NODEPROFILE(false),
    /**
     * NODEJSAPP
     */
    NODEJSAPP(true, NODEPROFILE),
    /**
     * MAPSET
     */
    MAPSET,
    /**
     * MQCONN
     */
    MQCONN,
    /**
     * PARTITIONSET
     */
    PARTITIONSET,
    /**
     * PIPELINE
     */
    PIPELINE(false),
    /**
     * PROCESSTYPE
     */
    PROCESSTYPE,
    /**
     * PROGRAM
     */
    PROGRAM(true),
    /**
     * SCACOMPOSITE
     */
    SCACOMPOSITE,
    /**
     * TCPIPSERVICE
     */
    TCPIPSERVICE(true),
    /**
     * TDQUEUE
     */
    TDQUEUE,
    /**
     * TRANSACTION
     */
    TRANSACTION(false),
    /**
     * TSQMODEL
     */
    TSQMODEL,
    /**
     * URIMAP
     */
    URIMAP(false),
    /**
     * WEBSERVICE
     */
    WEBSERVICE(true),
    /**
     * XMLTRANSFORM
     */
    XMLTRANSFORM,
    /**
     * A jar
     */
    JAR(true),
    /**
     * Definition of an OSGi bundle
     */
    OSGIBUNDLE(true, JAR),
    /**
     * Definition of an OSGi service in an OSGi bundle
     */
    OSGISERVICE,
    /**
     * A WAR
     */
    WAR(true),
    /**
     * Definition of a WAR bundle
     */
    WARBUNDLE(true, WAR),
    /**
     * Bundle manifest (cics.xml)
     */
    CICSXML(true),
    /**
     * An EBA
     */
    EBA(true),
    /**
     * Definition of an EBA bundle
     */
    EBABUNDLE(true, EBA),
    /**
     * An EAR
     */
    EAR(true),
    /**
     * Definition of an EAR bundle
     */
    EARBUNDLE(true, EAR),
    /**
     * POLICY
     */
    POLICY(true),
    /**
     * A JVM profile definition
     */
    JVMPROFILE(false),
    /**
     * DB2 PACKAGESET 
     */
    PACKAGESET(true);
    
    private boolean binaryBundleResource = true;
    private CicsBundleResourceType subComponentType = null;     
    
    /**
     * 'Lazy' constructor. Assumes that the resource transfer type is binary
     */
    private CicsBundleResourceType() {}
    
    /**
     * Constructor, explicitly sets the resource transfer type 
     * @param binaryBundleResource
     */
    private CicsBundleResourceType(boolean binaryBundleResource, CicsBundleResourceType subComponentType) {
        this.binaryBundleResource = binaryBundleResource;
        this.subComponentType = subComponentType;
    }
    
    /**
     * Constructor, explicitly sets the resource transfer type 
     * @param binaryBundleResource
     */
    private CicsBundleResourceType(boolean binaryBundleResource) {
        this.binaryBundleResource = binaryBundleResource;
    }
    
    /**
     * Does this resource type require binary transfer 
     * @return true if binary transfer required
     */
    public boolean isBinaryBundleResource() {
        return binaryBundleResource;
    }
    
    /**
     * Return the resource type of a component listed in a component XML  file
     * @return the resource type
     */
    public CicsBundleResourceType getSubComponentType() {
        return this.subComponentType;
    }
}
