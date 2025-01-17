/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm;

import dev.galasa.ProductVersion;
import dev.galasa.zos.IZosImage;

public interface IImsSystem {

    /***
     * Retrieve the IMS TM System tag
     * @return the tag of the IMS TM System
     */
    String getTag();

    /***
     * Retrieve the IMS TM System applid
     * @return the applid of the IMS TM System
     */
    String getApplid();

    /***
     * Retrieve the IMS TM System version
     * @return the version of the IMS TM System
     * @throws ImstmManagerException If the version is not available
     */
    ProductVersion getVersion() throws ImstmManagerException;
    
    /***
     * Retrieve the zOS Image the IMS TM System resides on
     * @return the zOS Image the IMS TM System resides on
     */
    IZosImage getZosImage();

    void startup() throws ImstmManagerException;
    void shutdown() throws ImstmManagerException;
    
    boolean isProvisionStart();
}