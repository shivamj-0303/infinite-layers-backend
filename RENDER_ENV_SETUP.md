# How to Ensure application-prod.properties is Used on Render

## Quick Answer
**Two things must happen:**

1. ✅ **Dockerfile has `-Dspring.profiles.active=prod`** (Already done)
2. ✅ **Render has `SPRING_PROFILES_ACTIVE=prod` environment variable** (You must set this)

---

## How Spring Boot Loads Properties Files

When you start a Spring Boot app, it loads properties in this order:

```
1. application.properties           (default, always loaded)
   ↓
2. application-{PROFILE}.properties (if PROFILE is active)
```

**So if `prod` profile is active, it loads:**
```
1. application.properties
2. application-prod.properties (overrides values from step 1)
```

---

## Setting the Profile: Two Methods

### Method 1: Dockerfile (What You Have Now)

Your `Dockerfile` contains:
```dockerfile
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+ParallelRefProcEnabled -Dfile.encoding=UTF-8 -Dspring.profiles.active=prod"
```

**How it works:**
- When Docker starts the container, it sets the `JAVA_OPTS` environment variable
- This environment variable is passed to the Java process as: `-Dspring.profiles.active=prod`
- This tells Spring Boot to load `application-prod.properties`

**✅ This works and ensures `application-prod.properties` is loaded.**

---

### Method 2: Render Environment Variable (CLEANER - Recommended)

You can also set it in Render dashboard:

**Steps:**
1. Go to Render Dashboard
2. Click your Service
3. Click **"Environment"** section
4. Click **"Add Environment Variable"**
5. Add this variable:
   ```
   Key: SPRING_PROFILES_ACTIVE
   Value: prod
   ```

**How it works:**
- Render injects this into the Docker container's environment
- Spring Boot reads this environment variable automatically
- It activates the `prod` profile
- `application-prod.properties` gets loaded

**Advantage:** You can change the profile without rebuilding the Docker image!

---

## Complete Render Environment Variables Checklist

Make sure you have ALL these in Render Dashboard → Environment:

| Variable | Value | Purpose |
|----------|-------|---------|
| `SPRING_PROFILES_ACTIVE` | `prod` | **Activates prod profile** ← Most important! |
| `DB_URL` | `jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres?sslmode=require` | Database connection |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | Your Supabase password | Database password |
| `JWT_SECRET` | Your 64-char secret | JWT signing key |
| `SERVER_PORT` | `8080` | Port Spring listens on |

---

## How to Verify It's Working

Once your app is deployed, check the logs for this message:

```
The following profiles are active: prod
```

Or check the health endpoint:
```bash
curl https://your-render-service.onrender.com/api/actuator/health
```

---

## What Properties Get Used?

### From `application.properties` (Dev Defaults)
```properties
spring.application.name=infiniteprints-backend
server.port=${SERVER_PORT:8080}
server.servlet.context-path=/api
```

### From `application-prod.properties` (Prod Overrides)
```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
logging.file.path=/app/logs
# ... and all other prod-specific settings
```

**Result:** Prod properties override dev properties!

---

## If You Still See Dev Properties

If you see the app trying to connect to `localhost:5432` instead of your Supabase instance, it means:

❌ **The `prod` profile is NOT active**

**Fix:**
1. Go to Render Dashboard → Your Service → Environment
2. Add `SPRING_PROFILES_ACTIVE=prod`
3. Click "Save"
4. Click "Manual Deploy" → "Deploy latest commit"
5. Check logs again

---

## Summary

✅ Your **Dockerfile** already forces `prod` profile via `-Dspring.profiles.active=prod`

✅ You **should also** set `SPRING_PROFILES_ACTIVE=prod` in Render's Environment section (for consistency and flexibility)

**Result:** `application-prod.properties` will definitely be loaded and used.

---

## Quick Reference: Spring Profile Loading

```
Java Startup
    ↓
Read SPRING_PROFILES_ACTIVE env var (from Render)
    ↓
Apply JVM arg -Dspring.profiles.active=prod (from Dockerfile)
    ↓
Spring Boot loads:
  1. application.properties
  2. application-prod.properties (overrides)
    ↓
Your app uses prod configuration!
```

---

## Need to Debug?

Add this to your `application-prod.properties`:
```properties
logging.level.org.springframework.boot.context.config=DEBUG
```

Then you'll see log messages like:
```
Including profiles: prod
Loaded config from: application-prod.properties
```

This confirms prod properties were loaded!
