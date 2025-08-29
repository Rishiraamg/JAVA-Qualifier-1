# HRX Webhook SQL (Spring Boot)

On startup, this app:
1. Calls `generateWebhook` with your name, regNo, email.
2. Loads your final SQL from `src/main/resources/solution.sql` (or from `app.solve.inline`).
3. Submits `{"finalQuery":"<your sql>"}` to the returned **webhook** using the received **accessToken** in the `Authorization` header (no `Bearer` prefix).
4. Stores a local copy at `target/finalQuery.txt`.

> No controllers are exposed; it runs as a CLI app (`spring.main.web-application-type=none`).

## Requirements satisfied
- **RestTemplate** via `RestTemplateBuilder` with timeouts.
- **Auto-run** flow on startup using `ApplicationRunner` (no controller triggers).
- **JWT in Authorization** header for the submit call (exactly the token from the API, no prefix).
- **Webhook URL** from the first response is used to submit. If not present, it falls back to the documented `testWebhook` URL.

## Configure
Edit `src/main/resources/application.yml`:
```yaml
app:
  name: "John Doe"
  regNo: "REG12347"
  email: "john@example.com"
  solve:
    source: file         # file | inline
    file: "classpath:solution.sql"
    inline: "SELECT 1"
  endpoints:
    generate: "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA"
    submitFallback: "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA"
```

Put your SQL into `src/main/resources/solution.sql` (or set `solve.source=inline` and write SQL in `solve.inline`).

## Build
Make sure you have **Java 17+** and **Maven** installed.
```bash
mvn -q -DskipTests package
```
The JAR will be at `target/hrx-webhook-sql-1.0.0.jar`.

## Run
```bash
java -jar target/hrx-webhook-sql-1.0.0.jar
```

Logs will show:
- response from `generateWebhook` (webhook + accessToken lengths)
- submission status & response
- saved file `target/finalQuery.txt`

## Notes
- By spec, the submission call uses `Authorization: <accessToken>` **without** a `Bearer ` prefix.
- If the first response doesn’t include a `webhook` field, the app falls back to `https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA`.
- If your SQL ends with `;`, it is kept as-is (the gateway should accept either).

## What to upload per the Submission Checklist
- **Public GitHub repo** containing:
  - this source code
  - the built **JAR** (`target/hrx-webhook-sql-1.0.0.jar`) — after you run the build locally
  - a **RAW downloadable** GitHub link to the JAR (e.g., GitHub raw file URL)
- **Public JAR file link (downloadable)**: you can use the GitHub raw link.

## Quick test (without hitting the real challenge)
You can dry-run by leaving the default `solution.sql` as `SELECT 1;` and your own identity. The app will attempt real network calls; use only if you intend to actually submit.
