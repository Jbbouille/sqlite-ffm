package top.petit.sqlite.exception;

import top.petit.sqlite.result.Step;

/**
 * Exception thrown when stepping through a prepared statement fails.
 *
 * @see <a href="https://sqlite.org/c3ref/step.html">sqlite3_step documentation</a>
 */
public class SqliteExceptionStep extends Exception {

    private final Step result;

    public SqliteExceptionStep(String message, Step result) {
        super(message);
        this.result = result;
    }

    /**
     * Returns the step result code.
     */
    public Step result() {
        return result;
    }
}
