-- 文件AI助手数据库表结构

-- 文件夹配置表
CREATE TABLE IF NOT EXISTS watch_folders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    path TEXT UNIQUE NOT NULL,
    recursive BOOLEAN DEFAULT 1,
    enabled BOOLEAN DEFAULT 1,
    created_time TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_time TEXT DEFAULT CURRENT_TIMESTAMP
);

-- 文件索引表
CREATE TABLE IF NOT EXISTS file_index (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_path TEXT UNIQUE NOT NULL,
    file_name TEXT NOT NULL,
    file_size INTEGER,
    file_type TEXT,
    last_modified TEXT,
    indexed_time TEXT DEFAULT CURRENT_TIMESTAMP,
    folder_id INTEGER REFERENCES watch_folders(id),
    content TEXT,
    summary TEXT,
    vector_data BLOB
);

-- 搜索历史表
CREATE TABLE IF NOT EXISTS search_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    query_text TEXT NOT NULL,
    result_count INTEGER,
    search_time TEXT DEFAULT CURRENT_TIMESTAMP,
    search_type TEXT DEFAULT 'text'
);

-- 用户配置表
CREATE TABLE IF NOT EXISTS user_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    config_key TEXT UNIQUE NOT NULL,
    config_value TEXT,
    config_type TEXT DEFAULT 'string',
    created_time TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_time TEXT DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_file_index_path ON file_index(file_path);
CREATE INDEX IF NOT EXISTS idx_file_index_type ON file_index(file_type);
CREATE INDEX IF NOT EXISTS idx_file_index_folder ON file_index(folder_id);
CREATE INDEX IF NOT EXISTS idx_search_history_time ON search_history(search_time);
CREATE INDEX IF NOT EXISTS idx_user_config_key ON user_config(config_key);

-- 插入默认配置
INSERT OR IGNORE INTO user_config (config_key, config_value, config_type) VALUES 
('max_file_size', '52428800', 'integer'),
('batch_size', '100', 'integer'),
('embedding_model', 'all-MiniLM-L6-v2.onnx', 'string'),
('embedding_dimension', '384', 'integer'),
('default_page_size', '20', 'integer'),
('max_search_results', '1000', 'integer');
