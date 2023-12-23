package io.github.arlol.dora;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class GitDoraLeadTimeCalculatorApplication {

	public static void main(String[] args) {
		if (args.length == 1 && "--version".equals(args[0])) {
			Package pkg = GitDoraLeadTimeCalculatorApplication.class
					.getPackage();
			String title = pkg.getImplementationTitle();
			String version = pkg.getImplementationVersion();
			System.out.println(title + " version \"" + version + "\"");
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

			var commits = jgit(startCommit, endCommit, deployInstant);
			var average = StreamSupport.stream(commits.spliterator(), false)
					.peek(commit -> {
						System.out.println(
								"Considering commit " + commit.getId().getName()
										+ " with author time of "
										+ commit.getAuthorIdent()
												.getWhenAsInstant()
										+ " and message: "
										+ commit.getShortMessage()
						);
					})
					.map(commit -> {
						return Duration.between(
								commit.getAuthorIdent().getWhenAsInstant(),
								deployInstant
						);
					})
					.reduce((a, b) -> a.plus(b).dividedBy(2L))
					.orElse(Duration.ZERO);
			System.out.println(
					"Average between author and deploy times: " + average
			);
		}
	}

	@SuppressFBWarnings(
			value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE",
			justification = "FileRepositoryBuilder uses generics which spotbugs cant know"
	)
	private static Iterable<RevCommit> jgit(
			String startCommitId,
			String endCommitId,
			Instant deployInstant
	) {
		try (Repository repository = new FileRepositoryBuilder()
				.setMustExist(true)
				.readEnvironment()
				.findGitDir()
				.build(); RevWalk revWalk = new RevWalk(repository);) {
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
			return revWalk;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
