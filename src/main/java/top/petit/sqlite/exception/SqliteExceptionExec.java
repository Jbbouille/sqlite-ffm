package top.petit.sqlite.exception;

import top.petit.sqlite.result.Exec;

/**
 * Exception thrown when executing SQL fails.
 *
 * @see <a href="https://sqlite.org/c3ref/exec.html">sqlite3_exec documentation</a>
 */
public class SqliteExceptionExec extends Exception {

    private final Exec result;
    private final String errorMessage;

    public SqliteExceptionExec(String message, Exec result, String errorMessage) {
        super(message);
        this.result = result;
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the exec result code.
     */
    public Exec result() {
        return result;
    }

    /**
     * Returns the SQLite error message, or null if not available.
     */
    public String errorMessage() {
        return errorMessage;
    }
}
