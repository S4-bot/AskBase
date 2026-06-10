# AskBase 项目框架规范

本文件用于约束 AskBase 项目的后端工程结构、分层规范、命名规范和开发边界。后续开发必须优先遵守本节规范，再参考下方项目材料。

## 1. 项目定位

AskBase 是一个用于 Java 后端实习面试展示的企业智能知识库问答平台。

第一版坚持以下边界：

- 架构：Spring Boot 单体项目，不拆 Spring Cloud 微服务。
- 后端主线：登录注册、知识库管理、文档上传、文档解析、文档切片、向量化、pgvector 检索、Qwen RAG 问答、会话历史、引用来源。
- 前端演示：Vue 3 + Vite + Element Plus，后端主流程稳定后再联调。
- 优先级：先跑通最小闭环，再补 RabbitMQ、Redis、接口文档、部署说明。

## 2. 当前工程基础规范

### 2.1 Maven 坐标

当前项目以 `pom.xml` 为准：

- `groupId`: `com.it.mcp`
- `artifactId`: `AskBase`
- `name`: `AskBase`
- Java 版本：`17`
- Spring Boot 版本：`3.5.14`

除非明确重构项目坐标，否则不得随意修改 Maven 坐标和主包路径。

### 2.2 Java 根包

当前后端根包固定为：

```text
com.ssq.askbase
```

Spring Boot 启动类必须放在根包下：

```text
src/main/java/com/ssq/askbase/AskBaseApplication.java
```

所有后端业务代码都必须放在 `com.ssq.askbase` 及其子包下，确保 Spring Boot 能自动扫描到 Controller、Service、Mapper、Config 等组件。

### 2.3 配置文件

后端配置文件统一使用：

```text
src/main/resources/application.yaml
```

不要同时混用 `application.yml` 和 `application.yaml`。如果后续需要环境隔离，可以新增：

```text
application-dev.yaml
application-prod.yaml
```

并通过 `spring.profiles.active` 控制。

## 3. 总体目录结构

后端主目录必须保持以下结构：

```text
src/main/java/com/ssq/askbase
├── AskBaseApplication.java
├── auth
├── knowledge
├── document
├── rag
├── chat
├── common
└── config
```

各一级包职责如下：

| 包名 | 职责 | 说明 |
| --- | --- | --- |
| `auth` | 用户注册、登录、JWT、当前用户信息 | 只处理身份认证和登录态，不写知识库业务 |
| `knowledge` | 知识库创建、查询、修改、删除 | 所有查询必须校验用户资源归属 |
| `document` | 文档上传、本地存储、解析状态、失败原因 | 上传接口只负责接收文件和创建任务，不长期阻塞 |
| `rag` | Embedding、向量检索、Prompt 拼接、Qwen 调用 | RAG 核心逻辑集中在这里 |
| `chat` | 会话、消息记录、回答引用来源 | 保存用户问题、AI 回答和引用片段 |
| `common` | 通用响应、异常、枚举、常量、工具类 | 不允许放具体业务流程 |
| `config` | Spring Boot 配置类 | Security、MyBatis Plus、Redis、RabbitMQ、Web 配置 |

## 4. 模块内部标准结构

除 `common` 和 `config` 外，每个业务模块原则上使用相同分层：

```text
module
├── controller
├── service
├── service/impl
├── mapper
├── entity
├── dto
└── vo
```

各子包职责如下：

| 子包 | 职责 | 规则 |
| --- | --- | --- |
| `controller` | 接收 HTTP 请求 | 只做参数校验、当前用户获取、调用 Service，不写复杂业务 |
| `service` | 业务接口 | 定义模块对外业务能力 |
| `service.impl` | 业务实现 | 编排业务流程、校验资源归属、处理事务 |
| `mapper` | 数据库访问 | MyBatis Plus Mapper，只写数据访问 |
| `entity` | 数据库实体 | 与数据库表字段对应，不直接返回给前端 |
| `dto` | 请求参数对象 | 接收前端请求，使用 Validation 注解校验 |
| `vo` | 响应对象 | 返回给前端的数据结构，不暴露敏感字段 |

