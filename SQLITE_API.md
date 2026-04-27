# SQLite C API - Implementation Status

This document lists all SQLite C API functions and their implementation status in this Java wrapper.

- ✅ = Implemented
- ❌ = Not implemented
- 🚫 = Not applicable (internal/deprecated/16-bit variants)

## Summary

**Implemented: 64 functions**

---

## Connection Management

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_open | ✅ | `Sqlite.open(String)` |
| sqlite3_open_v2 | ✅ | `Sqlite.openV2(String, OpenFlag...)` |
| sqlite3_open16 | 🚫 | UTF-16 variant |
| sqlite3_close | ✅ | `Sqlite.closeV1()` |
| sqlite3_close_v2 | ✅ | `Sqlite.closeV2()` |

## Prepared Statements

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_prepare | ✅ | `prepare(String)` (deprecated) |
| sqlite3_prepare_v2 | ✅ | `prepareV2(String)` |
| sqlite3_prepare_v3 | ✅ | `prepareV3(String, PrepareFlag...)` |
| sqlite3_prepare16 | 🚫 | UTF-16 variant |
| sqlite3_prepare16_v2 | 🚫 | UTF-16 variant |
| sqlite3_prepare16_v3 | 🚫 | UTF-16 variant |
| sqlite3_step | ✅ | `step(PrepareStatement)` |
| sqlite3_reset | ✅ | `reset(PrepareStatement)` |
| sqlite3_finalize | ✅ | `finalize(PrepareStatement)` |

## Binding Parameters

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_bind_blob | ✅ | `bindBlob(PrepareStatement, int, byte[])` |
| sqlite3_bind_blob64 | ❌ | - |
| sqlite3_bind_double | ✅ | `bindDouble(PrepareStatement, int, double)` |
| sqlite3_bind_int | ✅ | `bindInt(PrepareStatement, int, int)` |
| sqlite3_bind_int64 | ✅ | `bindLong(PrepareStatement, int, long)` |
| sqlite3_bind_null | ✅ | `bindNull(PrepareStatement, int)` |
| sqlite3_bind_text | ✅ | `bindText(PrepareStatement, int, String)` |
| sqlite3_bind_text16 | 🚫 | UTF-16 variant |
| sqlite3_bind_text64 | ❌ | - |
| sqlite3_bind_value | ❌ | - |
| sqlite3_bind_pointer | ❌ | - |
| sqlite3_bind_zeroblob | ❌ | - |
| sqlite3_bind_zeroblob64 | ❌ | - |
| sqlite3_clear_bindings | ✅ | `clearBindings(PrepareStatement)` |
| sqlite3_bind_parameter_count | ✅ | `bindParameterCount(PrepareStatement)` |
| sqlite3_bind_parameter_index | ✅ | `bindParameterIndex(PrepareStatement, String)` |
| sqlite3_bind_parameter_name | ✅ | `bindParameterName(PrepareStatement, int)` |

## Column Access

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_column_blob | ✅ | `columnBlob(PrepareStatement, int)` |
| sqlite3_column_bytes | ✅ | Used internally by `columnBlob` |
| sqlite3_column_bytes16 | 🚫 | UTF-16 variant |
| sqlite3_column_double | ✅ | `columnDouble(PrepareStatement, int)` |
| sqlite3_column_int | ✅ | `columnInt(PrepareStatement, int)` |
| sqlite3_column_int64 | ✅ | `columnLong(PrepareStatement, int)` |
| sqlite3_column_text | ✅ | `columnText(PrepareStatement, int)` |
| sqlite3_column_text16 | 🚫 | UTF-16 variant |
| sqlite3_column_type | ✅ | `columnType(PrepareStatement, int)` |
| sqlite3_column_value | ❌ | - |
| sqlite3_column_count | ✅ | `columnCount(PrepareStatement)` |
| sqlite3_column_name | ✅ | `columnName(PrepareStatement, int)` |
| sqlite3_column_name16 | 🚫 | UTF-16 variant |
| sqlite3_column_database_name | ❌ | - |
| sqlite3_column_database_name16 | 🚫 | UTF-16 variant |
| sqlite3_column_table_name | ❌ | - |
| sqlite3_column_table_name16 | 🚫 | UTF-16 variant |
| sqlite3_column_origin_name | ❌ | - |
| sqlite3_column_origin_name16 | 🚫 | UTF-16 variant |
| sqlite3_column_decltype | ✅ | `columnDecltype(PrepareStatement, int)` |
| sqlite3_column_decltype16 | 🚫 | UTF-16 variant |
| sqlite3_data_count | ✅ | `dataCount(PrepareStatement)` |

