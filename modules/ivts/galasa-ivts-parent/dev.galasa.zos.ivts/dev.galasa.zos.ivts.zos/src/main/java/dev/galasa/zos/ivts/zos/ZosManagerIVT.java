/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.ivts.zos;

import static org.assertj.core.api.Assertions.*;
import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zos.ZosIpHost;
import dev.galasa.zos.ZosManagerException;

@Test
public class ZosManagerIVT {
    
    @Logger
    public Log logger;
    
    @ZosImage
    public IZosImage imagePrimary;
    
    @ZosIpHost
    public IIpHost hostPrimary;
    
    @Test
    public void checkPrimaryImage() throws Exception {
    	assertThat(imagePrimary).isNotNull();
    	assertThat(hostPrimary).isNotNull();
        logger.info("The Primary Image field has been correctly initialised");
    }

    @Test
    public void checkDefaultCredentials() throws Exception, ZosManagerException {
    	assertThat(imagePrimary.getDefaultCredentials()).isNotNull();
        logger.info("The Primary Credentials are being returned");
    }

}