调用方向固定为：

```text
Controller -> Service -> Mapper -> Database
```

禁止以下写法：

- Controller 直接调用 Mapper。
- Controller 中编写复杂业务逻辑。
- Mapper 中拼接业务流程。
- 直接把 Entity 返回给前端。
- 跨模块直接访问其他模块 Mapper。

跨模块调用优先调用目标模块 Service。

## 5. 各业务模块详细规范

### 5.1 auth 模块

目录：

```text
auth
├── controller
├── service
├── service/impl
├── mapper
├── entity
├── dto
├── vo
└── security
```

职责：

- 用户注册。
- 用户登录。
- 密码加密与校验。
- JWT 生成与解析。
- 当前登录用户获取。
- 登录过滤器和认证上下文维护。

推荐类名：

```text
AuthController
AuthService
AuthServiceImpl
UserMapper
User
RegisterRequest
LoginRequest
LoginVO
UserInfoVO
LoginUser
JwtTokenProvider
JwtAuthenticationFilter
```

规范：

- 密码必须使用 `BCryptPasswordEncoder` 加密保存。
- `passwordHash` 不允许返回给前端。
- JWT 密钥、过期时间必须放到配置文件中，不要硬编码。
- `/api/auth/register`、`/api/auth/login` 放行，其余业务接口默认需要登录。

### 5.2 knowledge 模块

目录：

```text
knowledge
├── controller
├── service
├── service/impl
├── mapper
├── entity
├── dto
└── vo
```

职责：

- 创建知识库。
- 查询当前用户知识库列表。
- 修改知识库。
- 删除知识库。
- 校验知识库是否属于当前用户。

推荐类名：

```text
KnowledgeBaseController
KnowledgeBaseService
KnowledgeBaseServiceImpl
KnowledgeBaseMapper
KnowledgeBase
KnowledgeBaseCreateRequest
KnowledgeBaseUpdateRequest
KnowledgeBaseVO
```

规范：

- 所有知识库查询必须带 `userId` 条件。
- 任何通过 `kbId` 操作知识库的接口都必须校验资源归属。
- 删除知识库前必须考虑关联文档、切片、会话的处理方式，第一版可以先做逻辑删除。

### 5.3 document 模块

目录：

```text
document
├── controller
├── service
├── service/impl
├── mapper
├── entity
├── dto
├── vo
├── parser
└── storage
```

职责：

- 文档上传。
- 文档本地存储。
- 文档记录入库。
- 文档解析状态流转。
- 解析失败原因记录。
- 后续接入 Apache Tika。

推荐类名：

```text
DocumentController
DocumentService
DocumentServiceImpl
DocumentMapper
Document
DocumentVO
DocumentStatus
DocumentParser
TikaDocumentParser
LocalFileStorageService
```

文档状态统一使用枚举：

```text
UPLOADED
PARSING
EMBEDDING
COMPLETED
FAILED
```

规范：

- 上传接口只负责文件保存、document 记录创建、任务触发。
- 第一版可以同步解析，接入 RabbitMQ 后必须改为异步处理。
- 解析失败必须记录 `failReason`。
- 不允许静默吞掉解析异常。
- 文件保存路径必须通过配置项控制，不要硬编码到业务代码。

### 5.4 rag 模块

目录：

```text
rag
├── controller
├── service
├── service/impl
├── mapper
├── entity
├── dto
├── vo
├── embedding
├── qwen
└── prompt
```

职责：

- 文档切片。
- Embedding 调用。
- 向量写入。
- pgvector 相似度检索。
- Prompt 拼接。
- Qwen 聊天模型调用。
- RAG 问答编排。

推荐类名：

