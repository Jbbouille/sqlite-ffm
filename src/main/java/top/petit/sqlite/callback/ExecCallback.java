package top.petit.sqlite.callback;

/**
 * Callback interface for {@link top.petit.sqlite.Sqlite#exec(String, ExecCallback)}.
 *
 * <p>This callback is invoked for each row returned by a SELECT statement
 * executed via exec.</p>
 *
 * @see <a href="https://sqlite.org/c3ref/exec.html">sqlite3_exec documentation</a>
 */
@FunctionalInterface
public interface ExecCallback {

    /**
     * Called for each row in the result set.
     *
     * @param columnNames the names of the columns
     * @param values the values of the columns (may contain nulls)
     * @return true to continue, false to abort the query
     */
    boolean onRow(String[] columnNames, String[] values);
}
