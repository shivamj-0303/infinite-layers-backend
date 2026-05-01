# Render Deployment Fix: "No open ports detected" Error

The error you're seeing means **Spring Boot is crashing on startup** before it can bind to port 8080. Here's how to fix it:

---

## Root Cause

Your app is likely failing because:
1. **Database connection is failing** (Render can't reach Supabase)
2. **Environment variables are missing or incorrect** on Render
3. **Invalid JDBC URL format** for Supabase

---

## Solution: Step-by-Step

### Step 1: Verify Your Supabase Setup is Complete

Go to **Supabase Dashboard** → Your Project → **Settings** → **Database**

Check the **Connection string** section. You need:

| Variable | Where to Find | Example |
|----------|---------------|---------|
| `DB_URL` | JDBC tab in Connection string | `jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres?sslmode=require` |
| `DB_USERNAME` | Always `postgres` | `postgres` |
| `DB_PASSWORD` | The password you set when creating the project | `MySecurePassword123!` |

**Important:** Make sure you've already:
- [ ] Created the Supabase project
- [ ] Run the SQL schema (`supabase_prod_init.sql`) in Supabase SQL Editor
- [ ] Verified tables exist (run `SELECT * FROM users;` in Supabase)

### Step 2: Update Render Environment Variables

Go to **Render Dashboard** → Your Service → **Environment**

Delete all existing environment variables and create these fresh:

#### Required Variables:

1. **DB_URL**
   ```
   jdbc:postgresql://db.xxxxxxxxxxxxx.supabase.co:5432/postgres?sslmode=require
   ```
   (Copy EXACTLY from Supabase, including `?sslmode=require`)

2. **DB_USERNAME**
   ```
   postgres
   ```

3. **DB_PASSWORD**
   ```
   (Your Supabase password - the one you set during project creation)
   ```

4. **JWT_SECRET**
   ```
   (Your 64-character secret from earlier)
   ```

5. **SPRING_PROFILES_ACTIVE**
   ```
   prod
   ```

6. **SERVER_PORT**
   ```
   8080
   ```

**After adding all variables, click "Save".**

### Step 3: Rebuild and Redeploy

In Render dashboard:
1. Click your service
2. Click **"Manual Deploy"** button (top right)
3. Select **"Deploy latest commit"**
4. Watch the logs (Logs tab)

**Expected log sequence:**
```
#20 exporting cache to client directory
#20 writing cache image manifest...
Pushing image to registry...
Upload succeeded
==> Deploying...
==> Setting WEB_CONCURRENCY=1 by default
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
 :: Spring Boot ::                (v4.0.5)

2026-05-01 14:26:05.649 INFO com.infiniteprints.platform.ecommerce.EcommerceBackendApplication : Starting EcommerceBackendApplication
...
2026-05-01 14:26:25.123 INFO o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat started on port(s): 8080
2026-05-01 14:26:25.456 INFO com.infiniteprints.platform.ecommerce.EcommerceBackendApplication : Started EcommerceBackendApplication in 19.807 seconds
```

If you see "**Started EcommerceBackendApplication**", your app is **RUNNING**! ✅

### Step 4: Test Your Live API

Once deployed successfully:

```bash
# Test health endpoint (should return 200)
curl -i https://your-render-service-name.onrender.com/api/actuator/health
```

Replace `your-render-service-name` with your actual service name from Render.

Expected response:
```
HTTP/2 200
{"status":"UP"}
```

---

## Common Issues & Fixes

### Issue 1: "Connection refused" error in logs

**Symptom:**
```
java.sql.SQLException: Connection refused
```

**Fix:**
1. Verify your Supabase project is **actually running** (check Supabase dashboard)
2. Verify your Supabase database password is correct
3. Check if Supabase is in "Paused" state → Resume it

### Issue 2: "Database does not exist" error

**Symptom:**
```
org.postgresql.util.PSQLException: ERROR: database "postgres" does not exist
```

**Fix:**
1. Go to Supabase → SQL Editor
2. Run this query:
   ```sql
   SELECT datname FROM pg_database;
   ```
3. You should see a `postgres` database
4. If not, contact Supabase support
5. If it exists, verify you're using the correct connection string

### Issue 3: "Validation of schema failed" error

**Symptom:**
```
org.hibernate.tool.schema.spi.SchemaManagementException: Schema validation failed
```

**Fix:**
1. Go to Supabase SQL Editor
2. Verify the tables exist:
   ```sql
   SELECT tablename FROM pg_tables WHERE schemaname = 'public';
   ```
3. You should see: `users`, `user_roles`, `jwt_tokens`, `audit_log`
4. If missing, run the SQL schema again:
   - Copy entire content of `src/main/resources/db/migration/supabase_prod_init.sql`
   - Paste into Supabase SQL Editor
   - Click Run

### Issue 4: "No open ports detected" but logs look fine

**Symptom:**
```
Started EcommerceBackendApplication...
==> No open ports detected, continuing to scan...
==> Exited with status 1
```

**Fix:**
This usually means the app starts but then crashes after startup. Check full logs:
1. Render dashboard → Your service → Logs
2. Scroll up to see the error message (usually ~5-10 seconds after "Started")
3. Look for exceptions like `NullPointerException`, `SQLException`, etc.
4. Refer to the specific error above

---

## Verification Checklist

Before deploying again, verify these locally:

```bash
cd /home/shivam/Desktop/Infinite-Prints

# 1. Verify Java version
java -version

# 2. Build the app locally
./mvnw clean package -DskipTests

# 3. Check if JAR was created
ls -lh target/*.jar

# 4. Test with docker-compose (local dev setup)
docker-compose -f docker-compose.local.yml up

# 5. In another terminal, test endpoints
curl http://localhost:8080/api/actuator/health
```

If all these work locally, they'll work on Render.

---

## Quick Reference: Environment Variables for Render

Copy-paste this format into Render (replacing with YOUR values):

```
DB_URL=jdbc:postgresql://db.YOUR_SUPABASE_ID.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=YOUR_SUPABASE_PASSWORD
JWT_SECRET=YOUR_64_CHAR_SECRET
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

---

## Still Having Issues?

1. **Check Render logs in real-time:**
   ```bash
   # If Render has CLI (optional)
   render logs --service your-service-name
   ```

2. **Ask for help with specific error:**
   - Go to Render dashboard → Logs tab
   - Find the error message
   - Search this document or Spring Boot docs for that error

3. **Rollback to working state:**
   - Render dashboard → Deployment tab
   - Click a previous deployment that worked
   - Click "Redeploy"

---

**Once you see "Started EcommerceBackendApplication" in the logs, you're good to go!** 🚀

Test with: `curl https://your-service.onrender.com/api/actuator/health`
