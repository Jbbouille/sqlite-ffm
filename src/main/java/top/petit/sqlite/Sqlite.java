package top.petit.sqlite;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import top.petit.sqlite.callback.ExecCallback;
import top.petit.sqlite.callback.WalHookCallback;
import top.petit.sqlite.exception.SqliteExceptionBackup;
import top.petit.sqlite.exception.SqliteExceptionBind;
import top.petit.sqlite.exception.SqliteExceptionBlob;
import top.petit.sqlite.exception.SqliteExceptionClose;
import top.petit.sqlite.exception.SqliteExceptionExec;
import top.petit.sqlite.exception.SqliteExceptionFinalize;
import top.petit.sqlite.exception.SqliteExceptionOpen;
import top.petit.sqlite.exception.SqliteExceptionPrepareStatement;
import top.petit.sqlite.exception.SqliteExceptionStep;
import top.petit.sqlite.object.Backup;
import top.petit.sqlite.object.Blob;
import top.petit.sqlite.object.PrepareStatement;
import top.petit.sqlite.result.BackupFinish;
import top.petit.sqlite.result.BackupStep;
import top.petit.sqlite.result.Bind;
import top.petit.sqlite.result.BlobResult;
import top.petit.sqlite.result.Close;
import top.petit.sqlite.result.ColumnType;
import top.petit.sqlite.result.Exec;
import top.petit.sqlite.result.Finalize;
import top.petit.sqlite.result.Open;
import top.petit.sqlite.result.OpenFlag;
import top.petit.sqlite.result.Prepare;
import top.petit.sqlite.result.PrepareFlag;
import top.petit.sqlite.result.Reset;
import top.petit.sqlite.object.SemVer;
import top.petit.sqlite.result.Step;

import static top.petit.sqlite.gen.sqlite3_h.*;

public class Sqlite implements AutoCloseable {

    private final Arena arena;
    private final MemorySegment db;
    private final boolean ownsArena;

    private Sqlite(Arena arena, MemorySegment db, boolean ownsArena) {
        this.arena = arena;
        this.db = db;
        this.ownsArena = ownsArena;
    }

    /**
     * Opens a connection to an SQLite database file.
     *
     * <p>The filename can be:</p>
     * <ul>
     *   <li>A path to a database file</li>
     *   <li>{@code ":memory:"} for an in-memory database</li>
     *   <li>{@code ""} for a temporary on-disk database</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try (var db = Sqlite.open(":memory:")) {
     *     var stmt = db.prepareV2("SELECT 1");
     *     db.step(stmt);
     *     db.finalize(stmt);
     * }
     * }</pre>
     *
     * @param filename the name of the database file to open
     * @return a new Sqlite connection
     * @throws SqliteExceptionOpen if the database cannot be opened
     * @see <a href="https://sqlite.org/c3ref/open.html">sqlite3_open documentation</a>
     */
    public static Sqlite open(String filename) throws SqliteExceptionOpen {
        return open(filename, null);
    }

    /**
     * Opens a connection to an SQLite database file using the specified arena.
     *
     * <p>The filename can be:</p>
     * <ul>
     *   <li>A path to a database file</li>
     *   <li>{@code ":memory:"} for an in-memory database</li>
     *   <li>{@code ""} for a temporary on-disk database</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try (var arena = Arena.ofConfined()) {
     *     var db = Sqlite.open(":memory:", arena);
     *     var stmt = db.prepareV2("SELECT 1");
     *     db.step(stmt);
     *     db.finalize(stmt);
     *     db.close();
     * }
     * }</pre>
     *
     * @param filename the name of the database file to open
     * @param arena the arena to use for memory allocation, or null to create a new confined arena
     * @return a new Sqlite connection
     * @throws SqliteExceptionOpen if the database cannot be opened
     * @see <a href="https://sqlite.org/c3ref/open.html">sqlite3_open documentation</a>
     */
    public static Sqlite open(String filename, Arena arena) throws SqliteExceptionOpen {
        var ownsArena = arena == null;
        if (ownsArena) {
            arena = Arena.ofConfined();
        }

        var dbPtr = arena.allocate(ValueLayout.ADDRESS);
        var rc = sqlite3_open(arena.allocateFrom(filename), dbPtr);
        var result = Open.fromCode(rc);

        if (!result.isOk()) {
            if (ownsArena) {
                arena.close();
            }
            throw new SqliteExceptionOpen("Failed to open database %s with error %s.".formatted(filename, result), result);
        }

        return new Sqlite(arena, dbPtr.get(ValueLayout.ADDRESS, 0), ownsArena);
    }

    /**
     * Opens a connection to an SQLite database file with specific flags.
     *
     * <p>The flags parameter controls how the database is opened. At minimum,
     * either {@link OpenFlag#READONLY} or {@link OpenFlag#READWRITE} must be specified.
     * {@link OpenFlag#CREATE} can be combined with READWRITE to create the database if it doesn't exist.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Open read-only
     * try (var db = Sqlite.openV2("data.db", OpenFlag.READONLY)) {
     *     // ...
     * }
     *
     * // Open read-write, create if not exists
     * try (var db = Sqlite.openV2("data.db", OpenFlag.READWRITE, OpenFlag.CREATE)) {
     *     // ...
     * }
     *
     * // Open in-memory database
     * try (var db = Sqlite.openV2(":memory:", OpenFlag.READWRITE, OpenFlag.MEMORY)) {
     *     // ...
     * }
     * }</pre>
     *
     * @param filename the name of the database file to open
     * @param flags the open flags
     * @return a new Sqlite connection
     * @throws SqliteExceptionOpen if the database cannot be opened
     * @see OpenFlag
     * @see <a href="https://sqlite.org/c3ref/open.html">sqlite3_open_v2 documentation</a>
     */
    public static Sqlite openV2(String filename, OpenFlag... flags) throws SqliteExceptionOpen {
        return openV2(filename, null, flags);
    }

    /**
     * Opens a connection to an SQLite database file with specific flags and arena.
     *
     * @param filename the name of the database file to open
     * @param arena the arena to use for memory allocation, or null to create a new confined arena
     * @param flags the open flags
     * @return a new Sqlite connection
     * @throws SqliteExceptionOpen if the database cannot be opened
     * @see OpenFlag
     * @see <a href="https://sqlite.org/c3ref/open.html">sqlite3_open_v2 documentation</a>
     */
    public static Sqlite openV2(String filename, Arena arena, OpenFlag... flags) throws SqliteExceptionOpen {
        var ownsArena = arena == null;
        if (ownsArena) {
            arena = Arena.ofConfined();
        }

        var dbPtr = arena.allocate(ValueLayout.ADDRESS);
        var openFlags = OpenFlag.combine(flags);
        var rc = sqlite3_open_v2(arena.allocateFrom(filename), dbPtr, openFlags, MemorySegment.NULL);
        var result = Open.fromCode(rc);

        if (!result.isOk()) {
            if (ownsArena) {
                arena.close();
            }
            throw new SqliteExceptionOpen("Failed to open database %s with error %s.".formatted(filename, result), result);
        }

        return new Sqlite(arena, dbPtr.get(ValueLayout.ADDRESS, 0), ownsArena);
    }

