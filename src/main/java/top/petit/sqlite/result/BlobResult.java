package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Result codes returned by BLOB operations.
 *
 * @see <a href="https://sqlite.org/c3ref/blob_open.html">sqlite3_blob documentation</a>
 */
public enum BlobResult {

    /**
     * The operation was successful.
     */
    OK(SQLITE_OK()),

    /**
     * A runtime error occurred.
     */
    ERROR(SQLITE_ERROR()),

    /**
     * The BLOB handle has been invalidated.
     */
    ABORT(SQLITE_ABORT()),

    /**
     * The database file is locked.
     */
    BUSY(SQLITE_BUSY()),

    /**
     * Out of memory.
     */
    NOMEM(SQLITE_NOMEM()),

    /**
     * Attempt to write a readonly BLOB.
     */
    READONLY(SQLITE_READONLY()),

    /**
     * The routine was called inappropriately.
     */
    MISUSE(SQLITE_MISUSE()),

    /**
     * An unknown or extended result code.
     */
    UNKNOWN(-1);

    private final int code;

    BlobResult(int code) {
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
     * Converts a native SQLite result code to a {@link BlobResult}.
     *
     * @param code the native result code from sqlite3_blob_*
     * @return the corresponding BlobResult
     */
    public static BlobResult fromCode(int code) {
        if (code == SQLITE_OK()) return OK;
        if (code == SQLITE_ERROR()) return ERROR;
        if (code == SQLITE_ABORT()) return ABORT;
        if (code == SQLITE_BUSY()) return BUSY;
        if (code == SQLITE_NOMEM()) return NOMEM;
        if (code == SQLITE_READONLY()) return READONLY;
        if (code == SQLITE_MISUSE()) return MISUSE;
        return UNKNOWN;
    }
}