```text
RagController
RagService
RagServiceImpl
DocumentChunkMapper
DocumentChunk
AskRequest
AskVO
SourceVO
EmbeddingClient
QwenClient
PromptBuilder
TextChunker
```

切片参数默认值：

```text
chunkSize: 800-1000 中文字符
overlap: 100-150 中文字符
topK: 5
similarityThreshold: 第一版不强制，后续再按测试样本调整
```

规范：

- Qwen API Key 必须来自环境变量或配置文件，不允许写死在代码中。
- Prompt 模板集中放在 `prompt` 包或配置文件中。
- 如果检索不到相关内容，必须返回“知识库中没有找到相关信息”。
- AI 调用失败时返回友好错误，不保存错误答案为正常回答。
- 向量检索必须限定 `kbId`，不能跨知识库召回。

### 5.5 chat 模块

目录：

```text
chat
├── controller
├── service
├── service/impl
├── mapper
├── entity
├── dto
└── vo
```

职责：

- 创建聊天会话。
- 查询会话列表。
- 保存用户消息。
- 保存 AI 回答。
- 保存回答引用来源。
- 查询历史消息和引用。

推荐类名：

```text
ChatSessionController
ChatMessageController
ChatService
ChatServiceImpl
ChatSessionMapper
ChatMessageMapper
ChatSourceMapper
ChatSession
ChatMessage
ChatSource
CreateSessionRequest
ChatMessageVO
ChatSourceVO
```

规范：

- 会话必须绑定 `userId` 和 `kbId`。
- 查询会话和消息时必须校验当前用户归属。
- 用户提问和 AI 回答都保存到 `chat_message`。
- 引用来源保存到 `chat_source`，并关联 AI 回答消息。

### 5.6 common 模块

目录：

```text
common
├── controller
├── response
├── exception
├── enums
├── constant
└── util
```

职责：

- 健康检查接口。
- 统一响应格式。
- 全局异常处理。
- 通用枚举。
- 常量。
- 通用工具类。

推荐类名：

```text
HealthController
ApiResponse
BusinessException
GlobalExceptionHandler
ErrorCode
UserContextHolder
```

统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

规范：

- Controller 默认返回 `ApiResponse<T>`。
- 业务异常统一抛 `BusinessException`。
- 全局异常由 `GlobalExceptionHandler` 处理。
- `common` 不能依赖具体业务模块，避免反向耦合。

### 5.7 config 模块

目录：

```text
config
├── SecurityConfig.java
├── MybatisPlusConfig.java
├── RedisConfig.java
├── RabbitMqConfig.java
└── WebConfig.java
```

职责：

- Spring Security 配置。
- MyBatis Plus 配置。
- Redis 配置。
- RabbitMQ 配置。
- Web MVC 配置。

规范：

- 配置类只写框架配置，不写业务逻辑。
- Bean 名称清晰，避免多个同类型 Bean 冲突。
- 安全放行路径集中维护在 `SecurityConfig`。

## 6. 命名规范

### 6.1 包命名

- 包名全部小写。
- 不使用下划线。
- 不使用复数包名。
- 业务模块用领域名：`auth`、`knowledge`、`document`、`rag`、`chat`。

### 6.2 类命名

| 类型 | 后缀 | 示例 |
| --- | --- | --- |
| Controller | `Controller` | `AuthController` |
| Service 接口 | `Service` | `AuthService` |
| Service 实现 | `ServiceImpl` | `AuthServiceImpl` |
| Mapper | `Mapper` | `UserMapper` |
| Entity | 无固定后缀 | `User` |
| DTO | `Request` | `LoginRequest` |
| VO | `VO` | `LoginVO` |
| 配置类 | `Config` | `SecurityConfig` |
| 枚举 | 语义名 | `DocumentStatus` |

### 6.3 方法命名

