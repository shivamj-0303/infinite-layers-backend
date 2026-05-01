# Step-by-Step: Deploy Infinite Prints to Production (Render + Supabase)

This is a beginner-friendly, detailed guide with exact commands and screenshots reference.

---

## Table of Contents
1. [Set Up Supabase Database](#1-set-up-supabase-database)
2. [Prepare Application for Production](#2-prepare-application-for-production)
3. [Deploy to Render](#3-deploy-to-render)
4. [Test Your Live API](#4-test-your-live-api)
5. [Troubleshooting](#5-troubleshooting)

---

## 1. Set Up Supabase Database

### Step 1.1: Create Supabase Account
1. Open browser and go to: **https://supabase.com**
2. Click **"Start your project"** (top right)
3. Sign up with GitHub (easiest) or email
4. Verify email if needed
5. You're logged in!

### Step 1.2: Create New Project
1. Click **"New Project"** (or **"+"** button)
2. Fill in the form:
   - **Project name**: `infinite-prints-prod`
   - **Database password**: Enter a strong password (20+ characters, mix of upper/lower/numbers/symbols)
     - Example: `Pr0duct10n!@#$%^&*()`
     - **Save this password in a password manager!**
   - **Region**: Choose closest to your users
     - For North America: "US East" or "US West"
     - For Europe: "EU West"
   - Click **"Create new project"**
3. **Wait 2-3 minutes** for database to initialize (you'll see a loading screen)
4. You'll see the Supabase dashboard once ready

### Step 1.3: Get Your Database Connection Details

**Find these in Supabase:**

1. On the left sidebar, click **"Settings"** (bottom)
2. Click **"Database"**
3. Scroll to **"Connection string"**
4. You'll see three tabs: "URI", "JDBC", "psql"

**Copy the JDBC format** (recommended for Java):
```
jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres?sslmode=require
```

Extract these details and save them (in a text file or password manager):

| Detail | Value | Example |
|--------|-------|---------|
| **Host** | From URL (db.xxxxx.supabase.co) | `db.xxxxxxxxxxxxx.supabase.co` |
| **Port** | Always 5432 | `5432` |
| **Database** | Always `postgres` | `postgres` |
| **Username** | Always `postgres` | `postgres` |
| **Password** | The one you created | `Pr0duct10n!@#$%^&*()` |

### Step 1.4: Create Database Schema (Run SQL)

**Important:** You need to run SQL on Supabase to create the tables.

1. In Supabase dashboard, click **"SQL Editor"** (left sidebar)
2. Click **"New Query"** (top right)
3. Copy and paste the entire content from this file:
   ```
   /home/shivam/Desktop/Infinite-Prints/src/main/resources/db/migration/supabase_prod_init.sql
   ```
4. **Replace the bcrypt password hash** in the SQL:
   - Find this line:
     ```sql
     '$2a$10$ABC123DEF456GHI789JKL.MNO/PQR.STU/VWXYZ.ABCDEFGH'
     ```
   - Replace it with your actual bcrypt hash

**To generate a bcrypt hash for your admin password:**

Option A (Online, easiest):
- Go to: **https://bcrypt-generator.com**
- Enter your desired password (e.g., "MySecureAdminPassword123!")
- Copy the generated hash

Option B (Command line):
```bash
# Run this on your Mac/Linux
echo -n "MySecureAdminPassword123!" | htpasswd -bnBC 10 "" - | tr -d ':\n'
```

5. Click **"Run"** (bottom right, or Ctrl+Enter)
6. You should see: "Schema initialization complete"

**Verify tables were created:**
- Still in SQL Editor, run:
  ```sql
  SELECT tablename FROM pg_tables WHERE schemaname = 'public';
  ```
- You should see: `users`, `user_roles`, `jwt_tokens`, `audit_log`

---

## 2. Prepare Application for Production

### Step 2.1: Generate a Secure JWT Secret

Open terminal on your machine:

```bash
# Generate 64-character random secret
openssl rand -base64 64
```

Copy the output. Example:
```
abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890
```

**Save this secret securely** (password manager recommended).

### Step 2.2: Verify Application Name is Updated

Check both property files use `infiniteprints-backend`:

```bash
# Check dev config
grep "spring.application.name" src/main/resources/application.properties

# Check prod config
grep "spring.application.name" src/main/resources/application-prod.properties
```

Both should output: `spring.application.name=infiniteprints-backend`

If not, they've been updated automatically for you.

### Step 2.3: Update Production Config (if needed)

Verify `/src/main/resources/application-prod.properties` looks like this:

```properties
spring.application.name=infiniteprints-backend

# Production: values must be provided via environment
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=20

# JPA (validate only, don't modify schema)
spring.jpa.hibernate.ddl-auto=validate

# Flyway disabled (migrations handled externally)
spring.flyway.enabled=false

# JWT secret from environment
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=${APP_JWT_EXPIRATION:86400000}
app.jwt.refresh-expiration=${APP_JWT_REFRESH_EXPIRATION:604800000}

# CORS for production
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:https://yourdomain.com}
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS

# Server
server.port=8080
server.servlet.context-path=/api

# Logging
logging.level.root=WARN

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
management.server.servlet.context-path=/actuator
```

### Step 2.4: Commit Changes to Git

```bash
cd /home/shivam/Desktop/Infinite-Prints

git add -A
git commit -m "feat: update app name to infiniteprints-backend and prepare for production"
git push origin feat/user-auth
```

---

## 3. Deploy to Render

### Step 3.1: Create Render Account

1. Open browser and go to: **https://render.com**
2. Click **"Sign Up"** (top right)
3. Sign up with GitHub (recommended, easier)
4. Authorize Render to access your GitHub account
5. You're logged in!

### Step 3.2: Connect GitHub Repository

1. On Render dashboard, click **"New +"** (top right)
2. Choose **"Web Service"**
3. Under "Build and deploy from a Git repository", find your repo:
   - Search for: `Infinite-Prints` or `infinite-layers-backend`
   - Click on it
4. Click **"Connect"**

Render is now connected to your GitHub repo.

### Step 3.3: Configure the Web Service

Fill in the form:

| Field | Value | Notes |
|-------|-------|-------|
| **Name** | `infiniteprints-api` | (or any name you prefer) |
| **Region** | `Oregon` (or closest to users) | US East/West, EU, etc. |
| **Branch** | `feat/user-auth` | (your current branch) |
| **Runtime** | `Docker` | (important! select this) |

Click **"Create Web Service"**.

### Step 3.4: Add Environment Variables

On the next page, scroll to **"Environment"** section.

Click **"Add Environment Variable"** and enter these **one by one**:

1. **DB_URL**
   - Key: `DB_URL`
   - Value: Copy from Supabase JDBC string
   - Example: `jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres?sslmode=require`

2. **DB_USERNAME**
   - Key: `DB_USERNAME`
   - Value: `postgres` (always postgres)

3. **DB_PASSWORD**
   - Key: `DB_PASSWORD`
   - Value: The Supabase password you created (e.g., `Pr0duct10n!@#$%^&*()`)

4. **JWT_SECRET**
   - Key: `JWT_SECRET`
   - Value: The bcrypt secret you generated earlier

5. **SPRING_PROFILES_ACTIVE**
   - Key: `SPRING_PROFILES_ACTIVE`
   - Value: `prod`

6. **CORS_ALLOWED_ORIGINS** (optional, for your frontend domain later)
   - Key: `CORS_ALLOWED_ORIGINS`
   - Value: `https://yourdomain.com` (replace with your domain, or use localhost:3000 for now)

### Step 3.5: Deploy

Click **"Create Web Service"** at the bottom.

Render will now:
1. Pull your code from GitHub
2. Build the Docker image (takes 3-5 minutes)
3. Deploy and start your app (takes 1-2 minutes)

**Watch the Logs tab** — you should see Spring Boot starting. Wait until you see:
```
Started EcommerceBackendApplication in X.XXX seconds
```

Once you see that, your app is **LIVE**! 🚀

### Step 3.6: Get Your Production URL

In the Render dashboard, at the top of your service, you'll see a URL like:
```
https://infiniteprints-api.onrender.com
```

This is your **live production API URL**!

Your endpoints are now at:
- **Health**: `https://infiniteprints-api.onrender.com/api/actuator/health`
- **Register**: `https://infiniteprints-api.onrender.com/api/auth/register`
- **Login**: `https://infiniteprints-api.onrender.com/api/auth/login`

---

## 4. Test Your Live API

### Test 4.1: Health Check

```bash
curl -i https://infiniteprints-api.onrender.com/api/actuator/health
```

Expected response: **HTTP 200 OK** with JSON health status

### Test 4.2: Register a User

```bash
curl -X POST https://infiniteprints-api.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testprod@example.com",
    "password": "TestPassword123!",
    "firstName": "Test",
    "lastName": "User"
  }'
```

Expected: **HTTP 200** with body `"registered"`

### Test 4.3: Login and Get Token

```bash
curl -s -X POST https://infiniteprints-api.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "testprod@example.com", "password": "TestPassword123!"}' | jq .
```

Expected output:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

**Copy the token** (everything after `"accessToken": "`).

### Test 4.4: Use Token on Protected Endpoint

```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
curl -H "Authorization: Bearer $TOKEN" https://infiniteprints-api.onrender.com/api/actuator/health
```

Expected: **HTTP 200** (with token) instead of 401 (without token)

---

## 5. Troubleshooting

### Problem: "App failed to build"

**Check the logs:**
1. Go to Render dashboard
2. Click your service
3. Click **"Logs"** tab
4. Look for error messages

**Common causes:**
- Missing Maven dependencies → Run `mvn clean package` locally
- Dockerfile not found → Ensure `Dockerfile` exists in repo root
- Java version mismatch → Check Java 21 is available

**Fix:**
```bash
./mvnw clean compile
git add -A
git commit -m "fix: rebuild after dependency update"
git push origin feat/user-auth
```

Then, on Render dashboard, click **"Manual Deploy"** → **"Deploy latest commit"**.

### Problem: "Database connection error"

**Check your environment variables:**
1. Render dashboard → Your service → **"Environment"**
2. Verify all DB env vars are correct:
   - `DB_URL` matches Supabase JDBC string
   - `DB_USERNAME` is `postgres`
   - `DB_PASSWORD` matches Supabase password
3. Verify Supabase database is running (check Supabase dashboard)

**Test locally:**
```bash
# Verify Supabase connection
psql "postgresql://postgres:YourPassword@db.xxxxx.supabase.co:5432/postgres?sslmode=require"
```

If that fails, your Supabase credentials are wrong.

### Problem: "Login returns 401 (Unauthorized)"

**Most likely causes:**
1. User doesn't exist (didn't register first)
2. Bcrypt hash for admin user is wrong in SQL
3. JWT secret mismatch

**Fix:**
1. Register a new user (see Test 4.2)
2. Try logging in with that user
3. If still fails, regenerate JWT secret and update Render env var

### Problem: "CORS errors from frontend"

**Update CORS in Render env vars:**
1. Go to Render dashboard → Your service → Environment
2. Update `CORS_ALLOWED_ORIGINS` to include your frontend domain:
   ```
   https://yourdomain.com,https://www.yourdomain.com,http://localhost:3000
   ```
3. Click **"Save"** and redeploy

---

## Checklist: You're Done! ✅

- [ ] Supabase account created
- [ ] Database created on Supabase
- [ ] SQL schema initialized on Supabase
- [ ] Admin user created in database
- [ ] JWT secret generated and saved
- [ ] App name updated to `infiniteprints-backend`
- [ ] Changes committed to Git
- [ ] Render account created
- [ ] GitHub connected to Render
- [ ] Environment variables added to Render
- [ ] App deployed to Render
- [ ] Health endpoint returns 200
- [ ] Register endpoint works
- [ ] Login endpoint returns JWT token
- [ ] Protected endpoint works with token

**Your production app is now LIVE!** 🎉

---

## Next Steps (After Going Live)

1. **Set up a custom domain** (instead of `.onrender.com`)
   - Render → Your service → Settings → Custom Domains
   - Add your domain (e.g., `api.infiniteprints.com`)
   - Update DNS records as instructed

2. **Monitor logs and performance**
   - Render dashboard → Logs tab (watch for errors)
   - Supabase dashboard → Database → Monitor (watch for slow queries)

3. **Implement more features**
   - Cart management
   - Order creation
   - Product catalog
   - Payment integration

4. **Set up CI/CD alerts** (paid Render plan)
   - Email alerts on deployment failures
   - Performance monitoring

---

## Support & Reference Links

- **Render docs**: https://render.com/docs
- **Supabase docs**: https://supabase.com/docs
- **Spring Boot**: https://spring.io/projects/spring-boot
- **PostgreSQL**: https://www.postgresql.org/docs/
- **Docker**: https://docs.docker.com/

---

**Questions?** Check the troubleshooting section or review the detailed guides above.

Good luck! 🚀
