package top.petit.sqlite.exception;

import top.petit.sqlite.result.Finalize;

/**
 * Exception thrown when finalizing a prepared statement fails.
 *
 * @see <a href="https://sqlite.org/c3ref/finalize.html">sqlite3_finalize documentation</a>
 */
public class SqliteExceptionFinalize extends Exception {

    private final Finalize result;

    public SqliteExceptionFinalize(String message, Finalize result) {
        super(message);
        this.result = result;
    }

    /**
     * Returns the finalize result code.
     */
    public Finalize result() {
        return result;
    }
}
