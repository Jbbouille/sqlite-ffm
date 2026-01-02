package top.petit.sqlite.result;

import static top.petit.sqlite.gen.sqlite3_h.*;

/**
 * SQLite column types returned by {@link top.petit.sqlite.Sqlite#columnType}.
 *
 * @see <a href="https://sqlite.org/c3ref/column_blob.html">sqlite3_column documentation</a>
 */
public enum ColumnType {

    /**
     * 64-bit signed integer.
     */
    INTEGER(SQLITE_INTEGER()),

    /**
     * 64-bit IEEE floating point number.
     */
    FLOAT(SQLITE_FLOAT()),

    /**
     * String.
     */
    TEXT(SQLITE_TEXT()),

    /**
     * Binary large object.
     */
    BLOB(SQLITE_BLOB()),

    /**
     * NULL value.
     */
    NULL(SQLITE_NULL()),

    /**
     * Unknown type.
     */
    UNKNOWN(-1);

    private final int code;

    ColumnType(int code) {
        this.code = code;
    }

    /**
     * Returns the native SQLite type code.
     */
    public int code() {
        return code;
    }

    /**
     * Converts a native SQLite type code to a {@link ColumnType}.
     *
     * @param code the native type code from sqlite3_column_type
     * @return the corresponding ColumnType
     */
    public static ColumnType fromCode(int code) {
        if (code == SQLITE_INTEGER()) return INTEGER;
        if (code == SQLITE_FLOAT()) return FLOAT;
        if (code == SQLITE_TEXT()) return TEXT;
        if (code == SQLITE_BLOB()) return BLOB;
        if (code == SQLITE_NULL()) return NULL;
        return UNKNOWN;
    }
}
