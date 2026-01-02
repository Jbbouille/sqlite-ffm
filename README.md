# SQLite FFM

SQLite bindings for Java using the Foreign Function & Memory (FFM) API.

## Features

- Zero dependencies at runtime (no JDBC, no JNI)
- Direct native calls via FFM API
- Auto-generated bindings with jextract
- Cross-compilation with Zig for all platforms

## Requirements

- Java 25+
- Maven
- Zig (for native library compilation)

## Build

```bash
# Full build (clean + generate + compile)
mvn clean generate-sources compile

# Release build (all platforms, optimized)
mvn clean generate-sources compile -Prelease
```

## Example

```java
import top.petit.sqlite.Sqlite;

public class Example {
    public static void main(String[] args) throws Exception {
        // Open an in-memory database (auto-closed with try-with-resources)
        try (var db = Sqlite.open(":memory:")) {

            // Create a table
            db.exec("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)");

            // Insert data with prepared statements (auto-closed)
            try (var insert = db.prepareV2("INSERT INTO users (name, age) VALUES (?, ?)")) {
                db.bindText(insert, 1, "Alice");
                db.bindInt(insert, 2, 30);
                db.step(insert);
                db.reset(insert);

                db.bindText(insert, 1, "Bob");
                db.bindInt(insert, 2, 25);
                db.step(insert);
            }

            // Query data (auto-closed)
            try (var select = db.prepareV2("SELECT id, name, age FROM users ORDER BY id")) {
                while (db.step(select).hasRow()) {
                    int id = db.columnInt(select, 0);
                    String name = db.columnText(select, 1);
                    int age = db.columnInt(select, 2);
                    System.out.println(id + ": " + name + " (" + age + ")");
                }
            }

            // Or use exec with callback for simple queries
            db.exec("SELECT name FROM users", (columnNames, values) -> {
                System.out.println("User: " + values[0]);
                return true; // continue iteration
            });
        }
    }
}
```

Output:
```text
1: Alice (30)
2: Bob (25)
User: Alice
User: Bob
```

## Configuration

Edit `pom.xml` properties:

| Property | Description |
|----------|-------------|
| `sqlite.version` | SQLite version in amalgamation format (e.g., 3510100) |
| `sqlite.year` | Year for sqlite.org download URLs |
| `zig.optimize` | Optimization level: Debug, ReleaseSafe, ReleaseFast, ReleaseSmall |
| `zig.allTargets` | Compile for all platforms (true/false) |

## Project Structure

```
src/main/java/top/petit/
├── Main.java                      # Example usage
├── sqlite/                        # SQLite wrapper classes
└── build/
    ├── JextractDownloader.java    # Downloads jextract tool
    └── SqliteNativeCompiler.java  # Cross-compiles SQLite with Zig
target/
├── generated-sources/jextract/    # Generated FFM bindings
├── sqlite-download/               # SQLite amalgamation sources
├── sqlite-native/                 # Native libraries per platform
└── jextract-download/             # jextract tool
```
