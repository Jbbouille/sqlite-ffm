package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Result codes returned by bind methods.
 *
 * <p>On success, {@link #OK} is returned. Otherwise an error code is returned.</p>
 *
 * @see <a href="https://sqlite.org/c3ref/bind_blob.html">sqlite3_bind documentation</a>
 */
public enum Bind {

    /**
     * The bind operation was successful.
     */
    OK(SQLITE_OK()),

    /**
     * A runtime error occurred.
     */
    ERROR(SQLITE_ERROR()),

    /**
     * String or BLOB exceeds size limit.
     */
    TOOBIG(SQLITE_TOOBIG()),

    /**
     * Out of memory.
     */
    NOMEM(SQLITE_NOMEM()),

    /**
     * Index out of range.
     */
    RANGE(SQLITE_RANGE()),

    /**
     * The routine was called inappropriately.
     */
    MISUSE(SQLITE_MISUSE()),

    /**
     * An unknown or extended result code.
     */
    UNKNOWN(-1);

    private final int code;

    Bind(int code) {
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
     * Converts a native SQLite result code to a {@link Bind}.
     *
     * @param code the native result code from sqlite3_bind_*
     * @return the corresponding Bind
     */
    public static Bind fromCode(int code) {
        if (code == SQLITE_OK()) return OK;
        if (code == SQLITE_ERROR()) return ERROR;
        if (code == SQLITE_TOOBIG()) return TOOBIG;
        if (code == SQLITE_NOMEM()) return NOMEM;
        if (code == SQLITE_RANGE()) return RANGE;
        if (code == SQLITE_MISUSE()) return MISUSE;
        return UNKNOWN;
    }
}
