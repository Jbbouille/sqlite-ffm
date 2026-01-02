package top.petit.sqlite.object;

import java.lang.foreign.MemorySegment;
import top.petit.sqlite.callback.CloseCallback;
import top.petit.sqlite.exception.SqliteExceptionBackup;

/**
 * Represents an online backup operation.
 *
 * <p>A backup is initialized using {@link top.petit.sqlite.Sqlite#backupInit}
 * and should be finished when the backup is complete.</p>
 *
 * @see <a href="https://sqlite.org/c3ref/backup.html">sqlite3_backup documentation</a>
 */
public record Backup(
    MemorySegment value,
    CloseCallback<Backup, SqliteExceptionBackup> closeCallback
) implements AutoCloseable {

    /**
     * Finishes this backup operation.
     *
     * @throws SqliteExceptionBackup if an error occurred during the backup
     * @see <a href="https://sqlite.org/c3ref/backup_finish.html">sqlite3_backup_finish documentation</a>
     */
    @Override
    public void close() throws SqliteExceptionBackup {
        closeCallback.close(this);
    }
}
