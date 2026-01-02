package top.petit.sqlite.exception;

import top.petit.sqlite.result.Prepare;

/**
 * Exception thrown when a SQL statement preparation fails.
 *
 * @see <a href="https://sqlite.org/c3ref/prepare.html">sqlite3_prepare documentation</a>
 */
public class SqliteExceptionPrepareStatement extends Exception {

    private final Prepare result;

    public SqliteExceptionPrepareStatement(String message, Prepare result) {
        super(message);
        this.result = result;
    }

    /**
     * Returns the prepare result code.
     */
    public Prepare result() {
        return result;
    }
}
