/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.internal;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.generated.UpdateGalasaMonitorRequest;
import dev.galasa.framework.api.beans.generated.UpdateGalasaMonitorRequestdata;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;

public class UpdateMonitorRequestValidator extends GalasaResourceValidator<UpdateGalasaMonitorRequest> {

    @Override
    public void validate(UpdateGalasaMonitorRequest monitor) throws InternalServletException {
        UpdateGalasaMonitorRequestdata requestData = monitor.getdata();

        if (requestData == null) {
            ServletError error = new ServletError(GAL5425_ERROR_MONITOR_UPDATE_MISSING_DATA);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
