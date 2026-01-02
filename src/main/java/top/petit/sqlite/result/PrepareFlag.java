package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Flags for {@link top.petit.sqlite.Sqlite#prepareV3(String, PrepareFlag...)}.
 *
 * <p>These flags modify the behavior of statement preparation.</p>
 *
 * @see <a href="https://sqlite.org/c3ref/prepare.html">sqlite3_prepare documentation</a>
 */
public enum PrepareFlag {

    /**
     * Hints that the prepared statement will be retained for a long time
     * and probably reused many times.
     *
     * <p>This causes SQLite to use more memory for the statement.</p>
     */
    PERSISTENT(SQLITE_PREPARE_PERSISTENT()),

    /**
     * Causes the SQL text to be normalized before preparing.
     *
     * <p>Normalization removes comments, extra whitespace, and literal values.</p>
     */
    NORMALIZE(SQLITE_PREPARE_NORMALIZE()),

    /**
     * Prevents the statement from using virtual tables.
     *
     * <p>Returns an error if the statement would require a virtual table.</p>
     */
    NO_VTAB(SQLITE_PREPARE_NO_VTAB()),

    /**
     * Prevents the statement from being logged.
     */
    DONT_LOG(SQLITE_PREPARE_DONT_LOG());

    private final int flag;

    PrepareFlag(int flag) {
        this.flag = flag;
    }

    /**
     * Returns the native SQLite flag value.
     */
    public int flag() {
        return flag;
    }

    /**
     * Combines multiple flags into a single integer.
     *
     * @param flags the flags to combine
     * @return the combined flag value
     */
    public static int combine(PrepareFlag... flags) {
        int result = 0;
        for (var flag : flags) {
            result |= flag.flag;
        }
        return result;
    }
}
