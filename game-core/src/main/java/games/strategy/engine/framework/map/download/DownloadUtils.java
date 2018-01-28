package games.strategy.engine.framework.map.download;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import games.strategy.debug.ClientLogger;
import games.strategy.engine.ClientFileSystemHelper;
import games.strategy.engine.framework.system.HttpProxy;

public final class DownloadUtils {
  @VisibleForTesting
  static final ConcurrentMap<String, Long> downloadLengthsByUri = new ConcurrentHashMap<>();

  private DownloadUtils() {}

  /**
   * Gets the download length for the resource at the specified URI.
   *
   * <p>
   * This method is thread safe.
   * </p>
   *
   * @param uri The resource URI; must not be {@code null}.
   *
   * @return The download length (in bytes) or empty if unknown; never {@code null}.
   */
  static Optional<Long> getDownloadLength(final String uri) {
    return getDownloadLengthFromCache(uri, DownloadUtils::getDownloadLengthFromHost);
  }

  @VisibleForTesting
  static Optional<Long> getDownloadLengthFromCache(final String uri, final DownloadLengthSupplier supplier) {
    return Optional.ofNullable(downloadLengthsByUri.computeIfAbsent(uri, k -> supplier.get(k).orElse(null)));
  }

  @VisibleForTesting
  interface DownloadLengthSupplier {
    Optional<Long> get(String uri);
  }

  private static Optional<Long> getDownloadLengthFromHost(final String uri) {
    try (CloseableHttpClient client = newHttpClient()) {
      return getDownloadLengthFromHost(uri, client);
    } catch (final IOException e) {
      ClientLogger.logQuietly(String.format("failed to get download length for '%s'", uri), e);
      return Optional.empty();
    }
  }

  @VisibleForTesting
  static Optional<Long> getDownloadLengthFromHost(
      final String uri,
      final CloseableHttpClient client) throws IOException {
    try (CloseableHttpResponse response = client.execute(newHttpHeadRequest(uri))) {
      final int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        throw new IOException(String.format("unexpected status code (%d)", statusCode));
      }

      final Header header = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
      // NB: it is legal for a server to respond with "Transfer-Encoding: chunked" instead of Content-Length
      if (header == null) {
        return Optional.empty();
      }

      final String encodedLength = header.getValue();
      if (encodedLength == null) {
        throw new IOException("content length header value is absent");
      }

      try {
        return Optional.of(Long.parseLong(encodedLength));
      } catch (final NumberFormatException e) {
        throw new IOException(e);
      }
    }
  }

  private static CloseableHttpClient newHttpClient() {
    return HttpClients.custom().disableCookieManagement().build();
  }

  private static HttpRequestBase newHttpHeadRequest(final String uri) {
    final HttpHead request = new HttpHead(uri);
    HttpProxy.addProxy(request);
    return request;
  }

  /**
   * Creaetes a temp file, downloads the contents of a target uri to that file, returns the file.
   *
   * @param uri The URI whose contents will be downloaded
   */
  public static FileDownloadResult downloadToFile(final String uri) {
    final File file = ClientFileSystemHelper.createTempFile();
    file.deleteOnExit();
    try {
      downloadToFile(uri, file);
      return FileDownloadResult.success(file);
    } catch (final IOException e) {
      ClientLogger.logError("Failed to download: " + uri + ", will attempt to use backup values where available. "
          + "Please check your network connection.", e);
      return FileDownloadResult.FAILURE;
    }
  }


  public static class FileDownloadResult {
    public final boolean wasSuccess;
    public final File downloadedFile;


    public static final FileDownloadResult FAILURE = new FileDownloadResult();

    public static FileDownloadResult success(final File downloadedFile) {
      return new FileDownloadResult(downloadedFile);
    }

    private FileDownloadResult() {
      wasSuccess = false;
      downloadedFile = null;
    }

    private FileDownloadResult(final File downloadedFile) {
      Preconditions.checkState(
          downloadedFile.exists(), "Error, file does not exist: " + downloadedFile.getAbsolutePath());
      this.downloadedFile = downloadedFile;
      this.wasSuccess = true;
    }
  }

  /**
   * Downloads the resource at the specified URI to the specified file.
   *
   * @param uri The resource URI; must not be {@code null}.
   * @param file The file that will receive the resource; must not be {@code null}.
   *
   * @throws IOException If an error occurs during the download.
   */
  public static void downloadToFile(final String uri, final File file) throws IOException {
    checkNotNull(uri);
    checkNotNull(file);

    try (FileOutputStream os = new FileOutputStream(file);
        CloseableHttpClient client = newHttpClient()) {
      downloadToFile(uri, os, client);
    }
  }



  @VisibleForTesting
  static void downloadToFile(
      final String uri,
      final FileOutputStream os,
      final CloseableHttpClient client) throws IOException {
    try (CloseableHttpResponse response = client.execute(newHttpGetRequest(uri))) {
      final int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        throw new IOException(String.format("unexpected status code (%d)", statusCode));
      }

      final HttpEntity entity = response.getEntity();
      if (entity == null) {
        throw new IOException("entity is missing");
      }

      os.getChannel().transferFrom(Channels.newChannel(entity.getContent()), 0L, Long.MAX_VALUE);
    }
  }

  private static HttpRequestBase newHttpGetRequest(final String uri) {
    final HttpGet request = new HttpGet(uri);
    HttpProxy.addProxy(request);
    return request;
  }
}
