package top.petit.sqlite.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utility class for loading the SQLite native library.
 */
public final class NativeLibrary {

    private static volatile boolean loaded = false;

    private NativeLibrary() {
    }

    /**
     * Loads the SQLite native library from the specified path.
     *
     * <p>This method is idempotent - calling it multiple times has no effect
     * after the library is loaded.</p>
     *
     * @param path the path to the native library file
     */
    public static synchronized void load(Path path) {
        if (loaded) {
            return;
        }
        System.load(path.toAbsolutePath().toString());
        loaded = true;
    }

    /**
     * Loads the SQLite native library from the classpath.
     *
     * <p>This method is idempotent - calling it multiple times has no effect
     * after the library is loaded.</p>
     *
     * @throws IllegalStateException if the library is not found in the classpath
     */
    public static synchronized void load() {
        if (loaded) {
            return;
        }

        String subDir = getPlatformSubDir();
        String libName = System.mapLibraryName("sqlite3");
        String resourcePath = "/native/" + subDir + "/" + libName;

        try (InputStream is = NativeLibrary.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Native library not found in classpath: " + resourcePath);
            }
            Path tempFile = Files.createTempFile("sqlite3", libName);
            tempFile.toFile().deleteOnExit();
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            System.load(tempFile.toAbsolutePath().toString());
            loaded = true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load native library from classpath", e);
        }
    }

    public static String getPlatformSubDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        String osName;
        if (os.contains("win")) {
            osName = "windows";
        } else if (os.contains("mac")) {
            osName = "macos";
        } else if (os.contains("nux")) {
            osName = "linux";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        String archName = switch (arch) {
            case "amd64", "x86_64" -> "x64";
            case "aarch64", "arm64" -> "arm64";
            case "x86", "i386", "i686" -> "x86";
            default -> throw new UnsupportedOperationException("Unsupported architecture: " + arch);
        };

        return osName + "-" + archName;
    }

    /**
     * Returns {@code true} if the native library has been loaded.
     */
    public static boolean isLoaded() {
        return loaded;
    }
}
