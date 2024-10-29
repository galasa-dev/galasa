/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.terminal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.internal.datastream.CommandEraseWrite;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.spi.Terminal;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class CursorTest extends Zos3270TestBase {

	@Test
	public void goldenPath() throws KeyboardLockedException, Zos3270Exception {
		Terminal terminal = CreateTestTerminal();
		Screen screen = terminal.getScreen();

		setScreen(screen);

		terminal.setCursorPosition(2, 5);

		int bufferPos = screen.getCursor();
		assertThat(bufferPos).as("Buffer position after setCursorPosition").isEqualTo(14);
	}

	@Test
	public void exceedsBuffer() throws KeyboardLockedException, Zos3270Exception {
		Terminal terminal = CreateTestTerminal();
		Screen screen = terminal.getScreen();

		setScreen(screen);

		try {
			terminal.setCursorPosition(3, 1);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("exceeds number of rows");
		}

		try {
			terminal.setCursorPosition(2, 11);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("exceeds number of columns");
		}
	}

	@Test
	public void invalidPositions() throws KeyboardLockedException, Zos3270Exception {
		Terminal terminal = CreateTestTerminal();
		Screen screen = terminal.getScreen();

		setScreen(screen);

		try {
			terminal.setCursorPosition(-1, 1);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("Invalid cursor position");
		}

		try {
			terminal.setCursorPosition(0, 1);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("Invalid cursor position");
		}

		try {
			terminal.setCursorPosition(1, -1);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("Invalid cursor position");
		}
		try {
			terminal.setCursorPosition(1, 0);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("Invalid cursor position");
		}
	}

	private void setScreen(Screen screen) throws DatastreamException {
		ArrayList<AbstractOrder> orders = new ArrayList<>();
		orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
		orders.add(new OrderStartField(false, false, false, false, false, false));
		orders.add(new OrderText("abcd", ebcdic));
		orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
		orders.add(new OrderStartField(false, false, false, false, false, false));
		orders.add(new OrderText("1234", ebcdic));

		screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
				new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

	}

}
