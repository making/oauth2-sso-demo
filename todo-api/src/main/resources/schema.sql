CREATE TABLE IF NOT EXISTS todo (
    todo_id TEXT PRIMARY KEY,
    todo_title TEXT NOT NULL,
    finished INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    created_by TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    updated_by TEXT NOT NULL
);
