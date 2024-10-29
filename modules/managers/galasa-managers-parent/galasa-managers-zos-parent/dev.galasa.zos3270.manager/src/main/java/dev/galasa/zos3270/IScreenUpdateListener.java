/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270;

import javax.validation.constraints.NotNull;

public interface IScreenUpdateListener {

    public enum Direction {
        RECEIVED,
        SENDING
    }

    void screenUpdated(@NotNull Direction direction, AttentionIdentification aid);

}
