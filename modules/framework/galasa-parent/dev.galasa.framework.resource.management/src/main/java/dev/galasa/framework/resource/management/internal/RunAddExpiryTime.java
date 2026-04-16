/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IUser;

/**
 * One-off initialisation job that adds expiry times to legacy tokens that don't
 * have them. This should be removed after Galasa 1.0.0 is released.
 */
@Component(service = { IResourceManagementProvider.class })
public class RunAddExpiryTime implements IResourceManagementProvider {
    private final Log logger = LogFactory.getLog(getClass());

    private static final int DEFAULT_TOKEN_LIFESPAN_DAYS = 90;

    @Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement) {
        logger.info("Starting token expiry migration");

        try {
            IAuthStore authStore = framework.getAuthStore();
            
            List<IUser> users = authStore.getAllUsers();

            int tokensProcessed = 0;
            int tokensMigrated = 0;

            // For each user, get their tokens
            for (IUser user : users) {
                String loginId = user.getLoginId();

                List<IInternalAuthToken> tokens = authStore.getTokensByLoginId(loginId);

                for (IInternalAuthToken token : tokens) {
                    tokensProcessed++;

                    if (token.getExpiryTime() == null) {
                        logger.info("Token " + token.getTokenId() + " for user " + loginId
                                + " is missing expiryTime, migrating...");

                        // Calculate expiry time based on creation time + 90 days
                        Instant newExpiryTime;
                        newExpiryTime = Instant.now().plus(DEFAULT_TOKEN_LIFESPAN_DAYS, ChronoUnit.DAYS);
                        logger.warn("Token has no creation time, using current time + "
                                + DEFAULT_TOKEN_LIFESPAN_DAYS + " days: " + newExpiryTime);

                        // Update the token with the new expiry time
                        authStore.updateTokenExpiryTime(token.getTokenId(), newExpiryTime);
                        tokensMigrated++;

                        logger.info("Successfully migrated token " + token.getTokenId());
                    }
                }
            }

            logger.info("Token expiry migration complete. Processed " + tokensProcessed + " tokens, migrated "
                    + tokensMigrated + " tokens");

        } catch (AuthStoreException e) {
            logger.error("Failed to migrate token expiry times", e);
        } catch (Exception e) {
            logger.error("Unexpected error during token expiry migration", e);
        }

        return true;
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
    }
}
