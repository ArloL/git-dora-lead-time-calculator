package io.github.arlol.dora;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class GitDoraLeadTimeCalculatorTests {

	private static final Instant DEPLOY = Instant.parse("2024-01-15T12:00:00Z");

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

	@Test
	void leadTimeOfEmptyRangeInOwnRepositoryIsZero() {
		String out = captureOut(
				() -> GitDoraLeadTimeCalculatorApplication
						.main("HEAD", "HEAD", "2024-01-15T12:00:00Z")
		);
		assertTrue(
				out.contains("Average between author and deploy times: PT0S"),
				"Expected zero average for an empty range"
		);
	}

	@Test
	void averageLeadTimeIsZeroWithoutCommits() {
		assertEquals(
				Duration.ZERO,
				GitDoraLeadTimeCalculatorApplication
						.averageLeadTime(List.of(), DEPLOY)
		);
	}

	@Test
	void averageLeadTimeOfSingleCommitIsItsLeadTime() {
		Instant authored = Instant.parse("2024-01-15T10:00:00Z");

		assertEquals(
				Duration.ofHours(2),
				GitDoraLeadTimeCalculatorApplication
						.averageLeadTime(List.of(authored), DEPLOY)
		);
	}

	@Test
	void averageLeadTimeOfTwoCommitsIsTheirMean() {
		Instant twoHoursBefore = DEPLOY.minus(Duration.ofHours(2));
		Instant fourHoursBefore = DEPLOY.minus(Duration.ofHours(4));

		assertEquals(
				Duration.ofHours(3),
				GitDoraLeadTimeCalculatorApplication.averageLeadTime(
						List.of(twoHoursBefore, fourHoursBefore),
						DEPLOY
				)
		);
	}

	@Test
	void averageLeadTimeFoldsCommitsPairwise() {
		// Three commits are reduced as ((d1 + d2) / 2 + d3) / 2.
		// d1 = 2h, d2 = 4h, d3 = 6h -> ((2+4)/2 + 6)/2 = (3 + 6)/2 = 4.5h
		Instant twoHoursBefore = DEPLOY.minus(Duration.ofHours(2));
		Instant fourHoursBefore = DEPLOY.minus(Duration.ofHours(4));
		Instant sixHoursBefore = DEPLOY.minus(Duration.ofHours(6));

		assertEquals(
				Duration.ofMinutes(270),
				GitDoraLeadTimeCalculatorApplication.averageLeadTime(
						List.of(
								twoHoursBefore,
								fourHoursBefore,
								sixHoursBefore
						),
						DEPLOY
				)
		);
	}

	@Test
	void averageLeadTimeIsNegativeWhenAuthoredAfterDeploy() {
		Instant afterDeploy = DEPLOY.plus(Duration.ofHours(1));

		assertEquals(
				Duration.ofHours(-1),
				GitDoraLeadTimeCalculatorApplication
						.averageLeadTime(List.of(afterDeploy), DEPLOY)
		);
	}

	@Test
	void versionStringIsFormatted() {
		assertEquals(
				"git-dora-lead-time-calculator version \"1.2.3\"",
				GitDoraLeadTimeCalculatorApplication
						.versionString("git-dora-lead-time-calculator", "1.2.3")
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
