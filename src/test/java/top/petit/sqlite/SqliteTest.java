package top.petit.sqlite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static top.petit.sqlite.util.NativeLibrary.getPlatformSubDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import top.petit.sqlite.exception.SqliteExceptionBind;
import top.petit.sqlite.exception.SqliteExceptionExec;
import top.petit.sqlite.exception.SqliteExceptionOpen;
import top.petit.sqlite.exception.SqliteExceptionPrepareStatement;
import top.petit.sqlite.result.BackupStep;
import top.petit.sqlite.result.Bind;
import top.petit.sqlite.result.ColumnType;
import top.petit.sqlite.result.Exec;
import top.petit.sqlite.result.Open;
import top.petit.sqlite.result.OpenFlag;
import top.petit.sqlite.result.Prepare;
import top.petit.sqlite.result.PrepareFlag;
import top.petit.sqlite.result.Step;
import top.petit.sqlite.util.NativeLibrary;

class SqliteTest {

  static {
    String subDir = getPlatformSubDir();
    String libName = System.mapLibraryName("sqlite3");
    Path libPath = Path.of("target", "sqlite-native", subDir, libName).toAbsolutePath();
    NativeLibrary.load(libPath);
  }

  @Test
  void libversion_returnsVersion() {
    var version = Sqlite.libversion();

    assertThat(version.major())
      .isEqualTo(3);
  }

  @Test
  void libversionNumber_returnsVersionNumber() {
    var versionNumber = Sqlite.libversionNumber();

    assertThat(versionNumber).isGreaterThan(3_000_000);
  }

