/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.common.resources.ResourceAction.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.generated.Stream;
import dev.galasa.framework.api.beans.generated.StreamData;
import dev.galasa.framework.api.beans.generated.StreamMetadata;
import dev.galasa.framework.api.beans.generated.StreamOBRData;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.RBACValidator;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.ResourceAction;
import dev.galasa.framework.api.resources.validators.GalasaStreamValidator;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.OBR;
import dev.galasa.framework.spi.streams.StreamsException;

public class GalasaStreamProcessor extends AbstractGalasaResourceProcessor implements IGalasaResourceProcessor {

    private IStreamsService streamsService;
    private final Log logger = LogFactory.getLog(getClass());

    public GalasaStreamProcessor(IStreamsService streamsService, RBACValidator rbacValidator) {
        super(rbacValidator);
        this.streamsService = streamsService;
    }

    @Override
    public List<String> processResource(JsonObject resourceJson, ResourceAction action, String username)
            throws InternalServletException {

        logger.info("Processing GalasaStream resource");
        List<String> errors = checkGalasaStreamJsonStructure(resourceJson, action);

        if (errors.isEmpty()) {

            Stream galasaStream = gson.fromJson(resourceJson, Stream.class);
            String streamName = galasaStream.getmetadata().getname();

            if (action == DELETE) {
                deleteStream(streamName);
            } else {
                try {
                    IStream existingStream = streamsService.getStreamByName(streamName);
                    if (action == CREATE && existingStream != null) {
                        ServletError error = new ServletError(GAL5429_ERROR_STREAM_ALREADY_EXISTS);
                        throw new InternalServletException(error, HttpServletResponse.SC_CONFLICT);
                    } else if (action == UPDATE && existingStream == null) {
                        ServletError error = new ServletError(GAL5432_ERROR_STREAM_DOES_NOT_EXIST);
                        throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
                    }

                    setStream(galasaStream);
                } catch (StreamsException e) {
                    ServletError error = new ServletError(GAL5433_FAILED_TO_SET_STREAM);
                    throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }

            logger.info("Processed GalasaStream resource OK");
        }

        return errors;
    }

    private void setStream(Stream galasaStream) throws InternalServletException {
        try {
            IStream stream = transformGalasaStreamToStream(galasaStream);
            logger.info("Creating stream in CPS store");
            streamsService.setStream(stream);
            logger.info("Created stream in CPS store OK");
        } catch (StreamsException e) {
            ServletError error = new ServletError(GAL5433_FAILED_TO_SET_STREAM);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void deleteStream(String streamName) throws InternalServletException {
        try {
            logger.info("Deleting stream from CPS store");
            streamsService.deleteStream(streamName);
            logger.info("Deleted stream from CPS store OK");
        } catch (StreamsException e) {
            ServletError error = new ServletError(GAL5426_FAILED_TO_DELETE_STREAM);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private List<String> checkGalasaStreamJsonStructure(JsonObject streamJson, ResourceAction action) throws InternalServletException {
        GalasaStreamValidator validator = new GalasaStreamValidator(action);
        return checkGalasaResourceJsonStructure(validator, streamJson);
    }

    @Override
    public void validateActionPermissions(ResourceAction action, String username) throws InternalServletException {
        BuiltInAction requestedAction = getResourceActionAsBuiltInAction(action, BuiltInAction.CPS_PROPERTIES_SET, BuiltInAction.CPS_PROPERTIES_DELETE);
        rbacValidator.validateActionPermitted(requestedAction, username);
    }

    private IStream transformGalasaStreamToStream(Stream galasaStream) throws StreamsException {
        dev.galasa.framework.spi.streams.Stream stream = new dev.galasa.framework.spi.streams.Stream();
        StreamMetadata metadata = galasaStream.getmetadata();
        StreamData data = galasaStream.getdata();

        stream.setName(metadata.getname());
        stream.setDescription(metadata.getdescription());
        stream.setTestCatalogUrl(data.getTestCatalog().geturl());
        stream.setMavenRepositoryUrl(data.getrepository().geturl());
        stream.setIsEnabled(data.getIsEnabled());
        stream.setObrs(transformGalasaStreamOBRsToOBRs(data.getobrs()));

        return stream;
    }

    private List<IOBR> transformGalasaStreamOBRsToOBRs(StreamOBRData[] streamObrs) throws StreamsException {
        List<IOBR> obrs = new ArrayList<>();
        for (StreamOBRData streamObr : streamObrs) {
            OBR obr = new OBR(streamObr.getGroupId(), streamObr.getArtifactId(), streamObr.getversion());
            obrs.add(obr);
        }
        return obrs;
    }
}
