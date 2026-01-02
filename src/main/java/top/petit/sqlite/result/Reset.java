package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Result codes returned by {@link top.petit.sqlite.Sqlite#reset}.
 *
 * @see <a href="https://sqlite.org/c3ref/reset.html">sqlite3_reset documentation</a>
 */
public enum Reset {

    /**
     * The statement was reset successfully.
     */
    OK(SQLITE_OK()),

    /**
     * A runtime error occurred during the most recent step.
     */
    ERROR(SQLITE_ERROR()),

    /**
     * The operation was aborted.
     */
    ABORT(SQLITE_ABORT()),

    /**
     * The database file is locked.
     */
    BUSY(SQLITE_BUSY()),

    /**
     * A constraint violation occurred.
     */
    CONSTRAINT(SQLITE_CONSTRAINT()),

    /**
     * The routine was called inappropriately.
     */
    MISUSE(SQLITE_MISUSE()),

    /**
     * An unknown or extended result code.
     */
    UNKNOWN(-1);

    private final int code;

    Reset(int code) {
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
     * Converts a native SQLite result code to a {@link Reset}.
     *
     * @param code the native result code from sqlite3_reset
     * @return the corresponding Reset
     */
    public static Reset fromCode(int code) {
        if (code == SQLITE_OK()) return OK;
        if (code == SQLITE_ERROR()) return ERROR;
        if (code == SQLITE_ABORT()) return ABORT;
        if (code == SQLITE_BUSY()) return BUSY;
        if (code == SQLITE_CONSTRAINT()) return CONSTRAINT;
        if (code == SQLITE_MISUSE()) return MISUSE;
        return UNKNOWN;
    }
}
