package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Result codes returned by {@link top.petit.sqlite.Sqlite#exec}.
 *
 * <p>On success, {@link #OK} is returned. Otherwise an error code is returned.</p>
 *
 * @see <a href="https://sqlite.org/c3ref/exec.html">sqlite3_exec documentation</a>
 */
public enum Exec {

    /**
     * The operation was successful.
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
     * A constraint violation occurred.
     */
    CONSTRAINT(SQLITE_CONSTRAINT()),

    /**
     * The routine was called inappropriately.
     */
    MISUSE(SQLITE_MISUSE()),

    /**
     * Authorization denied.
     */
    AUTH(SQLITE_AUTH()),

    /**
     * Execution was aborted by callback.
     */
    ABORT(SQLITE_ABORT()),

    /**
     * An unknown or extended result code.
     */
    UNKNOWN(-1);

    private final int code;

    Exec(int code) {
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
     * Converts a native SQLite result code to an {@link Exec}.
     *
     * @param code the native result code from sqlite3_exec
     * @return the corresponding Exec
     */
    public static Exec fromCode(int code) {
        if (code == SQLITE_OK()) return OK;
        if (code == SQLITE_ERROR()) return ERROR;
        if (code == SQLITE_BUSY()) return BUSY;
        if (code == SQLITE_CONSTRAINT()) return CONSTRAINT;
        if (code == SQLITE_MISUSE()) return MISUSE;
        if (code == SQLITE_AUTH()) return AUTH;
        if (code == SQLITE_ABORT()) return ABORT;
        return UNKNOWN;
    }
}
