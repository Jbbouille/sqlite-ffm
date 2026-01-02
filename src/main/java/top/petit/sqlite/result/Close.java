package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Result codes returned by {@link top.petit.sqlite.Sqlite#closeV1} and {@link top.petit.sqlite.Sqlite#closeV2}.
 *
 * @see <a href="https://sqlite.org/c3ref/close.html">sqlite3_close documentation</a>
 */
public enum Close {

    /**
     * The database was closed successfully.
     */
    OK(SQLITE_OK()),

    /**
     * The database connection is busy (has unfinalized statements, open BLOBs, or unfinished backups).
     */
    BUSY(SQLITE_BUSY()),

    /**
     * Out of memory.
     */
    NOMEM(SQLITE_NOMEM()),

    /**
     * An unknown or extended result code.
     */
    UNKNOWN(-1);

    private final int code;

    Close(int code) {
        this.code = code;
    }

    /**
     * Returns the native SQLite result code.
     */
    public int code() {
        return code;
    }

    /**
     * Returns {@code true} if the result indicates success.
     */
    public boolean isOk() {
        return this == OK;
    }

    /**
     * Converts a native SQLite result code to a {@link Close}.
     *
     * @param code the native result code from sqlite3_close
     * @return the corresponding Close
     */
    public static Close fromCode(int code) {
        if (code == SQLITE_OK()) return OK;
        if (code == SQLITE_BUSY()) return BUSY;
        if (code == SQLITE_NOMEM()) return NOMEM;
        return UNKNOWN;
    }
}
