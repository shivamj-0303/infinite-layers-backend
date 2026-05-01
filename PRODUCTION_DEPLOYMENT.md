# Production Deployment Guide: Render + Supabase + Docker

This guide covers deploying your Infinite Prints backend to Render using Docker, with PostgreSQL from Supabase.

## Architecture Overview

```
┌─────────────────────────────────────────────┐
│  Render.com (Docker Container)              │
│  ┌─────────────────────────────────────────┐│
│  │  Spring Boot App (port 8080)            ││
│  │  - JWT Authentication                   ││
│  │  - Register/Login endpoints              ││
│  │  - Protected API routes                 ││
│  └─────────────────────────────────────────┘│
│          ↓ (connects to)                     │
└─────────────────────────────────────────────┘
         ↓
┌──────────────────────────────────┐
│  Supabase PostgreSQL Database    │
│  - Cloud-hosted PostgreSQL       │
│  - Automatic backups             │
│  - SSL/TLS encryption            │
└──────────────────────────────────┘
```

---

## Step 1: Set Up Supabase PostgreSQL Database

### 1.1 Create a Supabase Account

1. Go to [https://supabase.com](https://supabase.com)
2. Click "Start your project" or sign up
3. Sign in with GitHub/Google/Email
4. Click "New project"

### 1.2 Create a New Project

- **Name**: `infinite-prints-prod`
- **Database Password**: Generate a strong password (save it!)
- **Region**: Choose closest to your users (e.g., US West for North America)
- Click "Create new project"

Wait 2-3 minutes for the database to initialize.

### 1.3 Get Your Database Connection Details

Once created, go to **Settings → Database → Connection String**

You'll see options for:
- **URI** (use this)
- **psql** command
- **JDBC** string

**Copy the URI**, it looks like:
```
postgresql://postgres:[PASSWORD]@db.xxxxx.supabase.co:5432/postgres
```

Or use the **JDBC** format (recommended for Java):
```
jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres?sslmode=require
```

### 1.4 Note Your Credentials

Save these in a secure location (password manager):
- **Host**: `db.xxxxx.supabase.co`
- **Port**: `5432`
- **Database**: `postgres`
- **Username**: `postgres`
- **Password**: (the one you set during project creation)

---

## Step 2: Prepare Your Application for Production

### 2.1 Update `application-prod.properties`

The file should read environment variables for production. Check/update it:

```properties
spring.application.name=ecommerce-backend

# Database (read from env variables)
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=20

# JPA (create tables if missing, don't update existing)
spring.jpa.hibernate.ddl-auto=validate

# Flyway disabled (we'll handle migrations manually or via external tool)
spring.flyway.enabled=false

# JWT (read from env)
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=${APP_JWT_EXPIRATION:86400000}
app.jwt.refresh-expiration=${APP_JWT_REFRESH_EXPIRATION:604800000}

# CORS (production origins)
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:https://yourdomain.com,https://www.yourdomain.com}
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
app.cors.max-age=3600

# Server
server.port=8080
server.servlet.context-path=/api

# Production logging
logging.level.root=INFO
logging.level.com.infiniteprints=INFO

# Actuator (expose health and info only)
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
```

### 2.2 Create `.env.production` (for local testing)

```bash
# Database (Supabase)
DB_URL=jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=your_supabase_password
DB_NAME=postgres

# JWT Secret (generate a secure random 64+ character string)
JWT_SECRET=your-super-secure-random-jwt-secret-64-characters-long-change-this-to-something-secure

# CORS
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

**To generate a secure JWT secret:**
```bash
openssl rand -base64 64
```

### 2.3 Initialize Production Database Schema

Before deploying, you need to create the database tables on Supabase.

**Option A: Run migration locally against Supabase**

```bash
# Set environment to prod and connect to Supabase
export DB_URL="jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres?sslmode=require"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_supabase_password"
export SPRING_PROFILES_ACTIVE=prod

# Run the app once to initialize schema (Hibernate will create tables)
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

Stop after seeing "Started EcommerceBackendApplication". Tables are now created.

**Option B: Run SQL directly on Supabase**

1. Go to Supabase → **SQL Editor**
2. Create a new query
3. Paste the contents of `src/main/resources/db/seed/local_seed.sql` (adjust for production)
4. Run it

### 2.4 Create Production Admin User (in Supabase)

In Supabase SQL Editor, create an admin:

```sql
-- Generate a bcrypt hash for password "admin_password" (use an online tool or command)
-- For demo, using a pre-generated hash (do NOT use in production)
INSERT INTO users (id, email, password, first_name, last_name, created_at)
VALUES (
  gen_random_uuid(),
  'admin@infiniteprints.com',
  '$2a$10$HASH_HERE', -- Replace with actual bcrypt hash of your password
  'Admin',
  'User',
  NOW()
);

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE email = 'admin@infiniteprints.com';
```

To generate a bcrypt hash:
- Online: https://bcrypt-generator.com/ (type your password, copy hash)
- Or use: `htpasswd -bnBC 10 "" your_password | tr -d ':\n'`

---

## Step 3: Configure Render Deployment

### 3.1 Create a Render Account

1. Go to [https://render.com](https://render.com)
2. Sign up with GitHub (recommended for easier deployments)
3. Authorize access to your GitHub repositories

### 3.2 Connect Your GitHub Repository to Render

1. On Render dashboard, click **"New +" → "Web Service"**
2. Choose **"Build and deploy from a Git repository"**
3. Search for your repo: `Infinite-Prints` or `infinite-layers-backend`
4. Select it and click **"Connect"**

### 3.3 Configure the Web Service

**Basic Settings:**
- **Name**: `infinite-prints-api` (or your preferred name)
- **Region**: Choose closest to users (e.g., "Oregon" for US West)
- **Branch**: `main` (or your production branch, e.g., `feat/user-auth` for testing)

**Build & Deploy:**
- **Runtime**: `Docker`
- **Build Command**: (leave blank — Render will use Dockerfile)
- **Start Command**: (leave blank — Dockerfile specifies CMD)

### 3.4 Add Environment Variables

Click **"Environment"** and add these:

```
# Database (from Supabase)
DB_URL=jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=<your_supabase_password>

# JWT Secret (generate with: openssl rand -base64 64)
JWT_SECRET=<your-64-character-secure-random-string>

# CORS
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

**CRITICAL:** Use separate secure values for each environment variable. Never hardcode secrets.

### 3.5 Configure Health Check (Optional but Recommended)

Under **"Health Check"**:
- **Health Check Path**: `/api/actuator/health`
- **Health Check Protocol**: `HTTPS`

### 3.6 Create the Service

Click **"Create Web Service"**.

Render will now:
1. Build your Docker image
2. Push it to Render's registry
3. Deploy the container
4. Start your app on a public URL

**Wait 5-10 minutes for the build and deployment to complete.**

### 3.7 Get Your Production URL

Once deployed, Render will give you a URL like:
```
https://infinite-prints-api.onrender.com
```

Your API is now live at:
```
https://infinite-prints-api.onrender.com/api
```

---

## Step 4: Test Production Deployment

### 4.1 Test Health Endpoint

```bash
curl https://infinite-prints-api.onrender.com/api/actuator/health
```

Expected: HTTP 200 with health status

### 4.2 Test Register

```bash
curl -X POST https://infinite-prints-api.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "produser@example.com",
    "password": "secure_password",
    "firstName": "Prod",
    "lastName": "User"
  }'
```

### 4.3 Test Login

```bash
curl -s -X POST https://infinite-prints-api.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "produser@example.com", "password": "secure_password"}' | jq .
```

You should get a JWT token.

---

## Step 5: Update `docker-compose.prod.yml` (Reference)

Your existing `docker-compose.prod.yml` is for local testing with prod config. On Render, Docker Compose is **not used** — Render runs your Dockerfile directly.

However, you can use it locally to test prod config:

```bash
# Test locally with prod config before deploying
docker compose -f docker-compose.prod.yml up --build
```

This requires you to set env vars in `.env.prod` or pass them:

```bash
DB_URL=jdbc:postgresql://... docker compose -f docker-compose.prod.yml up
```

---

## Step 6: Dockerfile Verification

Verify your `Dockerfile` exists and is correct:

```dockerfile
# Multi-stage build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package -DskipTests

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
```

If no Dockerfile exists, create one (as shown above).

---

## Step 7: CI/CD Pipeline (Optional)

### Auto-deploy on Push

Render automatically redeploys when you push to your branch. To ensure clean builds:

1. Push to your main/prod branch
2. Render triggers a new build
3. Old deployment is replaced with new one

You can also manually trigger a redeploy on Render dashboard:
- Click your service
- Click **"Manual Deploy"** → **"Deploy Latest Commit"**

---

## Step 8: Monitoring & Logs

### View Logs on Render

1. Go to your service on Render dashboard
2. Click **"Logs"** tab
3. See real-time application logs

### Monitor Performance

- Check memory/CPU usage in Render dashboard
- Monitor database connections in Supabase dashboard

### Set Up Alerts (Render Pro)

Paid plans support email alerts for crashes/errors.

---

## Step 9: Custom Domain (Optional)

To use your own domain instead of `onrender.com`:

1. On Render, click your service
2. Go to **"Settings" → "Custom Domains"**
3. Add your domain (e.g., `api.infiniteprints.com`)
4. Update your DNS records to point to Render's CNAME
5. Wait for SSL certificate provisioning (automatic)

---

## Troubleshooting

### App won't start on Render

**Check logs:**
```
Render Dashboard → Your Service → Logs
```

**Common issues:**
- **Database connection error**: Verify `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars in Render
- **Port conflict**: Ensure app uses `server.port=8080`
- **Missing dependencies**: Check `pom.xml` and rebuild

### Database connection timeout

- Verify Supabase project is running (check Supabase dashboard)
- Ensure Render IP is allowed (Supabase should allow all IPs by default)
- Check `sslmode=require` in `DB_URL`

### JWT token errors

- Verify `JWT_SECRET` is the same value in Render env vars
- Ensure it's at least 64 characters and matches what was used to generate tokens

### CORS errors from frontend

- Update `CORS_ALLOWED_ORIGINS` in Render env vars to include your frontend domain
- Example: `https://yourdomain.com,https://www.yourdomain.com`

---

## Security Checklist for Production

- ✅ JWT secret is 64+ random characters
- ✅ Database password is strong (20+ chars, mixed case, numbers, symbols)
- ✅ `SPRING_PROFILES_ACTIVE=prod` is set
- ✅ `spring.jpa.hibernate.ddl-auto=validate` (don't auto-create/update in prod)
- ✅ CORS origins are restricted to your domain(s)
- ✅ HTTPS is enforced (Render does this automatically)
- ✅ Database backups enabled (Supabase does this automatically)
- ✅ Logging level is INFO (not DEBUG in prod)
- ✅ Admin user created in database
- ✅ Health endpoint is accessible (for monitoring)

---

## Useful Commands & Links

**Generate secure JWT secret:**
```bash
openssl rand -base64 64
```

**Test Supabase connection locally:**
```bash
psql "postgresql://postgres:PASSWORD@db.xxxxx.supabase.co:5432/postgres?sslmode=require"
```

**Render dashboard:**
https://dashboard.render.com

**Supabase dashboard:**
https://app.supabase.com

**View deployment logs:**
Render Dashboard → Your Service → Logs

---

## Next Steps

1. Set up Supabase PostgreSQL ✅
2. Get database credentials ✅
3. Initialize production schema ✅
4. Create admin user in database ✅
5. Connect Render to your GitHub repo ✅
6. Add environment variables to Render ✅
7. Deploy and test ✅
8. Set up custom domain (optional) ✅
9. Monitor logs and performance ✅

Your production app is now live! 🚀

---

## Support & References

- **Render Docs**: https://render.com/docs
- **Supabase Docs**: https://supabase.com/docs
- **Spring Boot Production Docs**: https://spring.io/guides/gs/spring-boot/
- **Docker Best Practices**: https://docs.docker.com/develop/dev-best-practices/