    /**
     * Returns the SQLite library version as a SemVer record.
     *
     * <p>The version contains major, minor, and patch components.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * SemVer version = Sqlite.libversion(); // SemVer[major=3, minor=51, patch=1]
     * }</pre>
     *
     * @return the version as a SemVer record
     * @see #libversionNumber()
     * @see <a href="https://sqlite.org/c3ref/libversion.html">sqlite3_libversion documentation</a>
     */
    public static SemVer libversion() {
        return SemVer.fromVersionString(sqlite3_libversion().getString(0));
    }

    /**
     * Returns the SQLite library version as a number.
     *
     * <p>The version number is computed as: {@code major * 1000000 + minor * 1000 + release}.
     * For example, version 3.51.1 is encoded as 3051001.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * int version = Sqlite.libversionNumber(); // 3051001
     * }</pre>
     *
     * @return the version number (e.g., 3051001 for version 3.51.1)
     * @see #libversion()
     * @see <a href="https://sqlite.org/c3ref/libversion.html">sqlite3_libversion_number documentation</a>
     */
    public static int libversionNumber() {
        return sqlite3_libversion_number();
    }

    /**
     * Executes one or more SQL statements.
     *
     * <p>This is a convenience method for executing SQL statements that do not
     * return data (e.g., CREATE, INSERT, UPDATE, DELETE). For queries that return
     * data, use {@link #prepareV2(String)} and {@link #step(PrepareStatement)} instead.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try (var db = Sqlite.open(":memory:")) {
     *     db.exec("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT)");
     *     db.exec("INSERT INTO users (name) VALUES ('Alice')");
     * }
     * }</pre>
     *
     * @param sql one or more SQL statements to execute
     * @throws SqliteExceptionExec if the execution fails
     * @see <a href="https://sqlite.org/c3ref/exec.html">sqlite3_exec documentation</a>
     */
    public void exec(String sql) throws SqliteExceptionExec {
        var errMsgPtr = arena.allocate(ValueLayout.ADDRESS);
        var rc = sqlite3_exec(db, arena.allocateFrom(sql), MemorySegment.NULL, MemorySegment.NULL, errMsgPtr);
        var result = Exec.fromCode(rc);

        if (!result.isOk()) {
            String errorMessage = null;
            var errMsg = errMsgPtr.get(ValueLayout.ADDRESS, 0);
            if (!errMsg.equals(MemorySegment.NULL)) {
                errorMessage = errMsg.reinterpret(1024).getString(0);
                sqlite3_free(errMsg);
            }
            throw new SqliteExceptionExec("Failed to execute SQL: %s".formatted(sql), result, errorMessage);
        }
    }

    /**
     * Executes one or more SQL statements with a callback for each result row.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try (var db = Sqlite.open(":memory:")) {
     *     db.exec("CREATE TABLE users (id INTEGER, name TEXT)");
     *     db.exec("INSERT INTO users VALUES (1, 'Alice'), (2, 'Bob')");
     *
     *     db.exec("SELECT * FROM users", (columnNames, values) -> {
     *         System.out.println(columnNames[1] + ": " + values[1]);
     *         return true; // continue
     *     });
     * }
     * }</pre>
     *
     * @param sql one or more SQL statements to execute
     * @param callback the callback to invoke for each row
     * @throws SqliteExceptionExec if the execution fails
     * @see <a href="https://sqlite.org/c3ref/exec.html">sqlite3_exec documentation</a>
     */
    public void exec(String sql, ExecCallback callback) throws SqliteExceptionExec {
        try {
            var callbackHandle = MethodHandles.lookup().findStatic(
                Sqlite.class,
                "execCallbackTrampoline",
                MethodType.methodType(int.class, ExecCallback.class, int.class, MemorySegment.class, MemorySegment.class)
            );

            // Bind the callback to the first argument
            callbackHandle = MethodHandles.insertArguments(callbackHandle, 0, callback);

            // Drop the first native argument (void* userData) since we don't need it
            callbackHandle = MethodHandles.dropArguments(callbackHandle, 0, MemorySegment.class);

            var callbackDescriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,  // void* userData
                ValueLayout.JAVA_INT, // int columnCount
                ValueLayout.ADDRESS,  // char** values
                ValueLayout.ADDRESS   // char** columnNames
            );

            var callbackStub = Linker.nativeLinker().upcallStub(callbackHandle, callbackDescriptor, arena);

            var errMsgPtr = arena.allocate(ValueLayout.ADDRESS);
            var rc = sqlite3_exec(db, arena.allocateFrom(sql), callbackStub, MemorySegment.NULL, errMsgPtr);
            var result = Exec.fromCode(rc);

            // ABORT is expected when callback returns false to stop iteration
            if (!result.isOk() && result != Exec.ABORT) {
                String errorMessage = null;
                var errMsg = errMsgPtr.get(ValueLayout.ADDRESS, 0);
                if (!errMsg.equals(MemorySegment.NULL)) {
                    errorMessage = errMsg.reinterpret(1024).getString(0);
                    sqlite3_free(errMsg);
                }
                throw new SqliteExceptionExec("Failed to execute SQL %s with error code %s and message %s.".formatted(sql, result, errorMessage), result, errorMessage);
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create callback", e);
        }
    }

    private static int execCallbackTrampoline(ExecCallback callback, int columnCount, MemorySegment valuesPtr, MemorySegment columnNamesPtr) {
        String[] values = new String[columnCount];
        String[] columnNames = new String[columnCount];

        for (int i = 0; i < columnCount; i++) {
            var valuePtr = valuesPtr.reinterpret(ValueLayout.ADDRESS.byteSize() * columnCount)
                .getAtIndex(ValueLayout.ADDRESS, i);
            if (!valuePtr.equals(MemorySegment.NULL)) {
                values[i] = valuePtr.reinterpret(1024).getString(0);
            }

            var namePtr = columnNamesPtr.reinterpret(ValueLayout.ADDRESS.byteSize() * columnCount)
                .getAtIndex(ValueLayout.ADDRESS, i);
            if (!namePtr.equals(MemorySegment.NULL)) {
                columnNames[i] = namePtr.reinterpret(1024).getString(0);
            }
        }

        return callback.onRow(columnNames, values) ? 0 : 1;
    }