  @Test
  void open_withMemoryDatabase_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      assertThat(db).isNotNull();
    }
  }

  @Test
  void open_withInvalidPath_throwsException() {
    assertThatThrownBy(() -> Sqlite.open("/nonexistent/path/to/database.db"))
      .isInstanceOf(SqliteExceptionOpen.class)
      .extracting(e -> ((SqliteExceptionOpen) e).result())
      .isEqualTo(Open.CANTOPEN);
  }

  @Test
  void prepareV2_withValidSql_returnsStmt() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1")) {
        assertThat(stmt).isNotNull();
      }
    }
  }

  @Test
  void prepareV2_withInvalidSql_throwsException() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      assertThatThrownBy(() -> db.prepareV2("INVALID SQL"))
        .isInstanceOf(SqliteExceptionPrepareStatement.class)
        .extracting(e -> ((SqliteExceptionPrepareStatement) e).result())
        .isEqualTo(Prepare.ERROR);
    }
  }

  @Test
  void step_withSelectStatement_returnsRow() throws Exception {
    try (var db = Sqlite.open(":memory:");
         var stmt = db.prepareV2("SELECT 1")) {
        var stepResult = db.step(stmt);
        assertThat(stepResult).isEqualTo(Step.ROW);
    }
  }

  @Test
  void step_afterAllRows_returnsDone() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1")) {
        db.step(stmt);
        var stepResult = db.step(stmt);

        assertThat(stepResult).isEqualTo(Step.DONE);
      }
    }
  }

  @Test
  void close_afterStep_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      var stmt = db.prepareV2("SELECT 1");
      db.step(stmt);

      assertThatCode(stmt::close).doesNotThrowAnyException();
    }
  }

  @Test
  void finalize_closesStatement() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      var stmt = db.prepareV2("SELECT 1");
      db.step(stmt);

      assertThatCode(() -> db.finalize(stmt)).doesNotThrowAnyException();
    }
  }

  @Test
  void createTableAndInsert_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      // Create table
      try (var create = db.prepareV2("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT)")) {
        assertThat(db.step(create)).isEqualTo(Step.DONE);
      }

      // Insert
      try (var insert = db.prepareV2("INSERT INTO users (name) VALUES ('Alice')")) {
        assertThat(db.step(insert)).isEqualTo(Step.DONE);
      }

      // Select
      try (var select = db.prepareV2("SELECT id, name FROM users")) {
        assertThat(db.step(select)).isEqualTo(Step.ROW);

        int id = db.columnInt(select, 0);
        String name = db.columnText(select, 1);

        assertThat(id).isEqualTo(1);
        assertThat(name).isEqualTo("Alice");
      }
    }
  }

  @Test
  void bindInt_withValidIndex_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value INTEGER)");

      try (var insert = db.prepareV2("INSERT INTO test (value) VALUES (?)")) {
        db.bindInt(insert, 1, 42);
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT value FROM test")) {
        assertThat(db.step(select)).isEqualTo(Step.ROW);
        assertThat(db.columnInt(select, 0)).isEqualTo(42);
      }
    }
  }

  @Test
  void bindInt_withInvalidIndex_throwsException() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT ?")) {
        assertThatThrownBy(() -> db.bindInt(stmt, 99, 42))
          .isInstanceOf(SqliteExceptionBind.class)
          .extracting(e -> ((SqliteExceptionBind) e).result())
          .isEqualTo(Bind.RANGE);
      }
    }
  }

  @Test
  void bindLong_withValidIndex_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value INTEGER)");

      try (var insert = db.prepareV2("INSERT INTO test (value) VALUES (?)")) {
        db.bindLong(insert, 1, 9_000_000_000L);
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT value FROM test")) {
        assertThat(db.step(select)).isEqualTo(Step.ROW);
        assertThat(db.columnLong(select, 0)).isEqualTo(9_000_000_000L);
      }
    }
  }

  @Test
  void bindDouble_withValidIndex_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value REAL)");

      try (var insert = db.prepareV2("INSERT INTO test (value) VALUES (?)")) {
        db.bindDouble(insert, 1, 3.14159);
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT value FROM test")) {
        assertThat(db.step(select)).isEqualTo(Step.ROW);
        assertThat(db.columnDouble(select, 0)).isEqualTo(3.14159);
      }
    }
  }

  @Test
  void bindText_withValidIndex_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value TEXT)");

      try (var insert = db.prepareV2("INSERT INTO test (value) VALUES (?)")) {
        db.bindText(insert, 1, "Hello, World!");
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT value FROM test")) {
        assertThat(db.step(select)).isEqualTo(Step.ROW);
        assertThat(db.columnText(select, 0)).isEqualTo("Hello, World!");
      }
    }
  }

  @Test
  void bindBlob_withValidIndex_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value BLOB)");

      byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05 };
      try (var insert = db.prepareV2("INSERT INTO test (value) VALUES (?)")) {
        db.bindBlob(insert, 1, data);
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT value FROM test")) {
        assertThat(db.step(select)).isEqualTo(Step.ROW);
        assertThat(db.columnBlob(select, 0)).isEqualTo(data);
      }
    }
  }

  @Test
  void bindBlob64_withValidIndex_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value BLOB)");

      byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05 };
      try (var insert = db.prepareV2("INSERT INTO test (value) VALUES (?)")) {
        db.bindBlob64(insert, 1, data);
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT value FROM test")) {
        assertThat(db.step(select)).isEqualTo(Step.ROW);
        assertThat(db.columnBlob(select, 0)).isEqualTo(data);
      }
    }
  }

  @Test
  void bindNull_withValidIndex_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value TEXT)");

      try (var insert = db.prepareV2("INSERT INTO test (value) VALUES (?)")) {
        db.bindNull(insert, 1);
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT value FROM test")) {
        assertThat(db.step(select)).isEqualTo(Step.ROW);
        assertThat(db.columnType(select, 0)).isEqualTo(ColumnType.NULL);
      }
    }
  }

  @Test
  void bindMultipleParameters_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE users (id INTEGER, name TEXT, score REAL)");

      try (var insert = db.prepareV2("INSERT INTO users (id, name, score) VALUES (?, ?, ?)")) {
        db.bindInt(insert, 1, 1);
        db.bindText(insert, 2, "Alice");
        db.bindDouble(insert, 3, 95.5);
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT id, name, score FROM users")) {
        assertThat(db.step(select)).isEqualTo(Step.ROW);
        assertThat(db.columnInt(select, 0)).isEqualTo(1);
        assertThat(db.columnText(select, 1)).isEqualTo("Alice");
        assertThat(db.columnDouble(select, 2)).isEqualTo(95.5);
      }
    }
  }

  @Test
  void columnCount_returnsNumberOfColumns() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1, 2, 3")) {
        assertThat(db.columnCount(stmt)).isEqualTo(3);
      }
    }
  }

  @Test
  void columnType_returnsCorrectTypes() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (i INTEGER, r REAL, t TEXT, b BLOB)");

      try (var insert = db.prepareV2("INSERT INTO test VALUES (?, ?, ?, ?)")) {
        db.bindInt(insert, 1, 42);
        db.bindDouble(insert, 2, 3.14);
        db.bindText(insert, 3, "hello");
        db.bindBlob(insert, 4, new byte[]{ 1, 2, 3 });
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT i, r, t, b FROM test")) {
        db.step(select);

        assertThat(db.columnType(select, 0)).isEqualTo(ColumnType.INTEGER);
        assertThat(db.columnType(select, 1)).isEqualTo(ColumnType.FLOAT);
        assertThat(db.columnType(select, 2)).isEqualTo(ColumnType.TEXT);
        assertThat(db.columnType(select, 3)).isEqualTo(ColumnType.BLOB);
      }
    }
  }

  @Test
  void columnType_withNull_returnsNull() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT NULL")) {
        db.step(stmt);
        assertThat(db.columnType(stmt, 0)).isEqualTo(ColumnType.NULL);
      }
    }
  }

  @Test
  void columnInt_returnsIntegerValue() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 42")) {
        db.step(stmt);
        assertThat(db.columnInt(stmt, 0)).isEqualTo(42);
      }
    }
  }

  @Test
  void columnLong_returnsLongValue() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 9000000000")) {
        db.step(stmt);
        assertThat(db.columnLong(stmt, 0)).isEqualTo(9_000_000_000L);
      }
    }
  }

  @Test
  void columnDouble_returnsDoubleValue() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 3.14159")) {
        db.step(stmt);
        assertThat(db.columnDouble(stmt, 0)).isEqualTo(3.14159);
      }
    }
  }

  @Test
  void columnText_returnsTextValue() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 'Hello, World!'")) {
        db.step(stmt);
        assertThat(db.columnText(stmt, 0)).isEqualTo("Hello, World!");
      }
    }
  }

  @Test
  void columnText_withNull_returnsNull() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT NULL")) {
        db.step(stmt);
        assertThat(db.columnText(stmt, 0)).isNull();
      }
    }
  }

  @Test
  void columnBlob_returnsBlobValue() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value BLOB)");

      byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05 };
      try (var insert = db.prepareV2("INSERT INTO test (value) VALUES (?)")) {
        db.bindBlob(insert, 1, data);
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT value FROM test")) {
        db.step(select);
        assertThat(db.columnBlob(select, 0)).isEqualTo(data);
      }
    }
  }

  @Test
  void columnBlob_withNull_returnsNull() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT NULL")) {
        db.step(stmt);
        assertThat(db.columnBlob(stmt, 0)).isNull();
      }
    }
  }

  @Test
  void columnMethods_withMultipleColumns_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE users (id INTEGER, name TEXT, score REAL)");

      try (var insert = db.prepareV2("INSERT INTO users VALUES (?, ?, ?)")) {
        db.bindInt(insert, 1, 1);
        db.bindText(insert, 2, "Alice");
        db.bindDouble(insert, 3, 95.5);
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT id, name, score FROM users")) {
        db.step(select);

        assertThat(db.columnCount(select)).isEqualTo(3);
        assertThat(db.columnInt(select, 0)).isEqualTo(1);
        assertThat(db.columnText(select, 1)).isEqualTo("Alice");
        assertThat(db.columnDouble(select, 2)).isEqualTo(95.5);
      }
    }
  }

  @Test
  void exec_withCreateTable_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      assertThatCode(() -> db.exec("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT)"))
        .doesNotThrowAnyException();
    }
  }

  @Test
  void exec_withInsert_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT)");
      db.exec("INSERT INTO users (name) VALUES ('Alice')");

      try (var stmt = db.prepareV2("SELECT name FROM users")) {
        db.step(stmt);
        assertThat(db.columnText(stmt, 0)).isEqualTo("Alice");
      }
    }
  }

  @Test
  void exec_withMultipleStatements_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("""
        CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT);
        INSERT INTO users (name) VALUES ('Alice');
        INSERT INTO users (name) VALUES ('Bob');
        """);

      try (var stmt = db.prepareV2("SELECT COUNT(*) FROM users")) {
        db.step(stmt);
        assertThat(db.columnInt(stmt, 0)).isEqualTo(2);
      }
    }
  }

  @Test
  void exec_withInvalidSql_throwsException() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      assertThatThrownBy(() -> db.exec("INVALID SQL"))
        .isInstanceOf(SqliteExceptionExec.class)
        .extracting(e -> ((SqliteExceptionExec) e).result())
        .isEqualTo(Exec.ERROR);
    }
  }

  @Test
  void exec_withInvalidSql_providesErrorMessage() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      assertThatThrownBy(() -> db.exec("INVALID SQL"))
        .isInstanceOf(SqliteExceptionExec.class)
        .satisfies(e -> {
          var sqliteEx = (SqliteExceptionExec) e;
          assertThat(sqliteEx.errorMessage()).isNotNull();
          assertThat(sqliteEx.errorMessage()).containsIgnoringCase("syntax");
        });
    }
  }

  @Test
  void exec_withConstraintViolation_throwsException() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT UNIQUE)");
      db.exec("INSERT INTO users (name) VALUES ('Alice')");

      assertThatThrownBy(() -> db.exec("INSERT INTO users (name) VALUES ('Alice')"))
        .isInstanceOf(SqliteExceptionExec.class)
        .extracting(e -> ((SqliteExceptionExec) e).result())
        .isEqualTo(Exec.CONSTRAINT);
    }
  }

  @Test
  void execWithCallback_collectsAllRows() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE users (id INTEGER, name TEXT)");
      db.exec("INSERT INTO users VALUES (1, 'Alice'), (2, 'Bob'), (3, 'Charlie')");

      List<String> names = new ArrayList<>();
      db.exec("SELECT name FROM users ORDER BY id", (columnNames, values) -> {
        names.add(values[0]);
        return true;
      });

      assertThat(names).containsExactly("Alice", "Bob", "Charlie");
    }
  }

  @Test
  void execWithCallback_providesColumnNames() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE users (id INTEGER, name TEXT)");
      db.exec("INSERT INTO users VALUES (1, 'Alice')");

      List<String> columnNamesList = new ArrayList<>();
      db.exec("SELECT id, name FROM users", (columnNames, values) -> {
        columnNamesList.addAll(List.of(columnNames));
        return true;
      });

      assertThat(columnNamesList).containsExactly("id", "name");
    }
  }

  @Test
  void execWithCallback_canAbortEarly() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE users (id INTEGER, name TEXT)");
      db.exec("INSERT INTO users VALUES (1, 'Alice'), (2, 'Bob'), (3, 'Charlie')");

      List<String> names = new ArrayList<>();
      db.exec("SELECT name FROM users ORDER BY id", (columnNames, values) -> {
        names.add(values[0]);
        return names.size() < 2; // Stop after 2 rows
      });

      assertThat(names).containsExactly("Alice", "Bob");
    }
  }

  @Test
  void execWithCallback_handlesNullValues() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE users (id INTEGER, name TEXT)");
      db.exec("INSERT INTO users VALUES (1, NULL)");

      List<String> values = new ArrayList<>();
      db.exec("SELECT name FROM users", (columnNames, vals) -> {
        values.add(vals[0]);
        return true;
      });

      assertThat(values).containsExactly((String) null);
    }
  }

  @Test
  void execWithCallback_handlesMultipleColumns() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE users (id INTEGER, name TEXT, age INTEGER)");
      db.exec("INSERT INTO users VALUES (1, 'Alice', 30)");

      List<String[]> rows = new ArrayList<>();
      db.exec("SELECT id, name, age FROM users", (columnNames, values) -> {
        rows.add(values.clone());
        return true;
      });

      assertThat(rows).hasSize(1);
      assertThat(rows.get(0)).containsExactly("1", "Alice", "30");
    }
  }

  // ========== reset / clearBindings tests ==========

  @Test
  void reset_allowsStatementReuse() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value INTEGER)");

      try (var insert = db.prepareV2("INSERT INTO test (value) VALUES (?)")) {
        db.bindInt(insert, 1, 1);
        db.step(insert);
        db.reset(insert);

        db.bindInt(insert, 1, 2);
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT COUNT(*) FROM test")) {
        db.step(select);
        assertThat(db.columnInt(select, 0)).isEqualTo(2);
      }
    }
  }

  @Test
  void clearBindings_clearsAllBindings() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value TEXT)");

      try (var insert = db.prepareV2("INSERT INTO test (value) VALUES (?)")) {
        db.bindText(insert, 1, "Hello");
        db.clearBindings(insert);
        db.step(insert);
      }

      try (var select = db.prepareV2("SELECT value FROM test")) {
        db.step(select);
        assertThat(db.columnType(select, 0)).isEqualTo(ColumnType.NULL);
      }
    }
  }

  // ========== changes / totalChanges / lastInsertRowid tests ==========

  @Test
  void changes_returnsNumberOfModifiedRows() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (id INTEGER, value TEXT)");
      db.exec("INSERT INTO test VALUES (1, 'a'), (2, 'b'), (3, 'c')");

      db.exec("UPDATE test SET value = 'x' WHERE id <= 2");

      assertThat(db.changes()).isEqualTo(2);
    }
  }

  @Test
  void changes64_returnsNumberOfModifiedRows() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (id INTEGER, value TEXT)");
      db.exec("INSERT INTO test VALUES (1, 'a'), (2, 'b'), (3, 'c')");

      db.exec("DELETE FROM test");

      assertThat(db.changes64()).isEqualTo(3L);
    }
  }

  @Test
  void totalChanges_returnsTotalModifiedRows() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value TEXT)");

      db.exec("INSERT INTO test VALUES ('a')");
      db.exec("INSERT INTO test VALUES ('b')");
      db.exec("INSERT INTO test VALUES ('c')");

      assertThat(db.totalChanges()).isGreaterThanOrEqualTo(3);
    }
  }

  @Test
  void totalChanges64_returnsTotalModifiedRows() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value TEXT)");

      db.exec("INSERT INTO test VALUES ('a'), ('b'), ('c')");
      db.exec("DELETE FROM test WHERE value = 'a'");

      assertThat(db.totalChanges64()).isGreaterThanOrEqualTo(4L);
    }
  }

  @Test
  void lastInsertRowid_returnsLastRowid() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (id INTEGER PRIMARY KEY, value TEXT)");

      db.exec("INSERT INTO test (value) VALUES ('a')");
      assertThat(db.lastInsertRowid()).isEqualTo(1L);

      db.exec("INSERT INTO test (value) VALUES ('b')");
      assertThat(db.lastInsertRowid()).isEqualTo(2L);

      db.exec("INSERT INTO test (value) VALUES ('c')");
      assertThat(db.lastInsertRowid()).isEqualTo(3L);
    }
  }

  // ========== error functions tests ==========

  @Test
  void errcode_returnsZeroOnSuccess() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("SELECT 1");

      assertThat(db.errcode()).isEqualTo(0);
    }
  }

  @Test
  void errmsg_returnsErrorMessage() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try {
        db.exec("INVALID SQL");
      } catch (Exception ignored) {
      }

      assertThat(db.errmsg()).containsIgnoringCase("syntax");
    }
  }

  @Test
  void extendedErrcode_returnsExtendedErrorCode() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("SELECT 1");

      assertThat(db.extendedErrcode()).isEqualTo(0);
    }
  }

  // ========== column metadata tests ==========

  @Test
  void columnName_returnsColumnName() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE users (id INTEGER, name TEXT)");
      db.exec("INSERT INTO users VALUES (1, 'Alice')");

      try (var stmt = db.prepareV2("SELECT id, name FROM users")) {
        db.step(stmt);
        assertThat(db.columnName(stmt, 0)).isEqualTo("id");
        assertThat(db.columnName(stmt, 1)).isEqualTo("name");
      }
    }
  }

  @Test
  void columnName_withAlias_returnsAlias() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1 AS my_column")) {
        assertThat(db.columnName(stmt, 0)).isEqualTo("my_column");
      }
    }
  }

  @Test
  void columnDecltype_returnsDeclaredType() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (i INTEGER, r REAL, t TEXT, b BLOB)");

      try (var stmt = db.prepareV2("SELECT i, r, t, b FROM test")) {
        assertThat(db.columnDecltype(stmt, 0)).isEqualTo("INTEGER");
        assertThat(db.columnDecltype(stmt, 1)).isEqualTo("REAL");
        assertThat(db.columnDecltype(stmt, 2)).isEqualTo("TEXT");
        assertThat(db.columnDecltype(stmt, 3)).isEqualTo("BLOB");
      }
    }
  }

  @Test
  void columnDecltype_withExpression_returnsNull() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1 + 2")) {
        assertThat(db.columnDecltype(stmt, 0)).isNull();
      }
    }
  }

  @Test
  void dataCount_returnsColumnsInCurrentRow() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1, 2, 3")) {
        assertThat(db.dataCount(stmt)).isEqualTo(0); // Before step

        db.step(stmt);
        assertThat(db.dataCount(stmt)).isEqualTo(3); // After step with row

        db.step(stmt); // DONE
        assertThat(db.dataCount(stmt)).isEqualTo(0); // After DONE
      }
    }
  }

  // ========== bind parameter functions tests ==========

  @Test
  void bindParameterCount_returnsNumberOfParameters() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT ?, ?, ?")) {
        assertThat(db.bindParameterCount(stmt)).isEqualTo(3);
      }
    }
  }

  @Test
  void bindParameterCount_withNoParameters_returnsZero() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1")) {
        assertThat(db.bindParameterCount(stmt)).isEqualTo(0);
      }
    }
  }

  @Test
  void bindParameterIndex_returnsIndexForNamedParameter() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT :first, :second, :third")) {
        assertThat(db.bindParameterIndex(stmt, ":first")).isEqualTo(1);
        assertThat(db.bindParameterIndex(stmt, ":second")).isEqualTo(2);
        assertThat(db.bindParameterIndex(stmt, ":third")).isEqualTo(3);
      }
    }
  }

  @Test
  void bindParameterIndex_withUnknownName_returnsZero() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT :known")) {
        assertThat(db.bindParameterIndex(stmt, ":unknown")).isEqualTo(0);
      }
    }
  }

  @Test
  void bindParameterName_returnsNameForIndex() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT :alpha, @beta, $gamma")) {
        assertThat(db.bindParameterName(stmt, 1)).isEqualTo(":alpha");
        assertThat(db.bindParameterName(stmt, 2)).isEqualTo("@beta");
        assertThat(db.bindParameterName(stmt, 3)).isEqualTo("$gamma");
      }
    }
  }

  @Test
  void bindParameterName_withAnonymousParameter_returnsNull() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT ?")) {
        assertThat(db.bindParameterName(stmt, 1)).isNull();
      }
    }
  }

  // ========== utility functions tests ==========

  @Test
  void busyTimeout_setsTimeout() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      assertThatCode(() -> db.busyTimeout(5000)).doesNotThrowAnyException();
    }
  }

  @Test
  void getAutocommit_returnsTrueByDefault() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      assertThat(db.getAutocommit()).isTrue();
    }
  }

  @Test
  void getAutocommit_returnsFalseInTransaction() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("BEGIN");

      assertThat(db.getAutocommit()).isFalse();

      db.exec("ROLLBACK");
      assertThat(db.getAutocommit()).isTrue();
    }
  }

  @Test
  void interrupt_interruptsLongRunningQuery() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      assertThatCode(() -> db.interrupt()).doesNotThrowAnyException();
    }
  }

  @Test
  void isInterrupted_returnsFalseByDefault() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      assertThat(db.isInterrupted()).isFalse();
    }
  }

  @Test
  void sql_returnsSqlText() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1, 2, 3")) {
        assertThat(db.sql(stmt)).isEqualTo("SELECT 1, 2, 3");
      }
    }
  }

  @Test
  void stmtReadonly_returnsTrueForSelect() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1")) {
        assertThat(db.stmtReadonly(stmt)).isTrue();
      }
    }
  }

  @Test
  void stmtReadonly_returnsFalseForInsert() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE test (value INTEGER)");

      try (var stmt = db.prepareV2("INSERT INTO test VALUES (1)")) {
        assertThat(db.stmtReadonly(stmt)).isFalse();
      }
    }
  }

  @Test
  void stmtBusy_returnsFalseBeforeStep() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1")) {
        assertThat(db.stmtBusy(stmt)).isFalse();
      }
    }
  }

  @Test
  void stmtBusy_returnsTrueAfterStepWithRow() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1")) {
        db.step(stmt);
        assertThat(db.stmtBusy(stmt)).isTrue();
      }
    }
  }

  @Test
  void stmtBusy_returnsFalseAfterDone() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV2("SELECT 1")) {
        db.step(stmt); // ROW
        db.step(stmt); // DONE
        assertThat(db.stmtBusy(stmt)).isFalse();
      }
    }
  }

  // ========== static utility functions tests ==========

  @Test
  void sourceid_returnsSourceId() {
    var sourceId = Sqlite.sourceid();

    assertThat(sourceId).isNotNull();
    assertThat(sourceId).isNotEmpty();
  }

  @Test
  void threadsafe_returnsThreadSafetyLevel() {
    var level = Sqlite.threadsafe();

    assertThat(level).isBetween(0, 2);
  }

  @Test
  void complete_returnsTrueForCompleteStatement() {
    assertThat(Sqlite.complete("SELECT 1;")).isTrue();
  }

  @Test
  void complete_returnsFalseForIncompleteStatement() {
    assertThat(Sqlite.complete("SELECT")).isFalse();
  }

  @Test
  void complete_returnsTrueForMultipleStatements() {
    assertThat(Sqlite.complete("SELECT 1; SELECT 2;")).isTrue();
  }

  // ========== prepare / prepareV3 tests ==========

  @Test
  @SuppressWarnings("deprecation")
  void prepare_withValidSql_returnsStmt() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepare("SELECT 1")) {
        assertThat(stmt).isNotNull();
      }
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void prepare_withInvalidSql_throwsException() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      assertThatThrownBy(() -> db.prepare("INVALID SQL"))
        .isInstanceOf(SqliteExceptionPrepareStatement.class)
        .extracting(e -> ((SqliteExceptionPrepareStatement) e).result())
        .isEqualTo(Prepare.ERROR);
    }
  }

  @Test
  void prepareV3_withNoFlags_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV3("SELECT 1")) {
        assertThat(stmt).isNotNull();
        db.step(stmt);
        assertThat(db.columnInt(stmt, 0)).isEqualTo(1);
      }
    }
  }

  @Test
  void prepareV3_withPersistentFlag_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV3("SELECT 1", PrepareFlag.PERSISTENT)) {
        assertThat(stmt).isNotNull();
        db.step(stmt);
        assertThat(db.columnInt(stmt, 0)).isEqualTo(1);
      }
    }
  }

  @Test
  void prepareV3_withMultipleFlags_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      try (var stmt = db.prepareV3("SELECT 1", PrepareFlag.PERSISTENT, PrepareFlag.NO_VTAB)) {
        assertThat(stmt).isNotNull();
        db.step(stmt);
        assertThat(db.columnInt(stmt, 0)).isEqualTo(1);
      }
    }
  }

  @Test
  void prepareV3_withInvalidSql_throwsException() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      assertThatThrownBy(() -> db.prepareV3("INVALID SQL"))
        .isInstanceOf(SqliteExceptionPrepareStatement.class)
        .extracting(e -> ((SqliteExceptionPrepareStatement) e).result())
        .isEqualTo(Prepare.ERROR);
    }
  }

  @Test
  void prepareFlag_combine_combinesFlags() {
    var combined = PrepareFlag.combine(PrepareFlag.PERSISTENT, PrepareFlag.NO_VTAB);

    assertThat(combined).isEqualTo(PrepareFlag.PERSISTENT.flag() | PrepareFlag.NO_VTAB.flag());
  }

  @Test
  void prepareFlag_combine_withNoFlags_returnsZero() {
    var combined = PrepareFlag.combine();

    assertThat(combined).isEqualTo(0);
  }

  // ========== openV2 tests ==========

  @Test
  void openV2_withReadWriteCreate_succeeds() throws Exception {
    try (var db = Sqlite.openV2(":memory:", OpenFlag.READWRITE, OpenFlag.CREATE)) {
      assertThat(db).isNotNull();
      db.exec("CREATE TABLE test (id INTEGER)");
    }
  }

  @Test
  void openV2_withReadOnly_succeeds() throws Exception {
    try (var db = Sqlite.openV2(":memory:", OpenFlag.READWRITE, OpenFlag.MEMORY)) {
      assertThat(db).isNotNull();
    }
  }

  @Test
  void openV2_withMemoryFlag_succeeds() throws Exception {
    try (var db = Sqlite.openV2(":memory:", OpenFlag.READWRITE, OpenFlag.MEMORY)) {
      db.exec("CREATE TABLE test (id INTEGER)");
      db.exec("INSERT INTO test VALUES (1)");

      try (var stmt = db.prepareV2("SELECT * FROM test")) {
        assertThat(db.step(stmt)).isEqualTo(Step.ROW);
      }
    }
  }

  @Test
  void openFlag_combine_combinesFlags() {
    var combined = OpenFlag.combine(OpenFlag.READWRITE, OpenFlag.CREATE);

    assertThat(combined).isEqualTo(OpenFlag.READWRITE.flag() | OpenFlag.CREATE.flag());
  }

  // ========== Blob I/O tests ==========

  @Test
  void blobOpen_withValidBlob_succeeds() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)");
      db.exec("INSERT INTO t (data) VALUES (zeroblob(100))");

      try (var blob = db.blobOpen("main", "t", "data", 1, true)) {
        assertThat(blob).isNotNull();
      }
    }
  }

  @Test
  void blobClose_closesBlob() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)");
      db.exec("INSERT INTO t (data) VALUES (zeroblob(100))");

      var blob = db.blobOpen("main", "t", "data", 1, true);
      assertThat(blob).isNotNull();

      assertThatCode(() -> db.blobClose(blob)).doesNotThrowAnyException();
    }
  }

  @Test
  void blobBytes_returnsCorrectSize() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)");
      db.exec("INSERT INTO t (data) VALUES (zeroblob(256))");

      try (var blob = db.blobOpen("main", "t", "data", 1, false)) {
        assertThat(db.blobBytes(blob)).isEqualTo(256);
      }
    }
  }

  @Test
  void blobWrite_writesData() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)");
      db.exec("INSERT INTO t (data) VALUES (zeroblob(10))");

      try (var blob = db.blobOpen("main", "t", "data", 1, true)) {
        db.blobWrite(blob, new byte[]{ 1, 2, 3, 4, 5 }, 0);
      }

      // Verify with regular query
      try (var stmt = db.prepareV2("SELECT data FROM t")) {
        db.step(stmt);
        byte[] data = db.columnBlob(stmt, 0);
        assertThat(data[0]).isEqualTo((byte) 1);
        assertThat(data[4]).isEqualTo((byte) 5);
      }
    }
  }

  @Test
  void blobRead_readsData() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)");
      db.exec("INSERT INTO t (data) VALUES (zeroblob(10))");

      try (var blob = db.blobOpen("main", "t", "data", 1, true)) {
        db.blobWrite(blob, new byte[]{ 10, 20, 30, 40, 50 }, 2);

        byte[] data = db.blobRead(blob, 5, 2);
        assertThat(data).containsExactly(10, 20, 30, 40, 50);
      }
    }
  }

  @Test
  void blobReopen_movesToDifferentRow() throws Exception {
    try (var db = Sqlite.open(":memory:")) {
      db.exec("CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)");
      db.exec("INSERT INTO t (data) VALUES (zeroblob(10))");
      db.exec("INSERT INTO t (data) VALUES (zeroblob(10))");

      try (var blob = db.blobOpen("main", "t", "data", 1, true)) {
        db.blobWrite(blob, new byte[]{ 1 }, 0);

        db.blobReopen(blob, 2);
        db.blobWrite(blob, new byte[]{ 2 }, 0);
      }

      // Verify both rows
      try (var stmt = db.prepareV2("SELECT data FROM t ORDER BY id")) {
        db.step(stmt);
        assertThat(db.columnBlob(stmt, 0)[0]).isEqualTo((byte) 1);

        db.step(stmt);
        assertThat(db.columnBlob(stmt, 0)[0]).isEqualTo((byte) 2);
      }
    }
  }

  // ========== Backup API tests ==========

  @Test
  void backupInit_succeeds() throws Exception {
    try (var src = Sqlite.open(":memory:");
      var dst = Sqlite.open(":memory:")) {

      src.exec("CREATE TABLE test (id INTEGER, name TEXT)");
      src.exec("INSERT INTO test VALUES (1, 'Alice')");

      try (var backup = src.backupInit(dst, "main", "main")) {
        assertThat(backup).isNotNull();
      }
    }
  }

  @Test
  void backupFinish_finishesBackup() throws Exception {
    try (var src = Sqlite.open(":memory:");
      var dst = Sqlite.open(":memory:")) {

      src.exec("CREATE TABLE test (id INTEGER, name TEXT)");
      src.exec("INSERT INTO test VALUES (1, 'Alice')");

      var backup = src.backupInit(dst, "main", "main");
      src.backupStep(backup, -1);

      assertThatCode(() -> src.backupFinish(backup)).doesNotThrowAnyException();
    }
  }

  @Test
  void backupStep_copiesPages() throws Exception {
    try (var src = Sqlite.open(":memory:");
      var dst = Sqlite.open(":memory:")) {

      src.exec("CREATE TABLE test (id INTEGER, name TEXT)");
      src.exec("INSERT INTO test VALUES (1, 'Alice'), (2, 'Bob')");

      try (var backup = src.backupInit(dst, "main", "main")) {
        var result = src.backupStep(backup, -1); // Copy all pages
        assertThat(result).isEqualTo(BackupStep.DONE);
      }

      // Verify data was copied
      try (var stmt = dst.prepareV2("SELECT COUNT(*) FROM test")) {
        dst.step(stmt);
        assertThat(dst.columnInt(stmt, 0)).isEqualTo(2);
      }
    }
  }

  @Test
  void backupRemaining_returnsRemainingPages() throws Exception {
    try (var src = Sqlite.open(":memory:");
      var dst = Sqlite.open(":memory:")) {

      src.exec("CREATE TABLE test (id INTEGER, data TEXT)");
      for (int i = 0; i < 1000; i++) {
        src.exec("INSERT INTO test VALUES (" + i + ", '" + "x".repeat(100) + "')");
      }

      try (var backup = src.backupInit(dst, "main", "main")) {
        // Must call backupStep at least once before pagecount/remaining return valid values
        src.backupStep(backup, 1);

        int total = src.backupPagecount(backup);
        assertThat(total).isGreaterThan(0);

        int remaining = src.backupRemaining(backup);
        assertThat(remaining).isGreaterThanOrEqualTo(0);
        assertThat(remaining).isLessThanOrEqualTo(total);

        // Complete the backup
        src.backupStep(backup, -1);

        remaining = src.backupRemaining(backup);
        assertThat(remaining).isEqualTo(0);
      }
    }
  }

  @Test
  void backupPagecount_returnsTotalPages() throws Exception {
    try (var src = Sqlite.open(":memory:");
      var dst = Sqlite.open(":memory:")) {

      src.exec("CREATE TABLE test (id INTEGER, data TEXT)");
      for (int i = 0; i < 1000; i++) {
        src.exec("INSERT INTO test VALUES (" + i + ", '" + "x".repeat(100) + "')");
      }

      try (var backup = src.backupInit(dst, "main", "main")) {
        // Must call backupStep at least once before pagecount returns valid value
        src.backupStep(backup, 1);

        int pagecount = src.backupPagecount(backup);
        assertThat(pagecount).isGreaterThan(0);

        // Complete the backup
        src.backupStep(backup, -1);
      }
    }
  }

  @Test
  void backup_incrementalCopy_succeeds() throws Exception {
    try (var src = Sqlite.open(":memory:");
      var dst = Sqlite.open(":memory:")) {

      src.exec("CREATE TABLE test (id INTEGER, data TEXT)");
      for (int i = 0; i < 50; i++) {
        src.exec("INSERT INTO test VALUES (" + i + ", 'data" + i + "')");
      }

      try (var backup = src.backupInit(dst, "main", "main")) {
        // Copy in chunks
        BackupStep result;
        int iterations = 0;
        do {
          result = src.backupStep(backup, 5);
          iterations++;
        } while (result.needsMoreWork() && iterations < 100);

        assertThat(result).isEqualTo(BackupStep.DONE);
      }

      // Verify
      try (var stmt = dst.prepareV2("SELECT COUNT(*) FROM test")) {
        dst.step(stmt);
        assertThat(dst.columnInt(stmt, 0)).isEqualTo(50);
      }
    }
  }

  @Test
  void walHook_isCalledAfterWalWrite() throws Exception {
    Path dbFile = Files.createTempFile("sqlite-wal-test", ".db");
    try (var db = Sqlite.open(dbFile.toString())) {
      db.exec("PRAGMA journal_mode=WAL");

      List<String> calls = new ArrayList<>();
      db.walHook((dbName, pageCount) -> {
        calls.add(dbName + ":" + pageCount);
        return 0;
      });

      db.exec("CREATE TABLE t (id INTEGER)");
      db.exec("INSERT INTO t VALUES (1)");

      assertThat(calls).hasSize(2);
      assertThat(calls.get(0)).startsWith("main:");
      assertThat(calls.get(1)).startsWith("main:");
    } finally {
      Files.deleteIfExists(dbFile);
      Files.deleteIfExists(dbFile.resolveSibling(dbFile.getFileName() + "-wal"));
      Files.deleteIfExists(dbFile.resolveSibling(dbFile.getFileName() + "-shm"));
    }
  }

  @Test
  void walHook_pageCountIsPositive() throws Exception {
    Path dbFile = Files.createTempFile("sqlite-wal-test", ".db");
    try (var db = Sqlite.open(dbFile.toString())) {
      db.exec("PRAGMA journal_mode=WAL");

      List<Integer> pageCounts = new ArrayList<>();
      db.walHook((dbName, pageCount) -> {
        pageCounts.add(pageCount);
        return 0;
      });

      db.exec("CREATE TABLE t (id INTEGER)");

      assertThat(pageCounts).hasSize(1);
      assertThat(pageCounts.get(0)).isGreaterThan(0);
    } finally {
      Files.deleteIfExists(dbFile);
      Files.deleteIfExists(dbFile.resolveSibling(dbFile.getFileName() + "-wal"));
      Files.deleteIfExists(dbFile.resolveSibling(dbFile.getFileName() + "-shm"));
    }
  }

  @Test
  void walHook_canBeRemoved() throws Exception {
    Path dbFile = Files.createTempFile("sqlite-wal-test", ".db");
    try (var db = Sqlite.open(dbFile.toString())) {
      db.exec("PRAGMA journal_mode=WAL");

      List<String> calls = new ArrayList<>();
      db.walHook((dbName, pageCount) -> {
        calls.add(dbName);
        return 0;
      });

      db.exec("CREATE TABLE t (id INTEGER)");
      assertThat(calls).hasSize(1);

      db.walHook(null);
      db.exec("INSERT INTO t VALUES (1)");

      assertThat(calls).hasSize(1);
    } finally {
      Files.deleteIfExists(dbFile);
      Files.deleteIfExists(dbFile.resolveSibling(dbFile.getFileName() + "-wal"));
      Files.deleteIfExists(dbFile.resolveSibling(dbFile.getFileName() + "-shm"));
    }
  }
}