## SQL Execution

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_exec | ✅ | `exec(String)`, `exec(String, ExecCallback)` |
| sqlite3_get_table | ❌ | - |
| sqlite3_free_table | ❌ | - |

## Error Handling

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_errcode | ✅ | `errcode()` |
| sqlite3_extended_errcode | ✅ | `extendedErrcode()` |
| sqlite3_errmsg | ✅ | `errmsg()` |
| sqlite3_errmsg16 | 🚫 | UTF-16 variant |
| sqlite3_errstr | ❌ | - |
| sqlite3_error_offset | ❌ | - |
| sqlite3_system_errno | ❌ | - |

## Statement Introspection

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_sql | ✅ | `sql(PrepareStatement)` |
| sqlite3_expanded_sql | ❌ | - |
| sqlite3_normalized_sql | ❌ | - |
| sqlite3_stmt_busy | ✅ | `stmtBusy(PrepareStatement)` |
| sqlite3_stmt_readonly | ✅ | `stmtReadonly(PrepareStatement)` |
| sqlite3_stmt_isexplain | ❌ | - |
| sqlite3_stmt_explain | ❌ | - |
| sqlite3_stmt_status | ❌ | - |
| sqlite3_stmt_scanstatus | ❌ | - |
| sqlite3_stmt_scanstatus_v2 | ❌ | - |
| sqlite3_stmt_scanstatus_reset | ❌ | - |

## Changes & Row IDs

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_changes | ✅ | `changes()` |
| sqlite3_changes64 | ✅ | `changes64()` |
| sqlite3_total_changes | ✅ | `totalChanges()` |
| sqlite3_total_changes64 | ✅ | `totalChanges64()` |
| sqlite3_last_insert_rowid | ✅ | `lastInsertRowid()` |
| sqlite3_set_last_insert_rowid | ❌ | - |

## Database Control

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_busy_timeout | ✅ | `busyTimeout(int)` |
| sqlite3_busy_handler | ❌ | - |
| sqlite3_get_autocommit | ✅ | `getAutocommit()` |
| sqlite3_interrupt | ✅ | `interrupt()` |
| sqlite3_is_interrupted | ✅ | `isInterrupted()` |
| sqlite3_setlk_timeout | ❌ | - |
| sqlite3_db_readonly | ❌ | - |
| sqlite3_db_filename | ❌ | - |
| sqlite3_db_name | ❌ | - |
| sqlite3_db_handle | ❌ | - |
| sqlite3_db_mutex | ❌ | - |
| sqlite3_db_cacheflush | ❌ | - |
| sqlite3_db_release_memory | ❌ | - |
| sqlite3_db_status | ❌ | - |
| sqlite3_db_status64 | ❌ | - |
| sqlite3_db_config | ❌ | - |
| sqlite3_limit | ❌ | - |
| sqlite3_txn_state | ❌ | - |

## Version Information

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_libversion | ✅ | `Sqlite.libversion()` |
| sqlite3_libversion_number | ✅ | `Sqlite.libversionNumber()` |
| sqlite3_sourceid | ✅ | `Sqlite.sourceid()` |
| sqlite3_version | ❌ | - |

## Compile Options

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_compileoption_get | ❌ | - |
| sqlite3_compileoption_used | ❌ | - |