    /**
     * Compiles an SQL statement into a prepared statement.
     *
     * <p>To execute an SQL statement, it must first be compiled into a byte-code
     * program using this method.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try (var db = Sqlite.open(":memory:")) {
     *     var stmt = db.prepareV2("SELECT id, name FROM users WHERE age > 18");
     *
     *     while (db.step(stmt) == Step.ROW) {
     *         int id = sqlite3_column_int(stmt.value(), 0);
     *         String name = sqlite3_column_text(stmt.value(), 1).getString(0);
     *     }
     *
     *     db.finalize(stmt);
     * }
     * }</pre>
     *
     * @param sql the SQL statement to compile
     * @return the prepared statement
     * @throws SqliteExceptionPrepareStatement if the statement cannot be compiled
     * @see <a href="https://sqlite.org/c3ref/prepare.html">sqlite3_prepare documentation</a>
     */
    public PrepareStatement prepareV2(String sql) throws SqliteExceptionPrepareStatement {
        var stmtPtr = arena.allocate(ValueLayout.ADDRESS);
        var rc = sqlite3_prepare_v2(db, arena.allocateFrom(sql), -1, stmtPtr, MemorySegment.NULL);
        var result = Prepare.fromCode(rc);

        if (!result.isOk()) {
            throw new SqliteExceptionPrepareStatement("Failed to prepare statement %s with error %s.".formatted(sql, result), result);
        }
        return new PrepareStatement(stmtPtr.get(ValueLayout.ADDRESS, 0), this::finalize);
    }

    /**
     * Compiles an SQL statement into a prepared statement with optional flags.
     *
     * <p>This is the v3 API that supports preparation flags for specialized behavior.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try (var db = Sqlite.open(":memory:")) {
     *     // Use PERSISTENT flag for statements that will be reused many times
     *     var stmt = db.prepareV3("SELECT * FROM users", PrepareFlag.PERSISTENT);
     *     // ...
     *     db.finalize(stmt);
     * }
     * }</pre>
     *
     * @param sql the SQL statement to compile
     * @param flags optional preparation flags
     * @return the prepared statement
     * @throws SqliteExceptionPrepareStatement if the statement cannot be compiled
     * @see PrepareFlag
     * @see <a href="https://sqlite.org/c3ref/prepare.html">sqlite3_prepare documentation</a>
     */
    public PrepareStatement prepareV3(String sql, PrepareFlag... flags) throws SqliteExceptionPrepareStatement {
        var stmtPtr = arena.allocate(ValueLayout.ADDRESS);
        var prepFlags = PrepareFlag.combine(flags);
        var rc = sqlite3_prepare_v3(db, arena.allocateFrom(sql), -1, prepFlags, stmtPtr, MemorySegment.NULL);
        var result = Prepare.fromCode(rc);

        if (!result.isOk()) {
            throw new SqliteExceptionPrepareStatement("Failed to prepare statement %s with error %s.".formatted(sql, result), result);
        }
        return new PrepareStatement(stmtPtr.get(ValueLayout.ADDRESS, 0), this::finalize);
    }

    /**
     * Compiles an SQL statement into a prepared statement.
     *
     * @param sql the SQL statement to compile
     * @return the prepared statement
     * @throws SqliteExceptionPrepareStatement if the statement cannot be compiled
     * @deprecated Use {@link #prepareV2(String)} instead. The legacy prepare API does not
     *             handle schema changes automatically and may return stale error codes.
     * @see <a href="https://sqlite.org/c3ref/prepare.html">sqlite3_prepare documentation</a>
     */
    @Deprecated
    public PrepareStatement prepare(String sql) throws SqliteExceptionPrepareStatement {
        var stmtPtr = arena.allocate(ValueLayout.ADDRESS);
        var rc = sqlite3_prepare(db, arena.allocateFrom(sql), -1, stmtPtr, MemorySegment.NULL);
        var result = Prepare.fromCode(rc);

        if (!result.isOk()) {
            throw new SqliteExceptionPrepareStatement("Failed to prepare statement %s with error %s.".formatted(sql, result), result);
        }
        return new PrepareStatement(stmtPtr.get(ValueLayout.ADDRESS, 0), this::finalize);
    }

    /**
     * Binds an integer value to a prepared statement parameter.
     *
     * @param stmt the prepared statement
     * @param index the parameter index (1-based)
     * @param value the integer value to bind
     * @throws SqliteExceptionBind if the bind operation fails
     * @see <a href="https://sqlite.org/c3ref/bind_blob.html">sqlite3_bind documentation</a>
     */
    public void bindInt(PrepareStatement stmt, int index, int value) throws SqliteExceptionBind {
        var rc = sqlite3_bind_int(stmt.value(), index, value);
        var result = Bind.fromCode(rc);
        if (!result.isOk()) {
            throw new SqliteExceptionBind("Failed to bind int at index %d with error %s.".formatted(index, result), result);
        }
    }

    /**
     * Binds a 64-bit integer value to a prepared statement parameter.
     *
     * @param stmt the prepared statement
     * @param index the parameter index (1-based)
     * @param value the long value to bind
     * @throws SqliteExceptionBind if the bind operation fails
     * @see <a href="https://sqlite.org/c3ref/bind_blob.html">sqlite3_bind documentation</a>
     */
    public void bindLong(PrepareStatement stmt, int index, long value) throws SqliteExceptionBind {
        var rc = sqlite3_bind_int64(stmt.value(), index, value);
        var result = Bind.fromCode(rc);
        if (!result.isOk()) {
            throw new SqliteExceptionBind("Failed to bind long at index %d with error %s.".formatted(index, result), result);
        }
    }

    /**
     * Binds a double value to a prepared statement parameter.
     *
     * @param stmt the prepared statement
     * @param index the parameter index (1-based)
     * @param value the double value to bind
     * @throws SqliteExceptionBind if the bind operation fails
     * @see <a href="https://sqlite.org/c3ref/bind_blob.html">sqlite3_bind documentation</a>
     */
    public void bindDouble(PrepareStatement stmt, int index, double value) throws SqliteExceptionBind {
        var rc = sqlite3_bind_double(stmt.value(), index, value);
        var result = Bind.fromCode(rc);
        if (!result.isOk()) {
            throw new SqliteExceptionBind("Failed to bind double at index %d with error %s.".formatted(index, result), result);
        }
    }