- 查询单个：`getById`、`getCurrentUser`
- 查询列表：`listByUserId`、`listDocuments`
- 创建：`create`
- 修改：`update`
- 删除：`delete`
- 校验归属：`checkOwner`、`validateOwner`
- RAG 问答：`ask`

### 6.4 数据库命名

- 表名使用小写下划线。
- 字段名使用小写下划线。
- Java 字段使用小驼峰。

示例：

```text
数据库字段：password_hash
Java 字段：passwordHash
```

## 7. 接口规范

接口统一以 `/api` 开头。

第一版接口固定如下：

| 接口 | 用途 | 鉴权 |
| --- | --- | --- |
| `POST /api/auth/register` | 用户注册 | 否 |
| `POST /api/auth/login` | 用户登录并返回 JWT | 否 |
| `GET /api/auth/me` | 获取当前登录用户 | 是 |
| `POST /api/kbs` | 创建知识库 | 是 |
| `GET /api/kbs` | 查询我的知识库列表 | 是 |
| `POST /api/kbs/{kbId}/documents` | 上传文档 | 是 |
| `GET /api/kbs/{kbId}/documents` | 查询知识库文档 | 是 |
| `GET /api/documents/{documentId}/status` | 查询文档解析状态 | 是 |
| `POST /api/chat/sessions` | 创建聊天会话 | 是 |
| `GET /api/chat/sessions` | 查询会话列表 | 是 |
| `POST /api/chat/ask` | 提问并返回 AI 回答 | 是 |
| `GET /api/chat/sessions/{sessionId}/messages` | 查询会话消息和引用 | 是 |
| `GET /api/health` | 健康检查 | 否 |

接口规则：

- 请求体使用 DTO。
- 响应体使用 VO。
- 参数校验使用 `jakarta.validation`。
- 登录用户从认证上下文获取，不允许由前端传 `userId` 决定资源归属。
- 分页接口后续统一使用 `pageNum`、`pageSize`。

## 8. 数据模型规范

核心表固定为：

```text
user
knowledge_base
document
document_chunk
chat_session
chat_message
chat_source
```

建议字段：

```text
user:
  id, username, password_hash, status, created_at, updated_at

knowledge_base:
  id, user_id, name, description, created_at, updated_at, deleted

document:
  id, kb_id, user_id, filename, path, type, size, status, fail_reason, created_at, updated_at

document_chunk:
  id, document_id, kb_id, content, chunk_index, embedding, source, created_at

chat_session:
  id, user_id, kb_id, title, created_at, updated_at

chat_message:
  id, session_id, role, content, created_at

chat_source:
  id, message_id, document_id, chunk_id, score, snippet, created_at
```

规范：

- 主键优先使用 `Long`。
- 时间字段使用 `LocalDateTime`。
- 状态字段使用枚举或固定字符串，禁止魔法值散落在代码中。
- 用户隔离字段必须明确：知识库、文档、会话都必须能追溯到 `userId`。

## 9. 依赖使用规范

第一阶段允许的核心依赖：

- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `postgresql`
- `lombok`
- `mybatis-plus-spring-boot3-starter`

后续按阶段引入：

- Redis：`spring-boot-starter-data-redis`
- RabbitMQ：`spring-boot-starter-amqp`
- 文档解析：Apache Tika
- JWT：优先使用成熟 JWT 库，不手写加解密细节
- AI 调用：优先封装为独立 Client，不在 Service 中散写 HTTP 请求

规范：

- 不引入与当前阶段无关的大量依赖。
- 新增依赖必须能说明用途。
- 依赖版本优先交给 Spring Boot BOM 管理，确实需要时再显式指定。

## 10. Docker 与本地环境规范

本地开发使用 `docker-compose.yml` 管理基础服务：

```text
PostgreSQL + pgvector
Redis
RabbitMQ
```

建议宿主机端口：

```text
PostgreSQL: 15432 -> 5432
Redis: 16379 -> 6379
RabbitMQ: 5673 -> 5672
RabbitMQ Management: 15673 -> 15672
```

