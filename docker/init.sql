-- RAGFlow Admin System Database Init
-- Note: Database 'ragflow_admin' is auto-created by MYSQL_DATABASE env var
-- This script runs against that database automatically
SET NAMES utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) DEFAULT '',
    ragflow_api_key VARCHAR(255) DEFAULT '',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    config_key VARCHAR(100) NOT NULL PRIMARY KEY,
    config_value VARCHAR(1000) NOT NULL DEFAULT '',
    remark VARCHAR(255) NOT NULL DEFAULT '',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 默认 RAGFlow 配置
INSERT INTO sys_config (config_key, config_value, remark) VALUES
('ragflow.base-url', 'http://159.138.146.41', 'RAGFlow 服务地址'),
('ragflow.api-key', 'ragflow-q6dOyf9Pn-K1cWii4aGLsy832oyjoecED1YpsQGtbKA', 'RAGFlow API 密钥')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 默认管理员 admin/admin123
INSERT INTO sys_user (username, password, nickname) VALUES
('admin', '$2a$10$vXxwVFgKVTpo806W66iEtubNaxE7sOOsDt3yZnVxYOmi.D4BkRlsC', '管理员')
ON DUPLICATE KEY UPDATE password = VALUES(password);