    /**
     * Binds a text value to a prepared statement parameter.
     *
     * @param stmt the prepared statement
     * @param index the parameter index (1-based)
     * @param value the text value to bind
     * @throws SqliteExceptionBind if the bind operation fails
     * @see <a href="https://sqlite.org/c3ref/bind_blob.html">sqlite3_bind documentation</a>
     */
    public void bindText(PrepareStatement stmt, int index, String value) throws SqliteExceptionBind {
        var textSegment = arena.allocateFrom(value);
        var rc = sqlite3_bind_text(stmt.value(), index, textSegment, (int) textSegment.byteSize() - 1, MemorySegment.NULL);
        var result = Bind.fromCode(rc);
        if (!result.isOk()) {
            throw new SqliteExceptionBind("Failed to bind text at index %d with error %s.".formatted(index, result), result);
        }
    }

    /**
     * Binds a blob value to a prepared statement parameter.
     *
     * @param stmt the prepared statement
     * @param index the parameter index (1-based)
     * @param value the byte array to bind
     * @throws SqliteExceptionBind if the bind operation fails
     * @see <a href="https://sqlite.org/c3ref/bind_blob.html">sqlite3_bind documentation</a>
     */
    public void bindBlob(PrepareStatement stmt, int index, byte[] value) throws SqliteExceptionBind {
        var blobSegment = arena.allocate(value.length);
        blobSegment.copyFrom(MemorySegment.ofArray(value));
        var rc = sqlite3_bind_blob(stmt.value(), index, blobSegment, value.length, MemorySegment.NULL);
        var result = Bind.fromCode(rc);
        if (!result.isOk()) {
            throw new SqliteExceptionBind("Failed to bind blob at index %d with error %s.".formatted(index, result), result);
        }
    }

    /**
     * Binds a blob value to a prepared statement parameter, using a 64-bit size.
     *
     * <p>Identical to {@link #bindBlob(PrepareStatement, int, byte[])} but accepts a {@code long}
     * size, allowing blobs larger than 2 GiB.</p>
     *
     * @param stmt the prepared statement
     * @param index the parameter index (1-based)
     * @param value the byte array to bind
     * @throws SqliteExceptionBind if the bind operation fails
     * @see <a href="https://sqlite.org/c3ref/bind_blob.html">sqlite3_bind documentation</a>
     */
    public void bindBlob64(PrepareStatement stmt, int index, byte[] value) throws SqliteExceptionBind {
        var blobSegment = arena.allocate(value.length);
        blobSegment.copyFrom(MemorySegment.ofArray(value));
        var rc = sqlite3_bind_blob64(stmt.value(), index, blobSegment, value.length, MemorySegment.NULL);
        var result = Bind.fromCode(rc);
        if (!result.isOk()) {
            throw new SqliteExceptionBind("Failed to bind blob64 at index %d with error %s.".formatted(index, result), result);
        }
    }

    /**
     * Binds a NULL value to a prepared statement parameter.
     *
     * @param stmt the prepared statement
     * @param index the parameter index (1-based)
     * @return the bind result
     * @throws SqliteExceptionBind if the bind operation fails
     * @see <a href="https://sqlite.org/c3ref/bind_blob.html">sqlite3_bind documentation</a>
     */
    public Bind bindNull(PrepareStatement stmt, int index) throws SqliteExceptionBind {
        var result = Bind.fromCode(sqlite3_bind_null(stmt.value(), index));
        if (!result.isOk()) {
            throw new SqliteExceptionBind("Failed to bind null at index %d with error %s.".formatted(index, result), result);
        }
        return result;
    }

    /**
     * Returns the number of columns in the result set.
     *
     * @param stmt the prepared statement
     * @return the number of columns
     * @see <a href="https://sqlite.org/c3ref/column_count.html">sqlite3_column_count documentation</a>
     */
    public int columnCount(PrepareStatement stmt) {
        return sqlite3_column_count(stmt.value());
    }

    /**
     * Returns the type of a column value.
     *
     * @param stmt the prepared statement
     * @param index the column index (0-based)
     * @return the column type
     * @see <a href="https://sqlite.org/c3ref/column_blob.html">sqlite3_column documentation</a>
     */
    public ColumnType columnType(PrepareStatement stmt, int index) {
        return ColumnType.fromCode(sqlite3_column_type(stmt.value(), index));
    }

    /**
     * Returns an integer value from a column.
     *
     * @param stmt the prepared statement
     * @param index the column index (0-based)
     * @return the integer value
     * @see <a href="https://sqlite.org/c3ref/column_blob.html">sqlite3_column documentation</a>
     */
    public int columnInt(PrepareStatement stmt, int index) {
        return sqlite3_column_int(stmt.value(), index);
    }

    /**
     * Returns a 64-bit integer value from a column.
     *
     * @param stmt the prepared statement
     * @param index the column index (0-based)
     * @return the long value
     * @see <a href="https://sqlite.org/c3ref/column_blob.html">sqlite3_column documentation</a>
     */
    public long columnLong(PrepareStatement stmt, int index) {
        return sqlite3_column_int64(stmt.value(), index);
    }

    /**
     * Returns a double value from a column.
     *
     * @param stmt the prepared statement
     * @param index the column index (0-based)
     * @return the double value
     * @see <a href="https://sqlite.org/c3ref/column_blob.html">sqlite3_column documentation</a>
     */
    public double columnDouble(PrepareStatement stmt, int index) {
        return sqlite3_column_double(stmt.value(), index);
    }

    /**
     * Returns a text value from a column.
     *
     * @param stmt the prepared statement
     * @param index the column index (0-based)
     * @return the text value, or null if the column is NULL
     * @see <a href="https://sqlite.org/c3ref/column_blob.html">sqlite3_column documentation</a>
     */
    public String columnText(PrepareStatement stmt, int index) {
        var ptr = sqlite3_column_text(stmt.value(), index);
        if (ptr.equals(MemorySegment.NULL)) {
            return null;
        }
        return ptr.getString(0);
    }

    /**
     * Returns a blob value from a column.
     *
     * @param stmt the prepared statement
     * @param index the column index (0-based)
     * @return the byte array, or null if the column is NULL
     * @see <a href="https://sqlite.org/c3ref/column_blob.html">sqlite3_column documentation</a>
     */
    public byte[] columnBlob(PrepareStatement stmt, int index) {
        var ptr = sqlite3_column_blob(stmt.value(), index);
        if (ptr.equals(MemorySegment.NULL)) {
            return null;
        }
        int size = sqlite3_column_bytes(stmt.value(), index);
        return ptr.reinterpret(size).toArray(ValueLayout.JAVA_BYTE);
    }