## Utilities

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_complete | ✅ | `Sqlite.complete(String)` |
| sqlite3_complete16 | 🚫 | UTF-16 variant |
| sqlite3_threadsafe | ✅ | `Sqlite.threadsafe()` |
| sqlite3_sleep | ❌ | - |
| sqlite3_randomness | ❌ | - |
| sqlite3_enable_shared_cache | ❌ | - |
| sqlite3_mprintf | ❌ | - |
| sqlite3_snprintf | ❌ | - |
| sqlite3_vmprintf | ❌ | - |
| sqlite3_vsnprintf | ❌ | - |
| sqlite3_strglob | ❌ | - |
| sqlite3_strlike | ❌ | - |
| sqlite3_stricmp | ❌ | - |
| sqlite3_strnicmp | ❌ | - |
| sqlite3_log | ❌ | - |

## Blob I/O

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_blob_open | ✅ | `blobOpen(String, String, String, long, boolean)` |
| sqlite3_blob_close | ✅ | `blobClose(Blob)` |
| sqlite3_blob_bytes | ✅ | `blobBytes(Blob)` |
| sqlite3_blob_read | ✅ | `blobRead(Blob, int, int)` |
| sqlite3_blob_write | ✅ | `blobWrite(Blob, byte[], int)` |
| sqlite3_blob_reopen | ✅ | `blobReopen(Blob, long)` |

## Backup API

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_backup_init | ✅ | `backupInit(Sqlite, String, String)` |
| sqlite3_backup_step | ✅ | `backupStep(Backup, int)` |
| sqlite3_backup_finish | ✅ | `backupFinish(Backup)` |
| sqlite3_backup_remaining | ✅ | `backupRemaining(Backup)` |
| sqlite3_backup_pagecount | ✅ | `backupPagecount(Backup)` |

## Snapshot API

Requires `SQLITE_ENABLE_SNAPSHOT` compile flag (enabled).

| Function | Status | Java Method | Notes |
|----------|--------|-------------|-------|
| sqlite3_snapshot_get | ❌ | - | constructor |
| sqlite3_snapshot_open | ❌ | - | |
| sqlite3_snapshot_recover | ❌ | - | |
| sqlite3_snapshot_cmp | ❌ | - | |
| sqlite3_snapshot_free | ❌ | - | destructor |

## Hooks & Callbacks

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_commit_hook | ❌ | - |
| sqlite3_rollback_hook | ❌ | - |
| sqlite3_update_hook | ❌ | - |
| sqlite3_preupdate_hook | ❌ | - |
| sqlite3_preupdate_old | ❌ | - |
| sqlite3_preupdate_new | ❌ | - |
| sqlite3_preupdate_count | ❌ | - |
| sqlite3_preupdate_depth | ❌ | - |
| sqlite3_preupdate_blobwrite | ❌ | - |
| sqlite3_autovacuum_pages | ❌ | - |
| sqlite3_progress_handler | ❌ | - |
| sqlite3_trace | ❌ | Deprecated |
| sqlite3_trace_v2 | ❌ | - |
| sqlite3_profile | ❌ | Deprecated |
| sqlite3_wal_hook | ❌ | - |
| sqlite3_wal_autocheckpoint | ❌ | - |
| sqlite3_wal_checkpoint | ❌ | - |
| sqlite3_wal_checkpoint_v2 | ❌ | - |

