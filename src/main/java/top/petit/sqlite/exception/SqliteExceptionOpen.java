package top.petit.sqlite.exception;

import top.petit.sqlite.result.Open;

/**
 * Exception thrown when opening a database fails.
 *
 * @see <a href="https://sqlite.org/c3ref/open.html">sqlite3_open documentation</a>
 */
public class SqliteExceptionOpen extends Exception {

    private final Open result;

    public SqliteExceptionOpen(String message, Open result) {
        super(message);
        this.result = result;
    }

    /**
     * Returns the open result code.
     */
    public Open result() {
        return result;
    }
}