    /**
     * Evaluates a prepared statement.
     *
     * <p>After a prepared statement has been prepared using {@link #prepareV2(String)},
     * this method must be called one or more times to evaluate the statement.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try (var db = Sqlite.open(":memory:")) {
     *     var stmt = db.prepareV2("SELECT id, name FROM users");
     *
     *     while (db.step(stmt) == Step.ROW) {
     *         int id = sqlite3_column_int(stmt.value(), 0);
     *         String name = sqlite3_column_text(stmt.value(), 1).getString(0);
     *     }
     *
     *     db.finalize(stmt);
     * }
     * }</pre>
     *
     * @param stmt the prepared statement to evaluate
     * @return {@link Step#ROW} if a row is available, {@link Step#DONE} if finished
     * @throws SqliteExceptionStep if an error occurs during evaluation
     * @see <a href="https://sqlite.org/c3ref/step.html">sqlite3_step documentation</a>
     */
    public Step step(PrepareStatement stmt) throws SqliteExceptionStep {
        var result = Step.fromCode(sqlite3_step(stmt.value()));

        if (result == Step.ERROR || result == Step.CORRUPT || result == Step.MISUSE) {
            throw new SqliteExceptionStep("Failed to step statement with error %s.".formatted(result), result);
        }
        return result;
    }

    /**
     * Destroys a prepared statement object.
     *
     * <p>The {@code finalize} method is called to delete a prepared statement.
     * If the most recent evaluation of the statement encountered no errors or
     * if the statement is never been evaluated, the method returns normally.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try (var db = Sqlite.open(":memory:")) {
     *     var stmt = db.prepareV2("INSERT INTO users (name) VALUES ('Alice')");
     *     db.step(stmt);
     *     db.finalize(stmt);
     * }
     * }</pre>
     *
     * @param stmt the prepared statement to destroy
     * @return the finalize result
     * @throws SqliteExceptionFinalize if the most recent evaluation of statement failed
     * @see <a href="https://sqlite.org/c3ref/finalize.html">sqlite3_finalize documentation</a>
     */
    public Finalize finalize(PrepareStatement stmt) throws SqliteExceptionFinalize {
        var result = Finalize.fromCode(sqlite3_finalize(stmt.value()));
        if (!result.isOk()) {
            throw new SqliteExceptionFinalize("Failed to finalize statement with error %s.".formatted(result), result);
        }
        return result;
    }

    /**
     * Resets a prepared statement to its initial state.
     *
     * <p>This method resets the statement so it can be re-executed. Any bindings
     * remain in place. Use {@link #clearBindings(PrepareStatement)} to clear them.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * var stmt = db.prepareV2("INSERT INTO users (name) VALUES (?)");
     * db.bindText(stmt, 1, "Alice");
     * db.step(stmt);
     * db.reset(stmt);
     * db.bindText(stmt, 1, "Bob");
     * db.step(stmt);
     * db.finalize(stmt);
     * }</pre>
     *
     * @param stmt the prepared statement to reset
     * @return the reset result
     * @see <a href="https://sqlite.org/c3ref/reset.html">sqlite3_reset documentation</a>
     */
    public Reset reset(PrepareStatement stmt) {
        return Reset.fromCode(sqlite3_reset(stmt.value()));
    }

    /**
     * Clears all bindings on a prepared statement.
     *
     * <p>Use this method to reset the bindings without destroying the statement.</p>
     *
     * @param stmt the prepared statement
     * @see <a href="https://sqlite.org/c3ref/clear_bindings.html">sqlite3_clear_bindings documentation</a>
     */
    public void clearBindings(PrepareStatement stmt) {
        sqlite3_clear_bindings(stmt.value());
    }

    /**
     * Returns the number of rows modified by the most recent INSERT, UPDATE, or DELETE.
     *
     * @return the number of rows changed
     * @see <a href="https://sqlite.org/c3ref/changes.html">sqlite3_changes documentation</a>
     */
    public int changes() {
        return sqlite3_changes(db);
    }

    /**
     * Returns the number of rows modified by the most recent INSERT, UPDATE, or DELETE.
     *
     * <p>This is the 64-bit version for very large changes.</p>
     *
     * @return the number of rows changed
     * @see <a href="https://sqlite.org/c3ref/changes.html">sqlite3_changes64 documentation</a>
     */
    public long changes64() {
        return sqlite3_changes64(db);
    }

    /**
     * Returns the total number of rows modified since the connection was opened.
     *
     * @return the total number of rows changed
     * @see <a href="https://sqlite.org/c3ref/total_changes.html">sqlite3_total_changes documentation</a>
     */
    public int totalChanges() {
        return sqlite3_total_changes(db);
    }

    /**
     * Returns the total number of rows modified since the connection was opened.
     *
     * <p>This is the 64-bit version for very large changes.</p>
     *
     * @return the total number of rows changed
     * @see <a href="https://sqlite.org/c3ref/total_changes.html">sqlite3_total_changes64 documentation</a>
     */
    public long totalChanges64() {
        return sqlite3_total_changes64(db);
    }

    /**
     * Returns the ROWID of the most recent successful INSERT.
     *
     * @return the last insert ROWID
     * @see <a href="https://sqlite.org/c3ref/last_insert_rowid.html">sqlite3_last_insert_rowid documentation</a>
     */
    public long lastInsertRowid() {
        return sqlite3_last_insert_rowid(db);
    }

    /**
     * Returns the error code for the most recent failed API call.
     *
     * @return the error code
     * @see <a href="https://sqlite.org/c3ref/errcode.html">sqlite3_errcode documentation</a>
     */
    public int errcode() {
        return sqlite3_errcode(db);
    }

    /**
     * Returns the extended error code for the most recent failed API call.
     *
     * @return the extended error code
     * @see <a href="https://sqlite.org/c3ref/errcode.html">sqlite3_extended_errcode documentation</a>
     */
    public int extendedErrcode() {
        return sqlite3_extended_errcode(db);
    }

    /**
     * Returns the error message for the most recent failed API call.
     *
     * @return the error message
     * @see <a href="https://sqlite.org/c3ref/errcode.html">sqlite3_errmsg documentation</a>
     */
    public String errmsg() {
        return sqlite3_errmsg(db).getString(0);
    }

    /**
     * Returns the name of a column in the result set.
     *
     * @param stmt the prepared statement
     * @param index the column index (0-based)
     * @return the column name
     * @see <a href="https://sqlite.org/c3ref/column_name.html">sqlite3_column_name documentation</a>
     */
    public String columnName(PrepareStatement stmt, int index) {
        var ptr = sqlite3_column_name(stmt.value(), index);
        if (ptr.equals(MemorySegment.NULL)) {
            return null;
        }
        return ptr.getString(0);
    }

    /**
     * Returns the declared type of a column in the result set.
     *
     * @param stmt the prepared statement
     * @param index the column index (0-based)
     * @return the declared type, or null if not available
     * @see <a href="https://sqlite.org/c3ref/column_decltype.html">sqlite3_column_decltype documentation</a>
     */
    public String columnDecltype(PrepareStatement stmt, int index) {
        var ptr = sqlite3_column_decltype(stmt.value(), index);
        if (ptr.equals(MemorySegment.NULL)) {
            return null;
        }
        return ptr.getString(0);
    }

