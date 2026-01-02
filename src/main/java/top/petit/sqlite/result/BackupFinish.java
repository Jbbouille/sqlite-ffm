package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Result codes returned by {@link top.petit.sqlite.Sqlite#backupFinish}.
 *
 * @see <a href="https://sqlite.org/c3ref/backup_finish.html">sqlite3_backup_finish documentation</a>
 */
public enum BackupFinish {

    /**
     * The backup was finished successfully.
     */
    OK(SQLITE_OK()),

    /**
     * The database file is locked.
     */
    BUSY(SQLITE_BUSY()),

    /**
     * A table in the source database was modified during the backup.
     */
    LOCKED(SQLITE_LOCKED()),

    /**
     * Out of memory.
     */
    NOMEM(SQLITE_NOMEM()),

    /**
     * An unknown or extended result code.
     */
    UNKNOWN(-1);

    private final int code;

    BackupFinish(int code) {
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
     * Converts a native SQLite result code to a {@link BackupFinish}.
     *
     * @param code the native result code from sqlite3_backup_finish
     * @return the corresponding BackupFinish
     */
    public static BackupFinish fromCode(int code) {
        if (code == SQLITE_OK()) return OK;
        if (code == SQLITE_BUSY()) return BUSY;
        if (code == SQLITE_LOCKED()) return LOCKED;
        if (code == SQLITE_NOMEM()) return NOMEM;
        return UNKNOWN;
    }
}
