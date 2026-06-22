package io.github.arlol.dora;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommitsBetweenTests {

	@TempDir
	Path repositoryDir;

	@Test
	void returnsCommitsReachableFromStartButNotFromEnd() throws Exception {
		try (Git git = Git.init().setDirectory(repositoryDir.toFile()).call()) {
			RevCommit base = commit(git, "base");
			RevCommit middle = commit(git, "middle");
			RevCommit head = commit(git, "head");

			List<String> result = names(
					GitDoraLeadTimeCalculatorApplication.commitsBetween(
							git.getRepository(),
							head.getName(),
							base.getName()
					)
			);

			assertEquals(List.of(head.getName(), middle.getName()), result);
		}
	}

	@Test
	void returnsSingleCommitForAdjacentRange() throws Exception {
		try (Git git = Git.init().setDirectory(repositoryDir.toFile()).call()) {
			RevCommit previous = commit(git, "previous");
			RevCommit release = commit(git, "release");

			List<String> result = names(
					GitDoraLeadTimeCalculatorApplication.commitsBetween(
							git.getRepository(),
							release.getName(),
							previous.getName()
					)
			);

			assertEquals(List.of(release.getName()), result);
		}
	}

	@Test
	void isEmptyWhenStartIsReachableFromEnd() throws Exception {
		try (Git git = Git.init().setDirectory(repositoryDir.toFile()).call()) {
			RevCommit previous = commit(git, "previous");
			commit(git, "release");

			List<String> result = names(
					GitDoraLeadTimeCalculatorApplication.commitsBetween(
							git.getRepository(),
							previous.getName(),
							previous.getName()
					)
			);

			assertEquals(List.of(), result);
		}
	}

	@Test
	void preservesAuthorTimes() throws Exception {
		Instant authored = Instant.parse("2024-01-15T10:00:00Z");
		try (Git git = Git.init().setDirectory(repositoryDir.toFile()).call()) {
			RevCommit base = commit(git, "base");
			commitAt(git, "release", authored);

			Repository repository = git.getRepository();
			List<RevCommit> commits = GitDoraLeadTimeCalculatorApplication
					.commitsBetween(repository, "HEAD", base.getName());

			assertEquals(1, commits.size());
			assertEquals(
					authored,
					commits.get(0).getAuthorIdent().getWhenAsInstant()
			);
		}
	}

	private static RevCommit commit(Git git, String message) throws Exception {
		return commitAt(git, message, Instant.parse("2024-01-01T00:00:00Z"));
	}

	private static RevCommit commitAt(Git git, String message, Instant when)
			throws Exception {
		PersonIdent ident = new PersonIdent(
				"Tester",
				"tester@example.com",
				when,
				ZoneOffset.UTC
		);
		return git.commit()
				.setMessage(message)
				.setAllowEmpty(true)
				.setSign(false)
				.setAuthor(ident)
				.setCommitter(ident)
				.call();
	}

	private static List<String> names(List<RevCommit> commits) {
		return commits.stream().map(RevCommit::getName).toList();
	}

}