    /**
     * Returns the number of columns in the current result row.
     *
     * <p>Unlike {@link #columnCount(PrepareStatement)}, this returns 0 if the
     * most recent call to step() returned DONE or an error.</p>
     *
     * @param stmt the prepared statement
     * @return the number of columns in the current row
     * @see <a href="https://sqlite.org/c3ref/data_count.html">sqlite3_data_count documentation</a>
     */
    public int dataCount(PrepareStatement stmt) {
        return sqlite3_data_count(stmt.value());
    }

    /**
     * Returns the number of parameters in a prepared statement.
     *
     * @param stmt the prepared statement
     * @return the number of parameters
     * @see <a href="https://sqlite.org/c3ref/bind_parameter_count.html">sqlite3_bind_parameter_count documentation</a>
     */
    public int bindParameterCount(PrepareStatement stmt) {
        return sqlite3_bind_parameter_count(stmt.value());
    }

    /**
     * Returns the index of a named parameter.
     *
     * @param stmt the prepared statement
     * @param name the parameter name (including the prefix like ":" or "@")
     * @return the parameter index (1-based), or 0 if not found
     * @see <a href="https://sqlite.org/c3ref/bind_parameter_index.html">sqlite3_bind_parameter_index documentation</a>
     */
    public int bindParameterIndex(PrepareStatement stmt, String name) {
        return sqlite3_bind_parameter_index(stmt.value(), arena.allocateFrom(name));
    }

    /**
     * Returns the name of a parameter by index.
     *
     * @param stmt the prepared statement
     * @param index the parameter index (1-based)
     * @return the parameter name, or null if anonymous
     * @see <a href="https://sqlite.org/c3ref/bind_parameter_name.html">sqlite3_bind_parameter_name documentation</a>
     */
    public String bindParameterName(PrepareStatement stmt, int index) {
        var ptr = sqlite3_bind_parameter_name(stmt.value(), index);
        if (ptr.equals(MemorySegment.NULL)) {
            return null;
        }
        return ptr.getString(0);
    }

    /**
     * Sets the busy timeout in milliseconds.
     *
     * <p>When a table is locked, SQLite will wait up to the timeout before
     * returning SQLITE_BUSY.</p>
     *
     * @param ms the timeout in milliseconds (0 to disable)
     * @see <a href="https://sqlite.org/c3ref/busy_timeout.html">sqlite3_busy_timeout documentation</a>
     */
    public void busyTimeout(int ms) {
        sqlite3_busy_timeout(db, ms);
    }

    /**
     * Returns true if the database is in auto-commit mode.
     *
     * <p>Auto-commit mode is on by default. It is turned off by a BEGIN statement
     * and turned back on by COMMIT or ROLLBACK.</p>
     *
     * @return true if in auto-commit mode
     * @see <a href="https://sqlite.org/c3ref/get_autocommit.html">sqlite3_get_autocommit documentation</a>
     */
    public boolean getAutocommit() {
        return sqlite3_get_autocommit(db) != 0;
    }

    /**
     * Interrupts a long-running query.
     *
     * <p>This causes any pending database operation to abort and return SQLITE_INTERRUPT.</p>
     *
     * @see <a href="https://sqlite.org/c3ref/interrupt.html">sqlite3_interrupt documentation</a>
     */
    public void interrupt() {
        sqlite3_interrupt(db);
    }

    /**
     * Returns true if the query was interrupted.
     *
     * @return true if interrupted
     * @see <a href="https://sqlite.org/c3ref/interrupt.html">sqlite3_is_interrupted documentation</a>
     */
    public boolean isInterrupted() {
        return sqlite3_is_interrupted(db) != 0;
    }

    /**
     * Returns the SQL text of a prepared statement.
     *
     * @param stmt the prepared statement
     * @return the SQL text
     * @see <a href="https://sqlite.org/c3ref/expanded_sql.html">sqlite3_sql documentation</a>
     */
    public String sql(PrepareStatement stmt) {
        var ptr = sqlite3_sql(stmt.value());
        if (ptr.equals(MemorySegment.NULL)) {
            return null;
        }
        return ptr.getString(0);
    }

    /**
     * Returns true if the prepared statement is read-only.
     *
     * @param stmt the prepared statement
     * @return true if read-only
     * @see <a href="https://sqlite.org/c3ref/stmt_readonly.html">sqlite3_stmt_readonly documentation</a>
     */
    public boolean stmtReadonly(PrepareStatement stmt) {
        return sqlite3_stmt_readonly(stmt.value()) != 0;
    }

    /**
     * Returns true if the prepared statement is busy (has been stepped but not reset or finalized).
     *
     * @param stmt the prepared statement
     * @return true if busy
     * @see <a href="https://sqlite.org/c3ref/stmt_busy.html">sqlite3_stmt_busy documentation</a>
     */
    public boolean stmtBusy(PrepareStatement stmt) {
        return sqlite3_stmt_busy(stmt.value()) != 0;
    }

    /**
     * Returns the SQLite source ID string.
     *
     * @return the source ID
     * @see <a href="https://sqlite.org/c3ref/libversion.html">sqlite3_sourceid documentation</a>
     */
    public static String sourceid() {
        return sqlite3_sourceid().getString(0);
    }

    /**
     * Returns 0 if SQLite was compiled with thread safety.
     *
     * @return 0 if single-threaded, 1 if serialized, 2 if multi-threaded
     * @see <a href="https://sqlite.org/c3ref/threadsafe.html">sqlite3_threadsafe documentation</a>
     */
    public static int threadsafe() {
        return sqlite3_threadsafe();
    }

    /**
     * Checks if the given SQL string is complete.
     *
     * <p>Returns true if the input appears to be a complete SQL statement.</p>
     *
     * @param sql the SQL string to check
     * @return true if the SQL appears complete
     * @see <a href="https://sqlite.org/c3ref/complete.html">sqlite3_complete documentation</a>
     */
    public static boolean complete(String sql) {
        try (var arena = Arena.ofConfined()) {
            return sqlite3_complete(arena.allocateFrom(sql)) != 0;
        }
    }

    /**
     * Checks if the given SQL string is complete.
     *
     * <p>Returns true if the input appears to be a complete SQL statement.</p>
     *
     * @param sql the SQL string to check
     * @return true if the SQL appears complete
     * @see <a href="https://sqlite.org/c3ref/complete.html">sqlite3_complete documentation</a>
     */
    public boolean isComplete(String sql) {
        return sqlite3_complete(arena.allocateFrom(sql)) != 0;
    }

