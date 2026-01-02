package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Result codes returned by {@link top.petit.sqlite.Sqlite#finalize}.
 *
 * <p>If the most recent evaluation of the statement encountered no errors
 * or if the statement is never been evaluated, then {@link #OK} is returned.</p>
 *
 * <p>If the most recent evaluation of statement failed, then the appropriate
 * error code is returned.</p>
 *
 * @see <a href="https://sqlite.org/c3ref/finalize.html">sqlite3_finalize documentation</a>
 */
public enum Finalize {

    /**
     * The statement was finalized successfully.
     */
    OK(SQLITE_OK()),

    /**
     * The operation was aborted.
     */
    ABORT(SQLITE_ABORT()),

    /**
     * The database file is locked.
     */
    BUSY(SQLITE_BUSY()),

    /**
     * A runtime error occurred.
     */
    ERROR(SQLITE_ERROR()),

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

    Finalize(int code) {
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
     * Converts a native SQLite result code to a {@link Finalize}.
     *
     * @param code the native result code from sqlite3_finalize
     * @return the corresponding Finalize
     */
    public static Finalize fromCode(int code) {
        if (code == SQLITE_OK()) return OK;
        if (code == SQLITE_ABORT()) return ABORT;
        if (code == SQLITE_BUSY()) return BUSY;
        if (code == SQLITE_ERROR()) return ERROR;
        if (code == SQLITE_CONSTRAINT()) return CONSTRAINT;
        if (code == SQLITE_MISUSE()) return MISUSE;
        return UNKNOWN;
    }
}
