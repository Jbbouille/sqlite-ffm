package top.petit;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.file.Path;

import static top.petit.sqlite.gen.sqlite3_h.*;

public class Main {

    static {
        // Load SQLite native library from target/sqlite-native
        String libName = System.mapLibraryName("sqlite3"); // sqlite3.dll, libsqlite3.so, libsqlite3.dylib
        Path libPath = Path.of("target", "sqlite-native", libName).toAbsolutePath();
        System.load(libPath.toString());
    }

    public static void main(String[] args) {
        // Display SQLite version
        String version = sqlite3_libversion().getString(0);
        int versionNumber = sqlite3_libversion_number();
        System.out.println("SQLite version: " + version + " (" + versionNumber + ")");

        try (var arena = Arena.ofConfined()) {
            // Allocate pointer for database handle
            var dbPtr = arena.allocate(ValueLayout.ADDRESS);

            // Open in-memory database
            int rc = sqlite3_open(arena.allocateFrom(":memory:"), dbPtr);
            if (rc != SQLITE_OK()) {
                System.err.println("Cannot open database");
                return;
            }
            var db = dbPtr.get(ValueLayout.ADDRESS, 0);
            System.out.println("Database opened successfully");

            // Create table
            execute(arena, db, """
                CREATE TABLE users (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    age INTEGER
                )
                """);

            // Insert data
            execute(arena, db, "INSERT INTO users (name, age) VALUES ('Alice', 30)");
            execute(arena, db, "INSERT INTO users (name, age) VALUES ('Bob', 25)");
            execute(arena, db, "INSERT INTO users (name, age) VALUES ('Charlie', 35)");
            System.out.println("Data inserted");

            // Query data
            System.out.println("\nUsers:");
            query(arena, db, "SELECT id, name, age FROM users");

            // Close database
            sqlite3_close(db);
            System.out.println("\nDatabase closed");
        }
    }

    private static void execute(Arena arena, MemorySegment db, String sql) {
        var stmtPtr = arena.allocate(ValueLayout.ADDRESS);

        int rc = sqlite3_prepare_v2(db, arena.allocateFrom(sql), -1, stmtPtr, MemorySegment.NULL);
        if (rc != SQLITE_OK()) {
            System.err.println("Failed to prepare: " + sql);
            return;
        }

        var stmt = stmtPtr.get(ValueLayout.ADDRESS, 0);
        sqlite3_step(stmt);
        sqlite3_finalize(stmt);
    }

    private static void query(Arena arena, MemorySegment db, String sql) {
        var stmtPtr = arena.allocate(ValueLayout.ADDRESS);

        int rc = sqlite3_prepare_v2(db, arena.allocateFrom(sql), -1, stmtPtr, MemorySegment.NULL);
        if (rc != SQLITE_OK()) {
            System.err.println("Failed to prepare query");
            return;
        }

        var stmt = stmtPtr.get(ValueLayout.ADDRESS, 0);

        while (sqlite3_step(stmt) == SQLITE_ROW()) {
            int id = sqlite3_column_int(stmt, 0);
            var namePtr = sqlite3_column_text(stmt, 1);
            String name = namePtr.getString(0);
            int age = sqlite3_column_int(stmt, 2);

            System.out.printf("  %d: %s, %d years old%n", id, name, age);
        }

        sqlite3_finalize(stmt);
    }
}
