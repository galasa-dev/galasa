/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.tags.mocks;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.tags.TagsServlet;
import dev.galasa.framework.spi.IFramework;

public class MockTagsServlet extends TagsServlet {

    public MockTagsServlet(IFramework framework, Environment env) {
        super(env);
        this.framework = framework;
    }
}