## User-Defined Functions

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_create_function | ❌ | - |
| sqlite3_create_function16 | 🚫 | UTF-16 variant |
| sqlite3_create_function_v2 | ❌ | - |
| sqlite3_create_window_function | ❌ | - |
| sqlite3_aggregate_context | ❌ | - |
| sqlite3_aggregate_count | ❌ | Deprecated |
| sqlite3_user_data | ❌ | - |
| sqlite3_context_db_handle | ❌ | - |
| sqlite3_get_auxdata | ❌ | - |
| sqlite3_set_auxdata | ❌ | - |
| sqlite3_result_blob | ❌ | - |
| sqlite3_result_blob64 | ❌ | - |
| sqlite3_result_double | ❌ | - |
| sqlite3_result_error | ❌ | - |
| sqlite3_result_error_toobig | ❌ | - |
| sqlite3_result_error_nomem | ❌ | - |
| sqlite3_result_error_code | ❌ | - |
| sqlite3_result_int | ❌ | - |
| sqlite3_result_int64 | ❌ | - |
| sqlite3_result_null | ❌ | - |
| sqlite3_result_text | ❌ | - |
| sqlite3_result_text64 | ❌ | - |
| sqlite3_result_text16 | 🚫 | UTF-16 variant |
| sqlite3_result_text16le | 🚫 | UTF-16 variant |
| sqlite3_result_text16be | 🚫 | UTF-16 variant |
| sqlite3_result_value | ❌ | - |
| sqlite3_result_pointer | ❌ | - |
| sqlite3_result_zeroblob | ❌ | - |
| sqlite3_result_zeroblob64 | ❌ | - |
| sqlite3_result_subtype | ❌ | - |
| sqlite3_value_blob | ❌ | - |
| sqlite3_value_bytes | ❌ | - |
| sqlite3_value_double | ❌ | - |
| sqlite3_value_int | ❌ | - |
| sqlite3_value_int64 | ❌ | - |
| sqlite3_value_pointer | ❌ | - |
| sqlite3_value_text | ❌ | - |
| sqlite3_value_text16 | 🚫 | UTF-16 variant |
| sqlite3_value_type | ❌ | - |
| sqlite3_value_numeric_type | ❌ | - |
| sqlite3_value_nochange | ❌ | - |
| sqlite3_value_frombind | ❌ | - |
| sqlite3_value_dup | ❌ | - |
| sqlite3_value_free | ❌ | - |
| sqlite3_value_subtype | ❌ | - |
| sqlite3_value_encoding | ❌ | - |

## Collation

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_create_collation | ❌ | - |
| sqlite3_create_collation16 | 🚫 | UTF-16 variant |
| sqlite3_create_collation_v2 | ❌ | - |
| sqlite3_collation_needed | ❌ | - |
| sqlite3_collation_needed16 | 🚫 | UTF-16 variant |

## Virtual Tables

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_create_module | ❌ | - |
| sqlite3_create_module_v2 | ❌ | - |
| sqlite3_declare_vtab | ❌ | - |
| sqlite3_drop_modules | ❌ | - |
| sqlite3_overload_function | ❌ | - |
| sqlite3_vtab_config | ❌ | - |
| sqlite3_vtab_on_conflict | ❌ | - |
| sqlite3_vtab_collation | ❌ | - |
| sqlite3_vtab_nochange | ❌ | - |
| sqlite3_vtab_distinct | ❌ | - |
| sqlite3_vtab_in | ❌ | - |
| sqlite3_vtab_in_first | ❌ | - |
| sqlite3_vtab_in_next | ❌ | - |
| sqlite3_vtab_rhs_value | ❌ | - |

## Authorization

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_set_authorizer | ❌ | - |

## Extensions

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_load_extension | ❌ | - |
| sqlite3_enable_load_extension | ❌ | - |
| sqlite3_auto_extension | ❌ | - |
| sqlite3_cancel_auto_extension | ❌ | - |
| sqlite3_reset_auto_extension | ❌ | - |
| sqlite3_carray_bind | ❌ | carray extension |
| sqlite3_carray_bind_v2 | ❌ | carray extension — added in 3.50.0 |

## Serialize/Deserialize

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_serialize | ❌ | - |
| sqlite3_deserialize | ❌ | - |

## URI & Filename

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_uri_parameter | ❌ | - |
| sqlite3_uri_boolean | ❌ | - |
| sqlite3_uri_int64 | ❌ | - |
| sqlite3_uri_key | ❌ | - |
| sqlite3_filename_database | ❌ | - |
| sqlite3_filename_journal | ❌ | - |
| sqlite3_filename_wal | ❌ | - |
| sqlite3_create_filename | ❌ | - |
| sqlite3_free_filename | ❌ | - |
| sqlite3_database_file_object | ❌ | - |

