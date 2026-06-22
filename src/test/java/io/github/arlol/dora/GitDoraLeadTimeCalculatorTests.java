package io.github.arlol.dora;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GitDoraLeadTimeCalculatorTests {

	private static final Instant DEPLOY = Instant.parse("2024-01-15T12:00:00Z");

	@Test
	public void averageLeadTimeIsZeroWithoutCommits() {
		assertEquals(
				Duration.ZERO,
				GitDoraLeadTimeCalculatorApplication
						.averageLeadTime(List.of(), DEPLOY)
		);
	}

	@Test
	public void averageLeadTimeOfSingleCommitIsItsLeadTime() {
		Instant authored = Instant.parse("2024-01-15T10:00:00Z");

		assertEquals(
				Duration.ofHours(2),
				GitDoraLeadTimeCalculatorApplication
						.averageLeadTime(List.of(authored), DEPLOY)
		);
	}

	@Test
	public void averageLeadTimeOfTwoCommitsIsTheirMean() {
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
	public void averageLeadTimeFoldsCommitsPairwise() {
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
	public void averageLeadTimeIsNegativeWhenAuthoredAfterDeploy() {
		Instant afterDeploy = DEPLOY.plus(Duration.ofHours(1));

		assertEquals(
				Duration.ofHours(-1),
				GitDoraLeadTimeCalculatorApplication
						.averageLeadTime(List.of(afterDeploy), DEPLOY)
		);
	}

	@Test
	public void versionStringIsFormatted() {
		assertEquals(
				"git-dora-lead-time-calculator version \"1.2.3\"",
				GitDoraLeadTimeCalculatorApplication
						.versionString("git-dora-lead-time-calculator", "1.2.3")
		);
	}

}
