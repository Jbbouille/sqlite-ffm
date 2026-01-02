package top.petit.sqlite.exception;

import top.petit.sqlite.result.Close;

/**
 * Exception thrown when closing a database connection fails.
 *
 * @see <a href="https://sqlite.org/c3ref/close.html">sqlite3_close documentation</a>
 */
public class SqliteExceptionClose extends Exception {

    private final Close result;

    public SqliteExceptionClose(String message, Close result) {
        super(message);
        this.result = result;
    }

    /**
     * Returns the SQLite close result.
     */
    public Close result() {
        return result;
    }
}