    /**
     * Opens a BLOB for incremental I/O.
     *
     * <p>This method opens a handle to a BLOB located in row {@code rowid}, column {@code column},
     * table {@code table} in database {@code database}. The handle can then be used with
     * {@link #blobRead} and {@link #blobWrite} for incremental I/O.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try (var db = Sqlite.open(":memory:")) {
     *     db.exec("CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)");
     *     db.exec("INSERT INTO t (data) VALUES (zeroblob(1024))");
     *
     *     var blob = db.blobOpen("main", "t", "data", 1, true);
     *     db.blobWrite(blob, new byte[]{1, 2, 3}, 0);
     *     byte[] data = db.blobRead(blob, 3, 0);
     *     db.blobClose(blob);
     * }
     * }</pre>
     *
     * @param database the database name ("main", "temp", or an attached database)
     * @param table the table name
     * @param column the column name
     * @param rowid the ROWID of the row
     * @param writeAccess true for read-write access, false for read-only
     * @return the BLOB handle
     * @throws SqliteExceptionBlob if the BLOB cannot be opened
     * @see <a href="https://sqlite.org/c3ref/blob_open.html">sqlite3_blob_open documentation</a>
     */
    public Blob blobOpen(String database, String table, String column, long rowid, boolean writeAccess) throws SqliteExceptionBlob {
        var blobPtr = arena.allocate(ValueLayout.ADDRESS);
        var result = BlobResult.fromCode(sqlite3_blob_open(
            db,
            arena.allocateFrom(database),
            arena.allocateFrom(table),
            arena.allocateFrom(column),
            rowid,
            writeAccess ? 1 : 0,
            blobPtr
        ));

        if (!result.isOk()) {
            throw new SqliteExceptionBlob("Failed to open BLOB on %s.%s.%s rowid %d with error %s.".formatted(database, table, column, rowid, result), result);
        }

        return new Blob(blobPtr.get(ValueLayout.ADDRESS, 0), this::blobClose);
    }

    /**
     * Closes a BLOB handle.
     *
     * @param blob the BLOB handle to close
     * @return the close result
     * @throws SqliteExceptionBlob if an error occurred during the BLOB's lifetime
     * @see <a href="https://sqlite.org/c3ref/blob_close.html">sqlite3_blob_close documentation</a>
     */
    public BlobResult blobClose(Blob blob) throws SqliteExceptionBlob {
        var result = BlobResult.fromCode(sqlite3_blob_close(blob.value()));
        if (!result.isOk()) {
            throw new SqliteExceptionBlob("Failed to close BLOB with error %s.".formatted(result), result);
        }
        return result;
    }

    /**
     * Returns the size of an open BLOB in bytes.
     *
     * @param blob the BLOB handle
     * @return the size in bytes
     * @see <a href="https://sqlite.org/c3ref/blob_bytes.html">sqlite3_blob_bytes documentation</a>
     */
    public int blobBytes(Blob blob) {
        return sqlite3_blob_bytes(blob.value());
    }

    /**
     * Reads data from a BLOB.
     *
     * @param blob the BLOB handle
     * @param length the number of bytes to read
     * @param offset the offset within the BLOB to start reading
     * @return the bytes read
     * @throws SqliteExceptionBlob if the read fails
     * @see <a href="https://sqlite.org/c3ref/blob_read.html">sqlite3_blob_read documentation</a>
     */
    public byte[] blobRead(Blob blob, int length, int offset) throws SqliteExceptionBlob {
        var buffer = arena.allocate(length);
        var result = BlobResult.fromCode(sqlite3_blob_read(blob.value(), buffer, length, offset));

        if (!result.isOk()) {
            throw new SqliteExceptionBlob("Failed to read from BLOB at offset %d with error %s.".formatted(offset, result), result);
        }

        return buffer.toArray(ValueLayout.JAVA_BYTE);
    }

    /**
     * Writes data to a BLOB.
     *
     * <p>This method writes data to an open BLOB handle. The BLOB must have been opened
     * with write access. Writing to a BLOB does not change its size.</p>
     *
     * @param blob the BLOB handle
     * @param data the bytes to write
     * @param offset the offset within the BLOB to start writing
     * @return the write result
     * @throws SqliteExceptionBlob if the write fails
     * @see <a href="https://sqlite.org/c3ref/blob_write.html">sqlite3_blob_write documentation</a>
     */
    public BlobResult blobWrite(Blob blob, byte[] data, int offset) throws SqliteExceptionBlob {
        var buffer = arena.allocate(data.length);
        buffer.copyFrom(MemorySegment.ofArray(data));
        var result = BlobResult.fromCode(sqlite3_blob_write(blob.value(), buffer, data.length, offset));

        if (!result.isOk()) {
            throw new SqliteExceptionBlob("Failed to write to BLOB at offset %d with error %s.".formatted(offset, result), result);
        }
        return result;
    }

    /**
     * Moves a BLOB handle to a different row.
     *
     * <p>This method moves an open BLOB handle to point to a different row of the same
     * table. This is faster than closing and reopening the BLOB.</p>
     *
     * @param blob the BLOB handle
     * @param rowid the new ROWID
     * @return the reopen result
     * @throws SqliteExceptionBlob if the reopen fails
     * @see <a href="https://sqlite.org/c3ref/blob_reopen.html">sqlite3_blob_reopen documentation</a>
     */
    public BlobResult blobReopen(Blob blob, long rowid) throws SqliteExceptionBlob {
        var result = BlobResult.fromCode(sqlite3_blob_reopen(blob.value(), rowid));

        if (!result.isOk()) {
            throw new SqliteExceptionBlob("Failed to reopen BLOB to rowid %d with error %s.".formatted(rowid, result), result);
        }
        return result;
    }

    // ==================== Backup API ====================

    /**
     * Initializes a backup operation.
     *
     * <p>This method initializes a backup from the source database to the destination database.
     * The backup copies from the "main" database of this connection to the specified database
     * of the destination connection.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try (var src = Sqlite.open("source.db");
     *      var dst = Sqlite.open("backup.db")) {
     *
     *     var backup = src.backupInit(dst, "main", "main");
     *     while (src.backupStep(backup, 100).needsMoreWork()) {
     *         // Progress: backupRemaining / backupPagecount
     *     }
     *     src.backupFinish(backup);
     * }
     * }</pre>
     *
     * @param destination the destination database connection
     * @param destName the destination database name (usually "main")
     * @param sourceName the source database name (usually "main")
     * @return the backup handle
     * @throws SqliteExceptionBackup if the backup cannot be initialized
     * @see <a href="https://sqlite.org/c3ref/backup_finish.html">sqlite3_backup_init documentation</a>
     */
    public Backup backupInit(Sqlite destination, String destName, String sourceName) throws SqliteExceptionBackup {
        var backup = sqlite3_backup_init(
            destination.db,
            arena.allocateFrom(destName),
            db,
            arena.allocateFrom(sourceName)
        );

        if (backup.equals(MemorySegment.NULL)) {
            var result = BackupFinish.fromCode(destination.errcode());
            throw new SqliteExceptionBackup("Failed to initialize backup from %s to %s with error %s.".formatted(sourceName, destName, result), result);
        }

        return new Backup(backup, this::backupFinish);
    }

