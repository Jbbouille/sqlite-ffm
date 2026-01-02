package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Result codes returned by {@link top.petit.sqlite.Sqlite#open}.
 *
 * @see <a href="https://sqlite.org/c3ref/open.html">sqlite3_open documentation</a>
 */
public enum Open {

    /**
     * The database was opened successfully.
     */
    OK(SQLITE_OK()),

    /**
     * Unable to open the database file.
     */
    CANTOPEN(SQLITE_CANTOPEN()),

    /**
     * The file is not a database or is encrypted.
     */
    NOTADB(SQLITE_NOTADB()),

    /**
     * The database file is malformed.
     */
    CORRUPT(SQLITE_CORRUPT()),

    /**
     * Access permission denied.
     */
    PERM(SQLITE_PERM()),

    /**
     * Attempt to write a readonly database.
     */
    READONLY(SQLITE_READONLY()),

    /**
     * Out of memory.
     */
    NOMEM(SQLITE_NOMEM()),

    /**
     * The database file is locked.
     */
    BUSY(SQLITE_BUSY()),

    /**
     * An unknown or extended result code.
     */
    UNKNOWN(-1);

    private final int code;

    Open(int code) {
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
     * Converts a native SQLite result code to an {@link Open}.
     *
     * @param code the native result code from sqlite3_open
     * @return the corresponding Open
     */
    public static Open fromCode(int code) {
        if (code == SQLITE_OK()) return OK;
        if (code == SQLITE_CANTOPEN()) return CANTOPEN;
        if (code == SQLITE_NOTADB()) return NOTADB;
        if (code == SQLITE_CORRUPT()) return CORRUPT;
        if (code == SQLITE_PERM()) return PERM;
        if (code == SQLITE_READONLY()) return READONLY;
        if (code == SQLITE_NOMEM()) return NOMEM;
        if (code == SQLITE_BUSY()) return BUSY;
        return UNKNOWN;
    }
}
