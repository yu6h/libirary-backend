# 本機啟動說明（Backend）

以下指令請在終端機執行。路徑以專案根目錄為 `demo2` 為例。

## 前置需求

- **Docker Desktop**（或已安裝並可執行 `docker compose`）
- **JDK 21**（執行 `./mvnw` 需要）
- 本機 **5432** 埠未被其他程式佔用（PostgreSQL 對外埠）

## 1. 啟動資料庫（PostgreSQL）

`compose.yaml` 位於本目錄，請先進入 `backend` 再啟動：

```bash
cd backend
docker compose up -d
```

確認容器狀態：

```bash
docker compose ps
```

停止資料庫（並移除容器；若要連同資料一併清空可加 `-v`）：

```bash
cd backend
docker compose down
```

若要**清空 volume 重新初始化**（會刪除 DB 內資料，下次啟動會依 `schema.sql`、`data.sql` 重建）：

```bash
cd backend
docker compose down -v
docker compose up -d
```

預設連線資訊（與 `application.properties` 預設值一致）：

- 主機：`localhost`
- 埠：`5432`
- 資料庫：`library`
- 使用者／密碼：`postgres` / `postgres`

## 2. 啟動後端（Spring Boot）

資料庫已啟動後，在同一目錄執行：

```bash
cd backend
./mvnw spring-boot:run
```

預設 API 位址：`http://localhost:8080`

### 選用環境變數

可依需求覆寫 `application.properties` 的預設值：

| 變數 | 說明 |
|------|------|
| `JWT_SECRET` | JWT 簽章金鑰（正式環境務必設定） |
| `DB_URL` | JDBC URL，預設 `jdbc:postgresql://localhost:5432/library` |
| `DB_USERNAME` | 資料庫使用者，預設 `postgres` |
| `DB_PASSWORD` | 資料庫密碼，預設 `postgres` |

範例（僅當次終端有效）：

```bash
export JWT_SECRET='你的長隨機字串至少32字元以上'
cd backend
./mvnw spring-boot:run
```

停止後端：在執行 `spring-boot:run` 的終端機按 **Ctrl+C**。

## 3. 啟動前端（簡述）

前端專案在上一層的 `frontend` 目錄，完整步驟請見 `frontend/START.md`。

```bash
cd ../frontend
npm install
npm run dev
```

瀏覽器開啟：`http://localhost:5173/`
