package top.petit.sqlite.callback;

/**
 * Callback interface for {@link top.petit.sqlite.Sqlite#walHook(WalHookCallback)}.
 *
 * <p>Invoked after each successful WAL write transaction. The return value controls
 * whether SQLite should attempt an automatic checkpoint: return 0 ({@code SQLITE_OK})
 * to allow it, or any non-zero value to suppress it.</p>
 *
 * @see <a href="https://sqlite.org/c3ref/wal_hook.html">sqlite3_wal_hook documentation</a>
 */
@FunctionalInterface
public interface WalHookCallback {

    /**
     * Called after each successful WAL write.
     *
     * @param dbName    the name of the database that was written (e.g. "main")
     * @param pageCount the number of pages in the WAL file after the commit
     * @return 0 to allow automatic checkpointing, non-zero to suppress it
     */
    int onWalCommit(String dbName, int pageCount);
}
