package top.petit.build;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads SQLite native library from sqlite.org
 * Windows: prebuilt DLL from sqlite.org
 * Linux/macOS: requires compilation from amalgamation (not yet implemented)
 */
public class SqliteNativeDownloader {

    public static void main(String[] args) throws Exception {
        String sqliteVersion = System.getProperty("sqlite.version", "3510100");
        String sqliteYear = System.getProperty("sqlite.year", "2025");
        String downloadDir = System.getProperty("sqlite.downloadDir", "target/sqlite-native");

        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        Path downloadPath = Paths.get(downloadDir);
        Files.createDirectories(downloadPath);

        if (osName.contains("win")) {
            downloadWindows(downloadPath, sqliteVersion, sqliteYear, osArch);
        } else if (osName.contains("mac")) {
            compileMacOS(downloadPath, sqliteVersion);
        } else if (osName.contains("nux")) {
            compileLinux(downloadPath, sqliteVersion);
        } else {
            throw new RuntimeException("Unsupported OS: " + osName);
        }
    }

    private static void downloadWindows(Path downloadPath, String version, String year, String arch) throws Exception {
        String archSuffix = arch.contains("64") ? "x64" : "x86";
        String targetLibName = "sqlite3.dll";
        Path targetLibPath = downloadPath.resolve(targetLibName);

        if (Files.exists(targetLibPath)) {
            System.out.println("SQLite native library already exists: " + targetLibPath.toAbsolutePath());
            return;
        }

        // Download from sqlite.org
        // Example: https://www.sqlite.org/2025/sqlite-dll-win-x64-3510100.zip
        String url = String.format(
            "https://www.sqlite.org/%s/sqlite-dll-win-%s-%s.zip",
            year, archSuffix, version
        );

        System.out.println("Downloading SQLite DLL from: " + url);

        HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        Path zipFile = downloadPath.resolve("sqlite-dll.zip");
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(zipFile));

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to download SQLite: HTTP " + response.statusCode());
        }

        System.out.println("Extracting sqlite3.dll...");
        extractDllFromZip(zipFile, downloadPath, "sqlite3.dll");

        Files.deleteIfExists(zipFile);

        System.out.println("SQLite native library extracted: " + targetLibPath.toAbsolutePath());
    }

    private static void extractDllFromZip(Path zipFile, Path targetDir, String fileName) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equalsIgnoreCase(fileName)) {
                    Path targetPath = targetDir.resolve(fileName);
                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }
        }
        throw new RuntimeException("File not found in zip: " + fileName);
    }

    private static void compileLinux(Path downloadPath, String version) throws Exception {
        Path targetLib = downloadPath.resolve("libsqlite3.so");
        if (Files.exists(targetLib)) {
            System.out.println("SQLite native library already exists: " + targetLib.toAbsolutePath());
            return;
        }

        Path amalgamationDir = Paths.get("target/sqlite-download/sqlite-amalgamation-" + version);
        if (!Files.exists(amalgamationDir)) {
            throw new RuntimeException("SQLite amalgamation not found. Run 'mvn generate-sources' first.");
        }

        System.out.println("Compiling SQLite for Linux...");
        ProcessBuilder pb = new ProcessBuilder(
            "gcc", "-shared", "-fPIC", "-O2",
            "-o", targetLib.toAbsolutePath().toString(),
            amalgamationDir.resolve("sqlite3.c").toAbsolutePath().toString()
        );
        pb.inheritIO();
        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to compile SQLite. Make sure gcc is installed.");
        }
        System.out.println("SQLite compiled: " + targetLib.toAbsolutePath());
    }

    private static void compileMacOS(Path downloadPath, String version) throws Exception {
        Path targetLib = downloadPath.resolve("libsqlite3.dylib");
        if (Files.exists(targetLib)) {
            System.out.println("SQLite native library already exists: " + targetLib.toAbsolutePath());
            return;
        }

        Path amalgamationDir = Paths.get("target/sqlite-download/sqlite-amalgamation-" + version);
        if (!Files.exists(amalgamationDir)) {
            throw new RuntimeException("SQLite amalgamation not found. Run 'mvn generate-sources' first.");
        }

        System.out.println("Compiling SQLite for macOS...");
        ProcessBuilder pb = new ProcessBuilder(
            "clang", "-dynamiclib", "-O2",
            "-o", targetLib.toAbsolutePath().toString(),
            amalgamationDir.resolve("sqlite3.c").toAbsolutePath().toString()
        );
        pb.inheritIO();
        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to compile SQLite. Make sure Xcode command line tools are installed.");
        }
        System.out.println("SQLite compiled: " + targetLib.toAbsolutePath());
    }
}
