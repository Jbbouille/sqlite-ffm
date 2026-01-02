package top.petit.sqlite.object;

import java.lang.foreign.MemorySegment;
import top.petit.sqlite.callback.CloseCallback;
import top.petit.sqlite.exception.SqliteExceptionFinalize;

/**
 * Represents a prepared statement.
 *
 * <p>A prepared statement is created using {@link top.petit.sqlite.Sqlite#prepareV2}
 * and should be finalized when no longer needed.</p>
 *
 * @see <a href="https://sqlite.org/c3ref/stmt.html">sqlite3_stmt documentation</a>
 */
public record PrepareStatement(
    MemorySegment value,
    CloseCallback<PrepareStatement, SqliteExceptionFinalize> closeCallback
) implements AutoCloseable {

    /**
     * Finalizes this prepared statement.
     *
     * @throws SqliteExceptionFinalize if the most recent evaluation of the statement failed
     * @see <a href="https://sqlite.org/c3ref/finalize.html">sqlite3_finalize documentation</a>
     */
    @Override
    public void close() throws SqliteExceptionFinalize {
        closeCallback.close(this);
    }
}