    /**
     * Copies pages during a backup operation.
     *
     * <p>This method copies up to {@code nPages} pages from the source to the destination.
     * If {@code nPages} is negative, all remaining pages are copied.</p>
     *
     * @param backup the backup handle
     * @param nPages the number of pages to copy, or -1 for all remaining pages
     * @return the result of the step operation
     * @see <a href="https://sqlite.org/c3ref/backup_finish.html">sqlite3_backup_step documentation</a>
     */
    public BackupStep backupStep(Backup backup, int nPages) {
        var rc = sqlite3_backup_step(backup.value(), nPages);
        return BackupStep.fromCode(rc);
    }

    /**
     * Finishes a backup operation.
     *
     * <p>This method releases all resources associated with the backup. It should be called
     * when the backup is complete or when you want to abort the backup.</p>
     *
     * @param backup the backup handle
     * @return the finish result
     * @throws SqliteExceptionBackup if an error occurred during the backup
     * @see <a href="https://sqlite.org/c3ref/backup_finish.html">sqlite3_backup_finish documentation</a>
     */
    public BackupFinish backupFinish(Backup backup) throws SqliteExceptionBackup {
        var result = BackupFinish.fromCode(sqlite3_backup_finish(backup.value()));
        if (!result.isOk()) {
            throw new SqliteExceptionBackup("Failed to finish backup with error %s.".formatted(result), result);
        }
        return result;
    }

    /**
     * Returns the number of pages remaining to be copied.
     *
     * @param backup the backup handle
     * @return the number of remaining pages
     * @see <a href="https://sqlite.org/c3ref/backup_finish.html">sqlite3_backup_remaining documentation</a>
     */
    public int backupRemaining(Backup backup) {
        return sqlite3_backup_remaining(backup.value());
    }

    /**
     * Returns the total number of pages in the source database.
     *
     * @param backup the backup handle
     * @return the total page count
     * @see <a href="https://sqlite.org/c3ref/backup_finish.html">sqlite3_backup_pagecount documentation</a>
     */
    public int backupPagecount(Backup backup) {
        return sqlite3_backup_pagecount(backup.value());
    }

    /**
     * Registers a WAL commit hook on this database connection.
     *
     * <p>The hook is invoked after each successful write into the WAL file. Passing
     * {@code null} removes any previously registered hook.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try (var db = Sqlite.open("test.db")) {
     *     db.exec("PRAGMA journal_mode=WAL");
     *     db.walHook((dbName, pageCount) -> {
     *         System.out.println("WAL commit on " + dbName + ", pages: " + pageCount);
     *         return 0;
     *     });
     *     db.exec("CREATE TABLE t (id INTEGER)");
     * }
     * }</pre>
     *
     * @param callback the hook to invoke after each WAL commit, or {@code null} to remove the hook
     * @see <a href="https://sqlite.org/c3ref/wal_hook.html">sqlite3_wal_hook documentation</a>
     */
    public void walHook(WalHookCallback callback) {
        if (callback == null) {
            sqlite3_wal_hook(db, MemorySegment.NULL, MemorySegment.NULL);
            return;
        }
        try {
            var trampolineHandle = MethodHandles.lookup().findStatic(
                Sqlite.class,
                "walHookTrampoline",
                MethodType.methodType(int.class, WalHookCallback.class, MemorySegment.class, MemorySegment.class, MemorySegment.class, int.class)
            );
            trampolineHandle = MethodHandles.insertArguments(trampolineHandle, 0, callback);

            var callbackDescriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // return int
                ValueLayout.ADDRESS,   // void* userData
                ValueLayout.ADDRESS,   // sqlite3* db
                ValueLayout.ADDRESS,   // const char* dbName
                ValueLayout.JAVA_INT   // int pageCount
            );

            var callbackStub = Linker.nativeLinker().upcallStub(trampolineHandle, callbackDescriptor, arena);
            sqlite3_wal_hook(db, callbackStub, MemorySegment.NULL);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create WAL hook", e);
        }
    }

    private static int walHookTrampoline(WalHookCallback callback, MemorySegment userData, MemorySegment db, MemorySegment dbNamePtr, int pageCount) {
        String dbName = dbNamePtr.equals(MemorySegment.NULL) ? null : dbNamePtr.reinterpret(1024).getString(0);
        return callback.onWalCommit(dbName, pageCount);
    }

    /**
     * Closes a database connection.
     *
     * <p>If the database connection has unfinalized prepared statements, open BLOB handles,
     * or unfinished backups, this method will throw an exception with error code SQLITE_BUSY.
     * In that case, the connection remains open and valid.</p>
     *
     * <p>For a more forgiving close that handles outstanding resources automatically,
     * use {@link #closeV2()}.</p>
     *
     * @return the close result
     * @throws SqliteExceptionClose if the database cannot be closed (e.g., SQLITE_BUSY)
     * @see <a href="https://sqlite.org/c3ref/close.html">sqlite3_close documentation</a>
     */
    public Close closeV1() throws SqliteExceptionClose {
        var result = Close.fromCode(sqlite3_close(db));
        if (!result.isOk()) {
            throw new SqliteExceptionClose("Failed to close database with error %s.".formatted(result), result);
        }
        if (ownsArena) {
            arena.close();
        }
        return result;
    }

    /**
     * Closes a database connection and marks it as a zombie if there are
     * unfinalized prepared statements, BLOB handles, or unfinished backups.
     *
     * <p>Unlike {@link #closeV1()}, this method will not fail if there are
     * outstanding resources. Instead, the connection will be automatically
     * closed when the last resource is released.</p>
     *
     * @return the close result
     * @throws SqliteExceptionClose if closing fails
     * @see <a href="https://sqlite.org/c3ref/close.html">sqlite3_close_v2 documentation</a>
     */
    public Close closeV2() throws SqliteExceptionClose {
        var result = Close.fromCode(sqlite3_close_v2(db));
        if (!result.isOk()) {
            throw new SqliteExceptionClose("Failed to close database with error %s.".formatted(result), result);
        }
        if (ownsArena) {
            arena.close();
        }
        return result;
    }

    @Override
    public void close() throws SqliteExceptionClose {
        closeV2();
    }
}
