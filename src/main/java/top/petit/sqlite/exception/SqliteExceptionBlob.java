package top.petit.sqlite.exception;

import top.petit.sqlite.result.BlobResult;

/**
 * Exception thrown when a BLOB operation fails.
 *
 * @see <a href="https://sqlite.org/c3ref/blob_open.html">sqlite3_blob_open documentation</a>
 */
public class SqliteExceptionBlob extends Exception {

    private final BlobResult result;

    public SqliteExceptionBlob(String message, BlobResult result) {
        super(message);
        this.result = result;
    }

    /**
     * Returns the BLOB operation result.
     */
    public BlobResult result() {
        return result;
    }
}
