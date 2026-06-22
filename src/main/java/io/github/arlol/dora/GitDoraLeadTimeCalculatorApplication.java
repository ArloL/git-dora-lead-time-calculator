package io.github.arlol.dora;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitDoraLeadTimeCalculatorApplication {

	public static void main(String[] args) {
		if (args.length == 1 && "--version".equals(args[0])) {
			Package pkg = GitDoraLeadTimeCalculatorApplication.class
					.getPackage();
			System.out.println(
					versionString(
							pkg.getImplementationTitle(),
							pkg.getImplementationVersion()
					)
			);
			return;
		}
		// dora commitIdOfRelease commitIdOfPreviousRelease
		// timeWhenReleaseIsFullyDone
		// dora acaf1fa5 a54b2bf 2023-12-23T15:16:35+01:00
		if (args.length == 3) {
			String startCommit = args[0];
			String endCommit = args[1];
			var deployInstant = ZonedDateTime.parse(args[2]).toInstant();
			System.out.println("Deploy time in UTC: " + deployInstant);

			List<RevCommit> commits = commitsBetween(startCommit, endCommit);
			List<Instant> authorTimes = new ArrayList<>();
			for (RevCommit commit : commits) {
				Instant authorTime = commit.getAuthorIdent().getWhenAsInstant();
				authorTimes.add(authorTime);
				System.out.println(
						"Considering commit " + commit.getId().getName()
								+ " with author time of " + authorTime
								+ " and message: " + commit.getShortMessage()
				);
			}
			Duration average = averageLeadTime(authorTimes, deployInstant);
			System.out.println(
					"Average between author and deploy times: " + average
			);
		}
	}

	/**
	 * Formats the {@code --version} output line.
	 */
	static String versionString(String title, String version) {
		return title + " version \"" + version + "\"";
	}

	/**
	 * Calculates the average lead time between every commit's author time and
	 * the deploy time.
	 *
	 * <p>
	 * The commits are folded pairwise, so the result is a running average of
	 * the shape {@code ((d1 + d2) / 2 + d3) / 2 ...} rather than the arithmetic
	 * mean. Returns {@link Duration#ZERO} when there are no commits.
	 */
	static Duration averageLeadTime(
			List<Instant> authorTimes,
			Instant deployInstant
	) {
		return authorTimes.stream()
				.map(authorTime -> Duration.between(authorTime, deployInstant))
				.reduce((a, b) -> a.plus(b).dividedBy(2L))
				.orElse(Duration.ZERO);
	}

	/**
	 * Resolves the commits reachable from {@code startCommitId} but not from
	 * {@code endCommitId} in the git repository found via the environment.
	 */
	static List<RevCommit> commitsBetween(
			String startCommitId,
			String endCommitId
	) {
		try (Repository repository = openRepository()) {
			return commitsBetween(repository, startCommitId, endCommitId);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static Repository openRepository() throws IOException {
		return new FileRepositoryBuilder().setMustExist(true)
				.readEnvironment()
				.findGitDir()
				.build();
	}

	/**
	 * Resolves the commits reachable from {@code startCommitId} but not from
	 * {@code endCommitId} in the given repository.
	 */
	static List<RevCommit> commitsBetween(
			Repository repository,
			String startCommitId,
			String endCommitId
	) {
		try (RevWalk revWalk = new RevWalk(repository)) {
			RevCommit startCommit = revWalk
					.parseCommit(repository.resolve(startCommitId));
			RevCommit endCommit = revWalk
					.parseCommit(repository.resolve(endCommitId));

			System.out.println(
					"Commit of release: " + startCommit.getId().getName() + ": "
							+ startCommit.getShortMessage()
			);
			System.out.println(
					"Commit of previous release: " + endCommit.getId().getName()
							+ ": " + endCommit.getShortMessage()
			);

			revWalk.markStart(startCommit);
			revWalk.markUninteresting(endCommit);

			List<RevCommit> commits = new ArrayList<>();
			for (RevCommit commit : revWalk) {
				commits.add(commit);
			}
			return commits;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
