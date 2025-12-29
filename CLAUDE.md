# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java Maven project using SQLite via Java's Foreign Function & Memory (FFM) API with jextract-generated bindings.

## Build Commands

**Full build (clean + generate + compile):**
```bash
mvn clean generate-sources compile
```

**Run main:**
```bash
mvn exec:java -Dexec.mainClass="top.petit.Main"
```

**Build and run:**
```bash
mvn clean generate-sources compile exec:java -Dexec.mainClass="top.petit.Main"
```

## Technical Details

- **Java version:** 25
- **Package:** `top.petit`
- **Generated bindings:** `top.petit.sqlite.gen`
- **Build tools:** `top.petit.build`

## Build Process

1. `initialize` - Compile build tools, download jextract
2. `generate-sources` - Download SQLite amalgamation, generate FFM bindings with jextract, download native library

## Key Properties (pom.xml)

| Property | Description |
|----------|-------------|
| `maven.compiler.source` | Java version (also used for jextract) |
| `sqlite.version` | SQLite version in amalgamation format (e.g., 3510100) |
| `sqlite.year` | Year for sqlite.org download URLs |

## Project Structure

```
src/main/java/top/petit/
├── Main.java                      # Example usage
└── build/
    ├── JextractDownloader.java    # Downloads jextract tool
    └── SqliteNativeDownloader.java # Downloads SQLite native library
target/
├── generated-sources/jextract/    # Generated FFM bindings
├── sqlite-download/               # SQLite amalgamation sources
├── sqlite-native/                 # Native library (sqlite3.dll)
└── jextract-download/             # jextract tool
```