规范：

- Compose 容器名统一以 `askbase-` 开头。
- 端口尽量避开本机已有 Docker Desktop 手动创建的容器。
- 数据库账号、密码、库名第一版可以固定为本地开发配置，生产配置以后再拆分。
- 不把真实 API Key 写进 `docker-compose.yml` 或 Git 仓库。

## 11. RAG 流程规范

上传流程：

```text
用户上传文档
-> 保存文件
-> 创建 document 记录，状态 UPLOADED
-> 触发解析任务
-> 状态 PARSING
-> 提取文本
-> 文本切片
-> 状态 EMBEDDING
-> 调用 Embedding
-> 写入 document_chunk
-> 状态 COMPLETED
```

失败流程：

```text
任意步骤失败
-> document.status = FAILED
-> document.fail_reason 记录可读错误
```

问答流程：

```text
用户提问
-> 问题向量化
-> pgvector 按 kbId 检索 Top 5
-> 拼接 Prompt
-> 调用 Qwen
-> 保存用户问题
-> 保存 AI 回答
-> 保存引用来源
-> 返回答案和来源
```

Prompt 基本要求：

```text
你是企业知识库助手。请只根据下面提供的资料回答问题。
如果资料中没有答案，请回答“知识库中没有找到相关信息”。
回答后列出你使用到的资料来源。
```

## 12. 安全与权限规范

- 除注册、登录、健康检查外，接口默认需要 JWT。
- 当前用户 ID 只能从 JWT 认证上下文中获取。
- 前端传来的 `userId` 不能作为可信身份。
- 所有资源查询必须带用户归属校验。
- 不允许越权访问其他用户的知识库、文档、会话、消息。
- 密码、Token、API Key 不允许写入日志。

## 13. 异常与日志规范

- 业务异常使用 `BusinessException`。
- 参数校验失败由全局异常处理器统一返回。
- 未预期异常返回通用错误提示，详细异常写日志。
- 日志中记录关键流程：注册、登录、上传、解析状态变化、AI 调用失败。
- 不记录敏感信息：密码、JWT、API Key、完整大段文档内容。

## 14. 测试与验收规范

每个阶段完成后必须至少手动验证主流程。

第一阶段验收：

```text
Spring Boot 能启动
GET /api/health 返回成功
Docker Compose 可启动 PostgreSQL/Redis/RabbitMQ
后端能连接 PostgreSQL
```

登录注册阶段验收：

```text
注册成功
重复用户名失败
登录成功并返回 JWT
密码错误登录失败
未登录访问受保护接口失败
```

知识库阶段验收：

```text
用户可创建知识库
用户只能查询自己的知识库
用户不能操作别人的知识库
```

RAG 阶段验收：

```text
上传文档后状态能流转
切片能入库
相关问题能召回片段
回答包含引用来源
无关问题返回知识库中没有找到相关信息
```

## 15. 开发顺序规范

严格按以下顺序推进：

```text
1. 项目基础结构、Docker Compose、数据库连接、健康检查
2. 统一响应、全局异常、Security 基础配置
3. 注册登录、JWT、当前用户接口
4. 知识库 CRUD 和用户隔离
5. 文档上传、本地存储、document 状态
6. 文档解析、切片入库
7. Embedding 接入、pgvector 检索
8. RAG 问答、Prompt、Qwen 调用
9. 会话历史、引用来源
10. RabbitMQ 异步解析
11. Redis 缓存和限流
12. Vue 演示页联调
13. README、部署说明、简历话术
```

在前一步未跑通前，不提前实现后面复杂能力。

## 16. 代码风格规范

