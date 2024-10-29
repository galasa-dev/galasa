/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

import java.nio.file.OpenOption;

public class SetContentType implements OpenOption {

    private final ResultArchiveStoreContentType contentType;

    public SetContentType(ResultArchiveStoreContentType contentType) {
        this.contentType = contentType;
    }

    public ResultArchiveStoreContentType getContentType() {
        return contentType;
    }

}
