package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Flags for {@link top.petit.sqlite.Sqlite#openV2(String, OpenFlag...)}.
 *
 * <p>These flags control how the database connection is opened.</p>
 *
 * @see <a href="https://sqlite.org/c3ref/open.html">sqlite3_open_v2 documentation</a>
 */
public enum OpenFlag {

    /**
     * Open the database in read-only mode.
     * The database must already exist.
     */
    READONLY(SQLITE_OPEN_READONLY()),

    /**
     * Open the database for reading and writing.
     * The database must already exist.
     */
    READWRITE(SQLITE_OPEN_READWRITE()),

    /**
     * Create the database if it does not exist.
     * Must be combined with READWRITE.
     */
    CREATE(SQLITE_OPEN_CREATE()),

    /**
     * The filename is interpreted as a URI.
     */
    URI(SQLITE_OPEN_URI()),

    /**
     * Open an in-memory database.
     */
    MEMORY(SQLITE_OPEN_MEMORY()),

    /**
     * Use multi-thread mode (no mutex on the connection).
     */
    NOMUTEX(SQLITE_OPEN_NOMUTEX()),

    /**
     * Use serialized mode (full mutex on the connection).
     */
    FULLMUTEX(SQLITE_OPEN_FULLMUTEX()),

    /**
     * Enable shared cache mode.
     */
    SHAREDCACHE(SQLITE_OPEN_SHAREDCACHE()),

    /**
     * Disable shared cache mode.
     */
    PRIVATECACHE(SQLITE_OPEN_PRIVATECACHE()),

    /**
     * Do not follow symbolic links when opening the database file.
     */
    NOFOLLOW(SQLITE_OPEN_NOFOLLOW()),

    /**
     * Enable extended result codes.
     */
    EXRESCODE(SQLITE_OPEN_EXRESCODE());

    private final int flag;

    OpenFlag(int flag) {
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
    public static int combine(OpenFlag... flags) {
        int result = 0;
        for (var flag : flags) {
            result |= flag.flag;
        }
        return result;
    }
}
