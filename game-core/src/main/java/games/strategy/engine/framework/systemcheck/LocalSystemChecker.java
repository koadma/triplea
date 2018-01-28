package games.strategy.engine.framework.systemcheck;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * This class runs a set of local system checks, like access network, and create a temp file.
 * Each check is always run, and this class records the results of those checks.
 */
public final class LocalSystemChecker {

  private final Set<SystemCheck> systemChecks;

  public LocalSystemChecker() {
    this(ImmutableSet.of(defaultNetworkCheck(), defaultFileSystemCheck()));
  }

  @VisibleForTesting
  LocalSystemChecker(final Set<SystemCheck> checks) {
    systemChecks = checks;
  }

  private static SystemCheck defaultNetworkCheck() {
    return new SystemCheck("Can connect to github.com (check network connection)", () -> {
      try {
        final int connectTimeoutInMilliseconds = 20000;
        final URL url = new URL("https://github.com");
        final URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(connectTimeoutInMilliseconds);
        urlConnection.connect();
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private static SystemCheck defaultFileSystemCheck() {
    return new SystemCheck("Can create temporary files (check disk usage, file permissions)", () -> {
      try {
        File.createTempFile("prefix", "suffix").delete();
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  /** Return any exceptions encountered while running each check. */
  public Set<Exception> getExceptions() {
    final Set<Exception> exceptions = Sets.newHashSet();
    for (final SystemCheck systemCheck : systemChecks) {
      if (systemCheck.getException().isPresent()) {
        exceptions.add(systemCheck.getException().get());
      }
    }
    return exceptions;
  }

  public String getStatusMessage() {
    final StringBuilder sb = new StringBuilder();
    for (final SystemCheck systemCheck : systemChecks) {
      sb.append(systemCheck.getResultMessage()).append("\n");
    }
    return sb.toString();
  }
}
