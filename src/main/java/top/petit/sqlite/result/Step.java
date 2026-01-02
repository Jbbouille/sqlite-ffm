package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Result codes returned by {@link top.petit.sqlite.Sqlite#step}.
 *
 * @see <a href="https://sqlite.org/c3ref/step.html">sqlite3_step documentation</a>
 */
public enum Step {

    /**
     * Another row of output is available.
     */
    ROW(SQLITE_ROW()),

    /**
     * The statement has finished executing successfully.
     */
    DONE(SQLITE_DONE()),

    /**
     * The database file is locked.
     */
    BUSY(SQLITE_BUSY()),

    /**
     * A runtime error occurred.
     */
    ERROR(SQLITE_ERROR()),

    /**
     * The database file is malformed.
     */
    CORRUPT(SQLITE_CORRUPT()),

    /**
     * The routine was called inappropriately.
     */
    MISUSE(SQLITE_MISUSE()),

    /**
     * An unknown or extended result code.
     */
    UNKNOWN(-1);

    private final int code;

    Step(int code) {
        this.code = code;
    }

    /**
     * Returns the native SQLite result code.
     */
    public int code() {
        return code;
    }

    /**
     * Returns {@code true} if another row of output is available.
     */
    public boolean hasRow() {
        return this == ROW;
    }

    /**
     * Returns {@code true} if the statement has finished executing.
     */
    public boolean isDone() {
        return this == DONE;
    }

    /**
     * Converts a native SQLite result code to a {@link Step}.
     *
     * @param code the native result code from sqlite3_step
     * @return the corresponding Step
     */
    public static Step fromCode(int code) {
        if (code == SQLITE_ROW()) return ROW;
        if (code == SQLITE_DONE()) return DONE;
        if (code == SQLITE_BUSY()) return BUSY;
        if (code == SQLITE_ERROR()) return ERROR;
        if (code == SQLITE_CORRUPT()) return CORRUPT;
        if (code == SQLITE_MISUSE()) return MISUSE;
        return UNKNOWN;
    }
}