## Dynamic String (sqlite3_str)

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_str_new | ❌ | - |
| sqlite3_str_finish | ❌ | - |
| sqlite3_str_appendf | ❌ | - |
| sqlite3_str_vappendf | ❌ | - |
| sqlite3_str_append | ❌ | - |
| sqlite3_str_appendall | ❌ | - |
| sqlite3_str_appendchar | ❌ | - |
| sqlite3_str_reset | ❌ | - |
| sqlite3_str_errcode | ❌ | - |
| sqlite3_str_length | ❌ | - |
| sqlite3_str_value | ❌ | - |
| sqlite3_str_truncate | ❌ | - |
| sqlite3_str_free | ❌ | - |

## VFS

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_vfs_find | ❌ | - |
| sqlite3_vfs_register | ❌ | - |
| sqlite3_vfs_unregister | ❌ | - |
| sqlite3_os_init | ❌ | - |
| sqlite3_os_end | ❌ | - |

## Mutex

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_mutex_alloc | ❌ | - |
| sqlite3_mutex_free | ❌ | - |
| sqlite3_mutex_enter | ❌ | - |
| sqlite3_mutex_try | ❌ | - |
| sqlite3_mutex_leave | ❌ | - |
| sqlite3_mutex_held | ❌ | - |
| sqlite3_mutex_notheld | ❌ | - |

## Memory Management

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_malloc | ❌ | Internal |
| sqlite3_malloc64 | ❌ | Internal |
| sqlite3_realloc | ❌ | Internal |
| sqlite3_realloc64 | ❌ | Internal |
| sqlite3_free | ✅ | Used internally |
| sqlite3_msize | ❌ | Internal |
| sqlite3_memory_used | ❌ | - |
| sqlite3_memory_highwater | ❌ | - |
| sqlite3_soft_heap_limit64 | ❌ | - |
| sqlite3_hard_heap_limit64 | ❌ | - |
| sqlite3_release_memory | ❌ | - |
| sqlite3_status | ❌ | - |
| sqlite3_status64 | ❌ | - |

## Configuration

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_config | ❌ | - |
| sqlite3_initialize | ❌ | - |
| sqlite3_shutdown | ❌ | - |
| sqlite3_extended_result_codes | ❌ | - |

## Miscellaneous

| Function | Status | Java Method |
|----------|--------|-------------|
| sqlite3_next_stmt | ❌ | - |
| sqlite3_table_column_metadata | ❌ | - |
| sqlite3_keyword_count | ❌ | - |
| sqlite3_keyword_name | ❌ | - |
| sqlite3_keyword_check | ❌ | - |
| sqlite3_file_control | ❌ | - |
| sqlite3_unlock_notify | ❌ | - |
| sqlite3_get_clientdata | ❌ | - |
| sqlite3_set_clientdata | ❌ | - |

---

## Implementation Priority Suggestions

### High Priority (Common Usage) - ✅ DONE
- ~~`sqlite3_open_v2` - Open with flags (READONLY, CREATE, etc.)~~ ✅
- ~~`sqlite3_blob_*` - Blob I/O for large binary data~~ ✅
- ~~`sqlite3_backup_*` - Database backup functionality~~ ✅

### Medium Priority
- `sqlite3_commit_hook` / `sqlite3_rollback_hook` / `sqlite3_update_hook` - Callbacks
- `sqlite3_trace_v2` - Query tracing/profiling
- `sqlite3_create_function_v2` - User-defined functions
- `sqlite3_expanded_sql` - SQL with bound values
- `sqlite3_setlk_timeout` - Lock wait timeout (added in 3.45.0)
- `sqlite3_snapshot_*` - Snapshot isolation (**compile flag enabled**)

### Lower Priority
- Virtual tables
- Collation
- Authorization
- Serialize/Deserialize
- Dynamic String API (`sqlite3_str_*`)
- VFS / Mutex
