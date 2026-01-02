package top.petit.sqlite.exception;

import top.petit.sqlite.result.Bind;

/**
 * Exception thrown when binding a parameter to a prepared statement fails.
 *
 * @see <a href="https://sqlite.org/c3ref/bind_blob.html">sqlite3_bind documentation</a>
 */
public class SqliteExceptionBind extends Exception {

    private final Bind result;

    public SqliteExceptionBind(String message, Bind result) {
        super(message);
        this.result = result;
    }

    /**
     * Returns the bind result code.
     */
    public Bind result() {
        return result;
    }
}