- Java 代码使用 4 个空格缩进。
- 类职责单一，不写超大类。
- 方法尽量短，复杂流程拆私有方法。
- 注释只解释复杂业务原因，不写无意义注释。
- 使用 Lombok 可以减少样板代码，但不要滥用。
- DTO 使用 Validation 注解，例如 `@NotBlank`、`@NotNull`、`@Size`。
- Service 方法涉及多表写入时使用 `@Transactional`。
- 枚举集中定义，不在代码中散落字符串状态。

## 17. 当前第一步执行标准

当前阶段只做基础工程，不实现完整业务。

必须完成：

```text
1. 确认启动类位于 com.ssq.askbase 根包
2. 建好 auth、knowledge、document、rag、chat、common、config 包
3. 创建 common.controller.HealthController
4. 创建 common.response.ApiResponse
5. 创建 config.SecurityConfig，临时放行 /api/health
6. 配置 docker-compose.yml
7. 配置 application.yaml 数据库连接
8. 启动项目并访问 /api/health
```

健康检查接口返回：

```json
{
  "code": 200,
  "message": "success",
  "data": "ok"
}
```

---

# 原始项目材料

Java 实习项目知识库材料
企业智能知识库问答平台
单体项目实施手册 / RAG 面试版

我用的开发工具是idea,版本是2024。
项目定位	Spring Boot 单体 + Vue 演示页，用于 Java 后端实习面试。
核心亮点	文档解析、切片、向量化、pgvector 检索、Qwen RAG 问答、引用来源。
推荐周期	15 天完成面试版，先跑通闭环，再补异步、缓存和演示页。
生成日期	2026-06-10

一句话介绍用户上传企业文档后，系统自动解析、切片、向量化存储；用户提问时先召回相关文档片段，再结合大模型生成回答，并返回引用来源。

1. 项目目标与边界
这个项目面向 Java 后端实习面试，重点展示业务建模、权限控制、文件处理、异步任务、向量检索和大模型接入能力。第一版坚持单体架构，不拆 Spring Cloud 微服务。
必须完成：登录注册、知识库管理、文档上传、文档解析、切片入库、RAG 问答、会话历史、引用来源。
建议完成：RabbitMQ 异步解析、Redis 限流与缓存、Vue 演示页、Docker Compose 部署说明。
暂不完成：多人协作知识库、流式输出、对象存储、复杂权限体系、微服务拆分。
2. 技术栈
层次	选型	说明
后端框架	Spring Boot	单体项目主框架，保留清晰模块边界。
鉴权	Spring Security + JWT	登录后签发 Token，接口通过过滤器校验身份。
数据库	PostgreSQL + pgvector	业务数据和向量数据统一存储，便于部署和讲解。
ORM	MyBatis Plus	减少 CRUD 样板代码，适合实习项目开发效率。
缓存/限流	Redis	保存登录状态辅助信息、热点问答缓存、用户提问频率限制。
异步任务	RabbitMQ	文档解析、切片、向量化从上传接口中拆出去。
文档解析	Apache Tika	统一解析 PDF、Word、TXT、Markdown 等文件。
AI 模型	通义千问 Qwen	聊天模型用于生成回答，Embedding 模型用于向量化。
前端	Vue 3 + Vite + Element Plus	完成登录、知识库、上传、聊天、历史记录演示。
3. 单体模块划分
模块	职责	面试可讲点
auth	注册、登录、JWT 生成与校验、当前用户信息。	为什么要做统一鉴权；JWT 如何过期；密码如何加密。
knowledge	知识库创建、列表、修改、删除、用户隔离。	如何保证用户只能访问自己的知识库。
document	文件上传、本地存储、解析状态、失败原因。	为什么文档解析不应该阻塞上传接口。
rag	Embedding、向量检索、Prompt 拼接、Qwen 调用。	RAG 为什么能减少大模型幻觉。
chat	会话、消息记录、回答引用来源。	如何保存问答历史，如何追溯答案依据。
common	统一响应、异常处理、限流工具、基础配置。	项目工程化能力。
4. 核心数据模型
表名	核心字段	用途
user	id, username, password\_hash, status, created\_at	保存用户账号与状态。
knowledge\_base	id, user\_id, name, description, created\_at	保存知识库基本信息，并绑定所属用户。
document	id, kb\_id, user\_id, filename, path, type, size, status, fail\_reason	保存上传文件与解析状态。
document\_chunk	id, document\_id, kb\_id, content, chunk\_index, embedding, source	保存切片文本和向量，是 RAG 检索核心。
chat\_session	id, user\_id, kb\_id, title, created\_at	保存一次问答会话。
chat\_message	id, session\_id, role, content, created\_at	保存用户问题和 AI 回答。
chat\_source	id, message\_id, document\_id, chunk\_id, score, snippet	保存回答引用来源。
5. 主要接口设计
接口	用途	鉴权
POST /api/auth/register	用户注册	否
POST /api/auth/login	用户登录并返回 JWT	否
GET /api/auth/me	获取当前登录用户	是
POST /api/kbs	创建知识库	是
GET /api/kbs	查询我的知识库列表	是
POST /api/kbs/{kbId}/documents	上传文档	是
GET /api/kbs/{kbId}/documents	查询知识库文档	是
GET /api/documents/{documentId}/status	查询文档解析状态	是
POST /api/chat/sessions	创建聊天会话	是
GET /api/chat/sessions	查询会话列表	是
POST /api/chat/ask	提问并返回 AI 回答	是
GET /api/chat/sessions/{sessionId}/messages	查询会话消息和引用	是
6. RAG 核心流程
RAG 是本项目最重要的面试点。它的目的不是让大模型凭空回答，而是先从知识库中找证据，再让模型基于证据生成答案。
上传流程:用户上传文档 -> 保存文件和 document 记录 -> 状态 UPLOADED -> 发送 RabbitMQ 消息 -> 解析文本 -> 切片 -> 调用 Embedding -> 写入 document\_chunk -> 状态 COMPLETED问答流程:用户提问 -> 问题向量化 -> pgvector 检索 Top 5 切片 -> 拼接 Prompt -> 调用 Qwen 聊天模型 -> 保存回答 -> 保存引用来源 -> 返回答案

