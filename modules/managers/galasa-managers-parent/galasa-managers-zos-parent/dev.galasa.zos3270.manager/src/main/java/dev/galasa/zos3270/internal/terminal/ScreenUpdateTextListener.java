/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.terminal;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.ErrorTextFoundException;
import dev.galasa.zos3270.IScreenUpdateListener;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.spi.Screen;

public class ScreenUpdateTextListener implements IScreenUpdateListener {

    private final Screen screen;
    private final String[] okText;
    private final String[] errorText;
    private int foundItem = -1;
    private ErrorTextFoundException errorFound = null;
    private Semaphore    textFound = new Semaphore(1, true);

    public ScreenUpdateTextListener(Screen screen, String ok[], String error[]) throws InterruptedException {
        this.screen = screen;
        this.okText = ok;
        this.errorText = error;
        this.textFound.acquire();

        synchronized (this.screen) {
            screenUpdated(Direction.RECEIVED, null);
            if (this.textFound.availablePermits() > 0) {
                return;
            }
            this.screen.registerScreenUpdateListener(this);
        }
    }

    @Override
    public void screenUpdated(Direction direction, AttentionIdentification aid) {
        try {
            this.foundItem = screen.searchFieldContaining(okText, errorText);
            this.textFound.release();
        } catch (TextNotFoundException e) {
            // IGNORE
        } catch (ErrorTextFoundException e) {
            this.errorFound = e;
            this.textFound.release();
        }

    }

    public int waitForText(long maxWait) throws InterruptedException, ErrorTextFoundException, Zos3270Exception {
        if (this.textFound == null) {
            throw new Zos3270Exception("Not allowed to use this listener more than once");
        }
        this.textFound.tryAcquire(1, maxWait, TimeUnit.MILLISECONDS);
        this.textFound = null;
        screen.unregisterScreenUpdateListener(this);
        
        if (errorFound != null) {
            throw errorFound;
        }

        return foundItem;
    }

    public static int waitForText(Screen screen, String ok[], String error[], long maxWait)
            throws InterruptedException, Zos3270Exception {
        return new ScreenUpdateTextListener(screen, ok, error).waitForText(maxWait);
    }

}
