package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * Result codes returned by {@link top.petit.sqlite.Sqlite#backupStep}.
 *
 * @see <a href="https://sqlite.org/c3ref/backup_finish.html">sqlite3_backup_step documentation</a>
 */
public enum BackupStep {

    /**
     * The operation completed successfully.
     */
    OK(SQLITE_OK()),

    /**
     * More pages remain to be copied.
     */
    BUSY(SQLITE_BUSY()),

    /**
     * The source or destination database was locked.
     */
    LOCKED(SQLITE_LOCKED()),

    /**
     * The backup is complete.
     */
    DONE(SQLITE_DONE()),

    /**
     * An unknown or other error occurred.
     */
    UNKNOWN(-1);

    private final int code;

    BackupStep(int code) {
        this.code = code;
    }

    /**
     * Returns the native SQLite result code.
     */
    public int code() {
        return code;
    }

    /**
     * Returns {@code true} if the backup is complete.
     */
    public boolean isDone() {
        return this == DONE;
    }

    /**
     * Returns {@code true} if more pages need to be copied.
     */
    public boolean needsMoreWork() {
        return this == OK || this == BUSY || this == LOCKED;
    }

    /**
     * Converts a native SQLite result code to a {@link BackupStep}.
     *
     * @param code the native result code from sqlite3_backup_step
     * @return the corresponding BackupStep
     */
    public static BackupStep fromCode(int code) {
        if (code == SQLITE_OK()) return OK;
        if (code == SQLITE_BUSY()) return BUSY;
        if (code == SQLITE_LOCKED()) return LOCKED;
        if (code == SQLITE_DONE()) return DONE;
        return UNKNOWN;
    }
}
