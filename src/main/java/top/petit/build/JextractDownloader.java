package top.petit.build;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.zip.GZIPInputStream;

public class JextractDownloader {

    public static void main(String[] args) throws Exception {
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
            System.setProperty("jextract.executable", executablePath.toAbsolutePath().toString());

            // Write to file for Maven to read
            Path propsFile = Paths.get("target/jextract.properties");
            Files.writeString(propsFile, "jextract.executable=" + executablePath.toAbsolutePath().toString());
            return;
        }

        System.out.println("Downloading jextract from: " + url);
        System.out.println("Target directory: " + downloadPath.toAbsolutePath());

        Files.createDirectories(downloadPath);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        Path tarGzFile = downloadPath.resolve("jextract.tar.gz");
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(tarGzFile));

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to download jextract: HTTP " + response.statusCode());
        }

        System.out.println("Extracting jextract...");
        extractTarGz(tarGzFile, downloadPath);

        Files.deleteIfExists(tarGzFile);

        if (!Files.exists(executablePath)) {
            throw new RuntimeException("jextract executable not found after extraction: " + executablePath);
        }

        if (!osName.contains("win")) {
            executablePath.toFile().setExecutable(true);
        }

        System.setProperty("jextract.executable", executablePath.toAbsolutePath().toString());

        // Write to file for Maven to read
        Path propsFile = Paths.get("target/jextract.properties");
        Files.writeString(propsFile, "jextract.executable=" + executablePath.toAbsolutePath().toString());

        System.out.println("jextract downloaded successfully: " + executablePath);
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
                    // Only write the actual file size, not the padded size
                    try (OutputStream os = Files.newOutputStream(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                        byte[] buffer = new byte[8192];
                        long remaining = entry.getSize();
                        while (remaining > 0) {
                            int toRead = (int) Math.min(buffer.length, remaining);
                            int read = tis.read(buffer, 0, toRead);
                            if (read <= 0) break;
                            os.write(buffer, 0, read);
                            remaining -= read;
                        }
                    }
                }
            }
        }
    }

    // Simple TAR implementation
    static class TarInputStream extends FilterInputStream {
        private byte[] buffer = new byte[512];
        private boolean hasNextEntry = true;
        private long bytesRemainingInCurrentEntry = 0;

        public TarInputStream(InputStream in) {
            super(in);
        }

        public TarEntry getNextEntry() throws IOException {
            if (!hasNextEntry) return null;

            // Skip any remaining bytes from the previous entry
            if (bytesRemainingInCurrentEntry > 0) {
                long skipped = in.skip(bytesRemainingInCurrentEntry);
                bytesRemainingInCurrentEntry = 0;
            }

            int read = in.readNBytes(buffer, 0, 512);
            if (read < 512) {
                hasNextEntry = false;
                return null;
            }

            // Check if it's an empty block (end of archive)
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

            // Calculate padding (tar files are aligned to 512 bytes)
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
            if (str.isEmpty()) return 0;
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

        public String getName() { return name; }
        public long getSize() { return size; }
        public boolean isDirectory() { return directory; }
    }
}
