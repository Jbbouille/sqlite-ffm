package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Result codes returned by {@link top.petit.sqlite.Sqlite#prepareV2}.
 *
 * <p>On success, {@link #OK} is returned. Otherwise an error code is returned.</p>
 *
 * @see <a href="https://sqlite.org/c3ref/prepare.html">sqlite3_prepare documentation</a>
 */
public enum Prepare {

    /**
     * The statement was prepared successfully.
     */
    OK(SQLITE_OK()),

    /**
     * A runtime error occurred.
     */
    ERROR(SQLITE_ERROR()),

    /**
     * The database file is locked.
     */
    BUSY(SQLITE_BUSY()),

    /**
     * Unable to open database file.
     */
    CANTOPEN(SQLITE_CANTOPEN()),

    /**
     * The database schema has changed.
     */
    SCHEMA(SQLITE_SCHEMA()),

    /**
     * String or BLOB exceeds size limit.
     */
    TOOBIG(SQLITE_TOOBIG()),

    /**
     * The routine was called inappropriately.
     */
    MISUSE(SQLITE_MISUSE()),

    /**
     * An unknown or extended result code.
     */
    UNKNOWN(-1);

    private final int code;

    Prepare(int code) {
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
     * Converts a native SQLite result code to a {@link Prepare}.
     *
     * @param code the native result code from sqlite3_prepare_v2
     * @return the corresponding Prepare
     */
    public static Prepare fromCode(int code) {
        if (code == SQLITE_OK()) return OK;
        if (code == SQLITE_ERROR()) return ERROR;
        if (code == SQLITE_BUSY()) return BUSY;
        if (code == SQLITE_CANTOPEN()) return CANTOPEN;
        if (code == SQLITE_SCHEMA()) return SCHEMA;
        if (code == SQLITE_TOOBIG()) return TOOBIG;
        if (code == SQLITE_MISUSE()) return MISUSE;
        return UNKNOWN;
    }
}
