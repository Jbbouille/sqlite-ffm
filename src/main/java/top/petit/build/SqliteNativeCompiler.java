package top.petit.build;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SqliteNativeCompiler {

  private static final String[][] TARGETS = {
    // { zigTarget, libraryName, subDir }
    { "x86_64-windows-gnu", "sqlite3.dll", "windows-x64" },
    { "x86-windows-gnu", "sqlite3.dll", "windows-x86" },
    { "x86_64-linux-gnu", "libsqlite3.so", "linux-x64" },
    { "aarch64-linux-gnu", "libsqlite3.so", "linux-arm64" },
    { "x86_64-macos", "libsqlite3.dylib", "macos-x64" },
    { "aarch64-macos", "libsqlite3.dylib", "macos-arm64" },
  };

  // exec-maven-plugin invokes main via reflection using Class.getMethod("main", String[].class),
  // so the full public static void main(String[] args) signature is required — flexible main (JEP 512) won't work here.
  public static void main(String[] args) throws Exception {
    String sqliteVersion = System.getProperty("sqlite.version", "3510100");
    String downloadDir = System.getProperty("sqlite.downloadDir", "target/sqlite-native");
    String optimize = System.getProperty("zig.optimize", "Debug");
    boolean allTargets = Boolean.parseBoolean(System.getProperty("zig.allTargets", "false"));

    System.out.println("=== SqliteNativeCompiler configuration ===");
    System.out.println("  sqlite.version    = " + sqliteVersion);
    System.out.println("  sqlite.downloadDir = " + downloadDir);
    System.out.println("  zig.optimize      = " + optimize);
    System.out.println("  zig.allTargets    = " + allTargets);
    System.out.println("==========================================");

    Path downloadPath = Paths.get(downloadDir);
    Files.createDirectories(downloadPath);

    Path amalgamationDir = Paths.get("target/sqlite-download/sqlite-amalgamation-" + sqliteVersion);
    if (!Files.exists(amalgamationDir)) {
      throw new RuntimeException("SQLite amalgamation not found. Run 'mvn generate-sources' first.");
    }

    if (allTargets) {
      for (String[] target : TARGETS) {
        crossCompileWithZig(downloadPath, amalgamationDir, target[0], target[1], target[2], optimize);
      }
    } else {
      String[] target = getCurrentTarget();
      crossCompileWithZig(downloadPath, amalgamationDir, target[0], target[1], target[2], optimize);
    }

    System.out.println("All SQLite native libraries are ready.");
  }

  private static String[] getCurrentTarget() {
    String os = System.getProperty("os.name").toLowerCase();
    String arch = System.getProperty("os.arch").toLowerCase();

    String zigTarget;
    String libName;
    String subDir;

    if (os.contains("win")) {
      libName = "sqlite3.dll";
      if (arch.equals("amd64") || arch.equals("x86_64")) {
        zigTarget = "x86_64-windows-gnu";
        subDir = "windows-x64";
      } else {
        zigTarget = "x86-windows-gnu";
        subDir = "windows-x86";
      }
    } else if (os.contains("mac")) {
      libName = "libsqlite3.dylib";
      if (arch.equals("aarch64") || arch.equals("arm64")) {
        zigTarget = "aarch64-macos";
        subDir = "macos-arm64";
      } else {
        zigTarget = "x86_64-macos";
        subDir = "macos-x64";
      }
    } else {
      libName = "libsqlite3.so";
      if (arch.equals("aarch64") || arch.equals("arm64")) {
        zigTarget = "aarch64-linux-gnu";
        subDir = "linux-arm64";
      } else {
        zigTarget = "x86_64-linux-gnu";
        subDir = "linux-x64";
      }
    }

    return new String[]{ zigTarget, libName, subDir };
  }

  private static void crossCompileWithZig(Path downloadPath, Path amalgamationDir,
    String zigTarget, String libName, String subDir,
    String optimize) throws Exception {
    Path targetDir = downloadPath.resolve(subDir);
    Files.createDirectories(targetDir);

    Path targetLib = targetDir.resolve(libName);
    if (Files.exists(targetLib)) {
      System.out.println("SQLite native library already exists: " + targetLib.toAbsolutePath());
      return;
    }

    System.out.println("Cross-compiling SQLite for " + zigTarget + " with Zig (" + optimize + ")...");

    Path sqliteSource = amalgamationDir.resolve("sqlite3.c");

    String optFlag = switch (optimize) {
      case "Debug" -> "-O0";
      case "ReleaseSafe" -> "-O2";
      case "ReleaseFast" -> "-O3";
      case "ReleaseSmall" -> "-Os";
      default -> throw new RuntimeException("Unsupported optimization level: " + optimize);
    };

    ProcessBuilder pb = new ProcessBuilder(
      "zig", "cc",
      "-shared", "-fPIC",
      optFlag,
      "-target", zigTarget,
      "-o", targetLib.toAbsolutePath().toString(),
      sqliteSource.toAbsolutePath().toString()
    );
    pb.inheritIO();

    int exitCode = pb.start().waitFor();
    if (exitCode != 0) {
      throw new RuntimeException("Failed to cross-compile SQLite for " + zigTarget + ". Make sure Zig is installed.");
    }

    System.out.println("SQLite compiled: " + targetLib.toAbsolutePath());
  }
}
