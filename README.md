# AI Demo Backend

Spring Boot 4 backend for a small AI application hub. It provides general chat, PDF RAG chat, role-play game chat, customer-service consultation with course tools, and video understanding endpoints for the Vue frontend.

## Features

- General AI chat with streaming text responses
- Multimodal chat with uploaded media
- PDF upload, vector indexing, download, and PDF question answering
- Course consultation assistant with MyBatis-Plus tools
- Course reservation tool calling
- Role-play game chat
- Video chat endpoint compatible with DashScope/OpenAI-style chat completions
- In-memory chat history index with thread-safe collections

## Tech Stack

- Java 17
- Spring Boot 4.0.6
- Spring AI 2.0.0-M6
- MyBatis-Plus 3.5.15
- MySQL
- Redis Stack / Redis Vector Store
- Maven Wrapper

## Project Structure

```text
src/main/java/org/starry/aidemo
+-- config          # Spring AI, MVC, Redis vector store configuration
+-- controller      # REST endpoints
+-- entity          # PO, query, and response objects
+-- mapper          # MyBatis-Plus mapper interfaces
+-- Repository      # Chat history and PDF file repositories
+-- service         # Service interfaces and implementations
`-- Tools           # Spring AI tool-calling tools

src/main/resources
+-- application-example.yaml
`-- mapper
```

## Requirements

Before starting the project, make sure these services and variables are available:

- JDK 17 or newer
- MySQL database named `web_school`
- Redis Stack running on `localhost:6379`
- DashScope API key in the environment variable `DASHSCOPE_API_KEY`

Example environment variable:

```powershell
$env:DASHSCOPE_API_KEY="your-api-key"
```

## Configuration

The repository includes a safe example configuration:

```text
src/main/resources/application-example.yaml
```

Copy it to `src/main/resources/application.yaml` for local development and fill in your own database password and API keys. The real `application.yaml` is ignored by Git.

Important settings:

```yaml
spring:
  ai:
    openai:
      base-url: https://dashscope-intl.aliyuncs.com/compatible-mode/v1
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        model: deepseek-v4-flash
      embedding:
        model: text-embedding-v4
        dimensions: 1024
  data:
    redis:
      host: localhost
      port: 6379
  datasource:
    url: jdbc:mysql://localhost:3306/web_school

app:
  pdf:
    upload-dir: ./data/pdf
```

PDF files are saved under the configured `app.pdf.upload-dir` with generated safe filenames.

## Run Locally

Start Redis Stack and MySQL first, then run:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend starts at:

```text
http://localhost:8080
```

Health check:

```powershell
curl http://localhost:8080/actuator/health
```

## Test

```powershell
.\mvnw.cmd test
```

The test suite loads the Spring context and exercises vector-store related behavior, so Redis Stack and the AI embedding configuration should be available.

## Frontend

The matching Vue frontend is located at:

```text
spring-ai-portal
```

Run it with:

```powershell
npm run dev
```

Then open:

```text
http://localhost:5173
```

The frontend calls this backend at `http://localhost:8080`.

If port `5173` is already occupied or blocked on your machine, start Vite on another port:

```powershell
npm run dev -- --host localhost --port 3000
```

## API Overview

### General Chat

```http
POST /ai/chat
```

Parameters:

- `prompt`: user message
- `chatId`: conversation id
- `files`: optional multipart files

Returns a streaming plain-text response.

### Game Chat

```http
GET /ai/game?prompt={prompt}&chatId={chatId}
```

Returns a streaming plain-text role-play game response.

### Customer Service

```http
GET /ai/service?prompt={prompt}&chatId={chatId}
```

Returns a streaming plain-text customer-service response. This chat client can call course query and reservation tools.

### PDF Upload

```http
POST /ai/pdf/upload/{chatId}
```

Multipart form field:

- `file`: PDF file

Example:

```powershell
curl.exe -F "file=@path\to\document.pdf;type=application/pdf" `
  http://localhost:8080/ai/pdf/upload/pdf_demo
```

### PDF Chat

```http
GET /ai/pdf/chat?prompt={prompt}&chatId={chatId}
```

Requires a PDF to be uploaded for the same `chatId` first.

### PDF Download

```http
GET /ai/pdf/file/{chatId}
```

Downloads the PDF associated with the chat id.

### Chat History

```http
GET /ai/history/{type}
GET /ai/history/{type}/{chatId}
```

Supported `type` values include:

- `chat`
- `pdf`
- `service`

## Development Notes

- Redis Stack is required for vector search.
- MySQL is required for course and school data.
- Chat history indexing is currently in memory and thread-safe, but not persistent.
- Uploaded PDF file mappings are persisted in `chat-pdf.properties` on shutdown.
- The PDF vector filter uses a generated `file_key` instead of raw file names.

## Roadmap

- Persist chat history indexes to Redis so conversation lists survive application restarts.
- Add environment-specific configuration profiles for local development and deployment.
- Add focused controller/service tests for chat, PDF upload, and customer-service workflows.

## License

MIT License Copyright (c) 2026 Starry.