推荐 Prompt 模板你是企业知识库助手。请只根据下面提供的资料回答问题。如果资料中没有答案，请回答“知识库中没有找到相关信息”。回答后列出你使用到的资料来源。

7. 文档切片策略
切片直接影响召回质量。切片太短会丢上下文，切片太长会带入无关信息并增加模型输入成本。第一版可以采用固定长度切片。
参数	推荐值	理由
chunk size	800-1000 中文字符	足够表达一个完整段落或小节。
overlap	100-150 中文字符	避免答案刚好跨越两个切片时丢失上下文。
topK	5	兼顾召回率和 Prompt 长度。
相似度阈值	先不强制，后续可设 0.25-0.35	不同 embedding 模型分数范围不同，先通过测试样本校准。
8. 开发路线：15 天面试版
阶段	目标	交付物
第 1 天	项目初始化、Docker Compose、基础配置。	后端工程、数据库连接、PostgreSQL/Redis/RabbitMQ 可启动。
第 2 天	Spring Security JWT 登录注册。	注册、登录、当前用户接口可用。
第 3 天	知识库 CRUD。	用户可创建并查看自己的知识库。
第 4 天	文档上传和本地存储。	文件落盘，document 记录入库。
第 5 天	文档解析和状态流转。	支持 PDF、Word、TXT、Markdown。
第 6 天	文档切片入库。	document\_chunk 保存切片文本。
第 7-8 天	Embedding 接入和 pgvector 检索。	问题可召回相似片段。
第 9 天	RAG 问答主流程。	提问接口返回答案和引用。
第 10 天	会话和问答历史。	可查看历史问题、回答和来源。
第 11 天	RabbitMQ 异步解析。	上传接口快速返回，后台处理文档。
第 12 天	Redis 缓存和限流。	限制每个用户每分钟提问次数。
第 13 天	Vue 页面联调。	登录、知识库、上传、聊天可演示。
第 14 天	异常处理、接口文档、日志。	错误信息友好，便于排查。
第 15 天	部署说明、简历话术、面试准备。	README 和演示流程完成。
9. 测试与验收
注册、登录、JWT 过期、未登录访问接口都要测试。
用户只能访问自己的知识库、文档、会话和消息。
上传文档后，状态应从 UPLOADED 变为 PARSING、EMBEDDING、COMPLETED 或 FAILED。
解析失败必须记录 fail\_reason，不能让接口静默失败。
相关问题应返回答案和引用来源；无关问题应返回知识库中没有找到相关信息。
AI 接口超时或失败时返回友好错误，不保存错误答案。
Redis 限流能限制用户高频提问。
Vue 页面能完成完整演示：登录 -> 创建知识库 -> 上传文档 -> 提问 -> 查看引用。
10. 面试讲解重点
问题	推荐回答方向
为什么不用普通关键词搜索？	关键词搜索依赖字面匹配，向量检索能找到语义相近内容，例如“报销规则”和“费用申请流程”。
为什么要切片？	大模型输入长度有限，整篇文档检索也不精确；切片后可以召回更具体的上下文。
如何减少大模型胡说？	先检索知识库片段，再在 Prompt 中要求只基于资料回答，并返回引用来源。
为什么文档解析要异步？	解析和 Embedding 可能耗时、失败或受外部 API 限制，异步可以避免上传接口长时间阻塞。
AI 接口失败怎么办？	设置超时、重试、错误状态和友好提示；不要把失败结果当正常回答保存。
如何保证数据隔离？	所有知识库、文档、会话查询都带 user\_id 条件，接口层校验资源归属。
11. 简历描述模板
项目描述基于 RAG 架构实现的企业智能知识库问答平台，支持用户上传文档、自动解析切片、向量化存储、相似内容召回和大模型问答，可用于企业内部制度、产品文档、技术资料等场景的智能检索与问答。

