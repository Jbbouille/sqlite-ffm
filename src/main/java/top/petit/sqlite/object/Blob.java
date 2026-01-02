package top.petit.sqlite.object;

import java.lang.foreign.MemorySegment;
import top.petit.sqlite.callback.CloseCallback;
import top.petit.sqlite.exception.SqliteExceptionBlob;

/**
 * Represents an open BLOB handle for incremental I/O.
 *
 * <p>A BLOB handle is opened using {@link top.petit.sqlite.Sqlite#blobOpen}
 * and should be closed when no longer needed.</p>
 *
 * @see <a href="https://sqlite.org/c3ref/blob.html">sqlite3_blob documentation</a>
 */
public record Blob(
    MemorySegment value,
    CloseCallback<Blob, SqliteExceptionBlob> closeCallback
) implements AutoCloseable {

    /**
     * Closes this BLOB handle.
     *
     * @throws SqliteExceptionBlob if an error occurred during the BLOB's lifetime
     * @see <a href="https://sqlite.org/c3ref/blob_close.html">sqlite3_blob_close documentation</a>
     */
    @Override
    public void close() throws SqliteExceptionBlob {
        closeCallback.close(this);
    }
}
