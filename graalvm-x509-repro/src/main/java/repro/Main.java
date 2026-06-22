package repro;

import org.eclipse.jgit.lib.ObjectId;

/**
 * Minimal application that exercises JGit's {@link ObjectId} (which is
 * initialized at build time, see
 * {@code META-INF/native-image/native-image.properties}).
 *
 * <p>On its own this builds fine. Adding the single reachability-metadata
 * entry in {@code META-INF/native-image/reachability-metadata.json} — which
 * registers the nested enum {@code org.eclipse.jgit.util.sha1.SHA1$Sha1Implementation}
 * via the modern metadata format — makes the GraalVM native-image build fail
 * while writing the image heap, because JGit's jar-signing X509 certificate
 * chain is dragged in and {@code sun.security.x509.X509CertInfo} is never
 * marked as instantiated by the static analysis.
 */
public final class Main {

	private Main() {
	}

	public static void main(String[] args) {
		ObjectId id = ObjectId
				.fromString("0123456789012345678901234567890123456789");
		System.out.println(id.name());
	}
}
