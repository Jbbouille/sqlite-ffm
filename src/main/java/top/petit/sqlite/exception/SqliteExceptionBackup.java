package top.petit.sqlite.exception;

import top.petit.sqlite.result.BackupFinish;

/**
 * Exception thrown when a backup operation fails.
 *
 * @see <a href="https://sqlite.org/c3ref/backup_finish.html">sqlite3_backup documentation</a>
 */
public class SqliteExceptionBackup extends Exception {

    private final BackupFinish result;

    public SqliteExceptionBackup(String message, BackupFinish result) {
        super(message);
        this.result = result;
    }

    /**
     * Returns the backup operation result.
     */
    public BackupFinish result() {
        return result;
    }
}
