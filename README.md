# RAGFlow 智能助手管理系统

基于 RAGFlow API 的智能对话系统，支持文字聊天和语音聊天。

## 技术栈

- **前端**: Vue 3 + Element Plus + Vite + Pinia
- **后端**: Spring Boot 3.2 + MyBatis-Plus + JWT + MySQL
- **部署**: Docker Compose

## 快速启动

```bash
docker-compose up -d --build
```

访问 http://localhost

默认管理员账号: admin / admin123

## 功能

- 智能对话 (文字 + 语音)
- 知识库管理
- 智能体管理
- 会话管理

## RAGFlow 配置

在 `docker-compose.yml` 中配置:
- `RAGFLOW_BASE_URL`: RAGFlow 服务地址
- `RAGFLOW_API_KEY`: RAGFlow API 密钥
