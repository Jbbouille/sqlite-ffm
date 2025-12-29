# SQLite FFM

SQLite bindings for Java using the Foreign Function & Memory (FFM) API.

## Features

- Zero dependencies at runtime (no JDBC, no JNI)
- Direct native calls via FFM API
- Auto-generated bindings with jextract
- Automatic download of SQLite native library

## Requirements

- Java 25+
- Maven

## Quick Start

```bash
# Build and run
mvn clean generate-sources compile exec:java -Dexec.mainClass="top.petit.Main"
```

## Example

```java
import static top.petit.sqlite.gen.sqlite3_h.*;

// Get SQLite version
String version = sqlite3_libversion().getString(0);

// Open database
var dbPtr = arena.allocate(ValueLayout.ADDRESS);
sqlite3_open(arena.allocateFrom(":memory:"), dbPtr);
var db = dbPtr.get(ValueLayout.ADDRESS, 0);

// Execute SQL
sqlite3_exec(db, arena.allocateFrom("CREATE TABLE ..."), ...);

// Close
sqlite3_close(db);
```

## Configuration

Edit `pom.xml` properties to change SQLite version:

```xml
<sqlite.version>3510100</sqlite.version>  <!-- 3.51.1 -->
<sqlite.year>2025</sqlite.year>
```
