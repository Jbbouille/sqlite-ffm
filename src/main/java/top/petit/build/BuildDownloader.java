package top.petit.build;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BuildDownloader {

  // exec-maven-plugin invokes main via reflection using Class.getMethod("main", String[].class),
  // so the full public static void main(String[] args) signature is required — flexible main (JEP 512) won't work here.
  public static void main(String[] args) throws Exception {
    downloadJextract();
    downloadSqlite();
  }

  private static void downloadJextract() throws Exception {
    String jextractVersion = System.getProperty("jextract.version", "25");
    String jextractBuild = System.getProperty("jextract.build", "2");
    String downloadDir = System.getProperty("jextract.downloadDir", "target/jextract-download");

    String osName = System.getProperty("os.name").toLowerCase();
    String osArch = System.getProperty("os.arch").toLowerCase();

    String platform;
    String executable;

    if (osName.contains("win")) {
      platform = "windows";
      executable = "jextract.bat";
    } else if (osName.contains("mac")) {
      platform = "macos";
      executable = "jextract";
    } else if (osName.contains("nux")) {
      platform = "linux";
      executable = "jextract";
    } else {
      throw new RuntimeException("Unsupported OS: " + osName);
    }

    String arch = osArch.contains("64") ? "x64" : osArch;

    String url = String.format(
      "https://download.java.net/java/early_access/jextract/%s/%s/openjdk-%s-jextract+%s-4_%s-%s_bin.tar.gz",
      jextractVersion, jextractBuild, jextractVersion, jextractBuild, platform, arch
    );

    Path downloadPath = Paths.get(downloadDir);
    Path executablePath = downloadPath
      .resolve("jextract-" + jextractVersion)
      .resolve("bin")
      .resolve(executable);

    if (Files.exists(executablePath)) {
      System.out.println("jextract already downloaded at: " + executablePath);
      writeJextractProperties(executablePath);
      return;
    }

    System.out.println("Downloading jextract from: " + url);
    System.out.println("Target directory: " + downloadPath.toAbsolutePath());

    Files.createDirectories(downloadPath);

    Path tarGzFile = downloadPath.resolve("jextract.tar.gz");
    download(url, tarGzFile);

    System.out.println("Extracting jextract...");
    extractTarGz(tarGzFile, downloadPath);
    Files.deleteIfExists(tarGzFile);

    if (!Files.exists(executablePath)) {
      throw new RuntimeException("jextract executable not found after extraction: " + executablePath);
    }

    if (!osName.contains("win")) {
      executablePath.toFile().setExecutable(true);
    }

    writeJextractProperties(executablePath);
    System.out.println("jextract downloaded successfully: " + executablePath);
  }

  private static void writeJextractProperties(Path executablePath) throws IOException {
    System.setProperty("jextract.executable", executablePath.toAbsolutePath().toString());
    Path propsFile = Paths.get("target/jextract.properties");
    Files.writeString(propsFile, "jextract.executable=" + executablePath.toAbsolutePath().toString());
  }

  private static void downloadSqlite() throws Exception {
    String sqliteVersion = System.getProperty("sqlite.version", "3530000");
    String sqliteYear = System.getProperty("sqlite.year", "2026");
    String downloadDir = System.getProperty("sqlite.downloadDir", "target/sqlite-download");

    Path amalgamationDir = Paths.get(downloadDir).resolve("sqlite-amalgamation-" + sqliteVersion);
    if (Files.exists(amalgamationDir)) {
      System.out.println("SQLite amalgamation already downloaded at: " + amalgamationDir);
      return;
    }

    String url = "https://sqlite.org/" + sqliteYear + "/sqlite-amalgamation-" + sqliteVersion + ".zip";
    System.out.println("Downloading SQLite amalgamation from: " + url);

    Path downloadPath = Paths.get(downloadDir);
    Files.createDirectories(downloadPath);

    Path zipFile = downloadPath.resolve("sqlite-amalgamation-" + sqliteVersion + ".zip");
    download(url, zipFile);

    System.out.println("Extracting SQLite amalgamation...");
    extractZip(zipFile, downloadPath);
    Files.deleteIfExists(zipFile);

    if (!Files.exists(amalgamationDir)) {
      throw new RuntimeException("SQLite amalgamation not found after extraction: " + amalgamationDir);
    }

    System.out.println("SQLite amalgamation downloaded successfully: " + amalgamationDir);
  }

  private static void download(String url, Path target) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .GET()
      .build();

    long start = System.currentTimeMillis();
    HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(target));
    if (response.statusCode() != 200) {
      throw new RuntimeException("Failed to download " + url + ": HTTP " + response.statusCode());
    }
    long elapsed = System.currentTimeMillis() - start;
    long sizeKb = Files.size(target) / 1024;
    System.out.println("Downloaded " + sizeKb + " KB in " + elapsed + " ms -> " + target.getFileName());
  }

  private static void extractZip(Path zipFile, Path targetDir) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        Path outputPath = targetDir.resolve(entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectories(outputPath);
        } else {
          Files.createDirectories(outputPath.getParent());
          Files.copy(zis, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }
        zis.closeEntry();
      }
    }
  }

  private static void extractTarGz(Path tarGzFile, Path targetDir) throws IOException {
    try (InputStream fis = Files.newInputStream(tarGzFile);
      GZIPInputStream gzis = new GZIPInputStream(fis);
      TarInputStream tis = new TarInputStream(gzis)) {

      TarEntry entry;
      while ((entry = tis.getNextEntry()) != null) {
        Path outputPath = targetDir.resolve(entry.getName());

        if (entry.isDirectory()) {
          Files.createDirectories(outputPath);
        } else {
          Files.createDirectories(outputPath.getParent());
          try (OutputStream os = Files.newOutputStream(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            byte[] buffer = new byte[8192];
            long remaining = entry.getSize();
            while (remaining > 0) {
              int toRead = (int) Math.min(buffer.length, remaining);
              int read = tis.read(buffer, 0, toRead);
              if (read <= 0) {
                break;
              }
              os.write(buffer, 0, read);
              remaining -= read;
            }
          }
        }
      }
    }
  }

  static class TarInputStream extends FilterInputStream {

    private byte[] buffer = new byte[512];
    private boolean hasNextEntry = true;
    private long bytesRemainingInCurrentEntry = 0;

    public TarInputStream(InputStream in) {
      super(in);
    }

    public TarEntry getNextEntry() throws IOException {
      if (!hasNextEntry) {
        return null;
      }

      if (bytesRemainingInCurrentEntry > 0) {
        in.skip(bytesRemainingInCurrentEntry);
        bytesRemainingInCurrentEntry = 0;
      }

      int read = in.readNBytes(buffer, 0, 512);
      if (read < 512) {
        hasNextEntry = false;
        return null;
      }

      boolean allZeros = true;
      for (byte b : buffer) {
        if (b != 0) {
          allZeros = false;
          break;
        }
      }
      if (allZeros) {
        hasNextEntry = false;
        return null;
      }

      String name = extractString(buffer, 0, 100);
      long size = extractOctal(buffer, 124, 12);
      byte typeFlag = buffer[156];

      bytesRemainingInCurrentEntry = ((size + 511) / 512) * 512;

      return new TarEntry(name, size, typeFlag == '5');
    }

    private String extractString(byte[] buffer, int offset, int length) {
      int end = offset;
      while (end < offset + length && buffer[end] != 0) {
        end++;
      }
      return new String(buffer, offset, end - offset);
    }

    private long extractOctal(byte[] buffer, int offset, int length) {
      String str = extractString(buffer, offset, length).trim();
      if (str.isEmpty()) {
        return 0;
      }
      return Long.parseLong(str, 8);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (bytesRemainingInCurrentEntry <= 0) {
        return -1;
      }
      int toRead = (int) Math.min(len, bytesRemainingInCurrentEntry);
      int actuallyRead = in.read(b, off, toRead);
      if (actuallyRead > 0) {
        bytesRemainingInCurrentEntry -= actuallyRead;
      }
      return actuallyRead;
    }
  }

  static class TarEntry {

    private final String name;
    private final long size;
    private final boolean directory;

    public TarEntry(String name, long size, boolean directory) {
      this.name = name;
      this.size = size;
      this.directory = directory;
    }

    public String getName() {
      return name;
    }

    public long getSize() {
      return size;
    }

    public boolean isDirectory() {
      return directory;
    }
  }
}
