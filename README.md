MediLab Pro - Spring Boot Backend (Java 21)

## How to run (Development)
1. Ensure Java 21 and Maven are installed.
2. Create a Postgres database and set environment variables:
   - `SPRING_DATASOURCE_URL` (e.g. `jdbc:postgresql://localhost:5432/medilab_db`)
   - `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
3. `mvn spring-boot:run`

## Running with Docker (Recommended)
The project is configured with a unified entry point via Nginx and Docker Compose. This handles both the Frontend (UI) and Backend (API) automatically.

1. Ensure you have Docker and Docker Compose installed.
2. Build and start all services:
   ```bash
   docker-compose up -d --build
   ```
3. Access the application at `http://localhost`.

## External Access (Cloudflare Tunnel)
To securely access your application from the internet:

1. Download `cloudflared` on your machine.
2. Run the tunnel directed at your local entry point:
   ```powershell
   C:\cloudflare> .\cloudflared.exe tunnel --url localhost
   ```
3. Copy the generated `trycloudflare.com` URL.
4. Open the `.env` file in the root of this backend directory.
5. Set the `PUBLIC_URL` variable:
   ```env
   PUBLIC_URL=https://your-generated-name.trycloudflare.com
   ```
6. The app will now serve the UI and correct report links through this public URL.