个人职责可以写：
负责知识库、文档、会话等核心模块设计，实现文档上传、解析、切片和状态流转。
接入 Qwen 聊天模型和 Embedding 模型，实现基于 pgvector 的 RAG 问答流程。
使用 RabbitMQ 异步处理文档解析和向量化任务，提升上传接口响应速度。
使用 Redis 实现热点问答缓存、用户权限缓存和接口访问限流。
基于 Spring Security JWT 实现登录鉴权和用户资源隔离。
12. 可作为知识库测试的问题
你完成项目后，可以把本文档上传到自己的系统中，然后用下面的问题测试检索和回答效果。
测试问题	期望命中内容
这个项目为什么适合 Java 实习面试？	项目目标与边界、技术栈、面试讲解重点。
RAG 问答的完整流程是什么？	RAG 核心流程。
文档切片应该怎么设置？	文档切片策略。
为什么文档解析要用 RabbitMQ 异步处理？	RAG 上传流程、面试讲解重点。
这个系统有哪些核心数据库表？	核心数据模型。
15 天开发计划怎么安排？	开发路线。
Redis 在这个项目里能做什么？	技术栈、开发路线、测试与验收。
简历上怎么描述这个项目？	简历描述模板。

13. 最小可运行版本建议
如果时间紧，先不要追求所有中间件都完成。最低可运行版本只需要 Spring Boot、PostgreSQL、Qwen API 和简单前端。先让上传文档到 AI 回答这条主流程跑通，再逐步补 RabbitMQ、Redis 和部署。
最低闭环:注册登录 -> 创建知识库 -> 上传 TXT/Markdown -> 解析文本 -> 切片 -> 调 Embedding -> pgvector 检索 -> Qwen 回答 -> 保存历史

