from database.connection import DatabaseConnection


class DatabaseSchema:
    def __init__(self):
        self.db = DatabaseConnection()

    def create_tables(self):
        tables = [
            self._samples_table(),
            self._test_records_table(),
            self._test_record_changes_table(),
            self._attachments_table(),
            self._audit_logs_table(),
            self._system_settings_table()
        ]

        for table_sql in tables:
            self.db.execute(table_sql)

    def _samples_table(self) -> str:
        return """
        CREATE TABLE IF NOT EXISTS samples (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            sample_no TEXT NOT NULL UNIQUE,
            sample_name TEXT NOT NULL,
            source_unit TEXT NOT NULL,
            sender TEXT,
            receiver TEXT NOT NULL,
            receive_time TEXT NOT NULL,
            test_items TEXT NOT NULL,
            status TEXT NOT NULL DEFAULT '待检测',
            description TEXT,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        )
        """

    def _test_records_table(self) -> str:
        return """
        CREATE TABLE IF NOT EXISTS test_records (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            sample_id INTEGER NOT NULL,
            test_item TEXT NOT NULL,
            tester TEXT NOT NULL,
            test_time TEXT,
            test_result TEXT,
            result_value TEXT,
            standard_value TEXT,
            is_qualified INTEGER DEFAULT 1,
            remarks TEXT,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (sample_id) REFERENCES samples(id) ON DELETE CASCADE
        )
        """

    def _test_record_changes_table(self) -> str:
        return """
        CREATE TABLE IF NOT EXISTS test_record_changes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            test_record_id INTEGER NOT NULL,
            sample_id INTEGER NOT NULL,
            field_name TEXT NOT NULL,
            old_value TEXT,
            new_value TEXT,
            change_reason TEXT NOT NULL,
            operator TEXT NOT NULL,
            change_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            remarks TEXT,
            FOREIGN KEY (test_record_id) REFERENCES test_records(id) ON DELETE CASCADE,
            FOREIGN KEY (sample_id) REFERENCES samples(id) ON DELETE CASCADE
        )
        """

    def _attachments_table(self) -> str:
        return """
        CREATE TABLE IF NOT EXISTS attachments (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            sample_id INTEGER NOT NULL,
            file_name TEXT NOT NULL,
            file_path TEXT NOT NULL,
            file_size INTEGER,
            file_type TEXT,
            uploaded_by TEXT,
            uploaded_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            description TEXT,
            FOREIGN KEY (sample_id) REFERENCES samples(id) ON DELETE CASCADE
        )
        """

    def _audit_logs_table(self) -> str:
        return """
        CREATE TABLE IF NOT EXISTS audit_logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            sample_id INTEGER,
            operation_type TEXT NOT NULL,
            field_name TEXT,
            old_value TEXT,
            new_value TEXT,
            operator TEXT NOT NULL,
            operation_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            remarks TEXT,
            FOREIGN KEY (sample_id) REFERENCES samples(id) ON DELETE SET NULL
        )
        """

    def _system_settings_table(self) -> str:
        return """
        CREATE TABLE IF NOT EXISTS system_settings (
            key TEXT PRIMARY KEY,
            value TEXT,
            description TEXT,
            updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        )
        """

    def create_indexes(self):
        indexes = [
            "CREATE INDEX IF NOT EXISTS idx_samples_sample_no ON samples(sample_no)",
            "CREATE INDEX IF NOT EXISTS idx_samples_status ON samples(status)",
            "CREATE INDEX IF NOT EXISTS idx_samples_receive_time ON samples(receive_time)",
            "CREATE INDEX IF NOT EXISTS idx_test_records_sample_id ON test_records(sample_id)",
            "CREATE INDEX IF NOT EXISTS idx_test_record_changes_test_record_id ON test_record_changes(test_record_id)",
            "CREATE INDEX IF NOT EXISTS idx_test_record_changes_sample_id ON test_record_changes(sample_id)",
            "CREATE INDEX IF NOT EXISTS idx_test_record_changes_operator ON test_record_changes(operator)",
            "CREATE INDEX IF NOT EXISTS idx_attachments_sample_id ON attachments(sample_id)",
            "CREATE INDEX IF NOT EXISTS idx_audit_logs_sample_id ON audit_logs(sample_id)"
        ]

        for index_sql in indexes:
            self.db.execute(index_sql)

    def initialize(self):
        self.create_tables()
        self.create_indexes()
