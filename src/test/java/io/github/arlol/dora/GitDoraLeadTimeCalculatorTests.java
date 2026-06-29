package io.github.arlol.dora;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

class GitDoraLeadTimeCalculatorTests {

	@Test
	void versionPrintsTitleAndVersion() {
		String out = captureOut(
				() -> GitDoraLeadTimeCalculatorApplication.main("--version")
		);
		assertTrue(
				out.contains("version \""),
				"Expected --version output to contain 'version \"'"
		);
	}

	private static String captureOut(Runnable runnable) {
		PrintStream previous = System.out;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		System.setOut(new PrintStream(buffer));
		try {
			runnable.run();
		} finally {
			System.setOut(previous);
		}
		return buffer.toString();
	}

}
