#!/bin/bash

# Production Deployment Script
# Requires: Docker, Docker Compose, and environment variables set

set -e

echo "🚀 Starting Infinite Prints Backend Production Deployment..."

# Check if required environment variables are set
required_vars=("DB_USERNAME" "DB_PASSWORD" "JWT_SECRET" "CORS_ALLOWED_ORIGINS")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "❌ Error: Required environment variable '$var' is not set"
        exit 1
    fi
done

echo "✅ Environment variables validated"

# Build and start containers
echo "📦 Building Docker images..."
docker-compose build --no-cache

echo "🐳 Starting containers..."
docker-compose up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
for i in {1..30}; do
    if docker-compose exec -T postgres pg_isready -U "$DB_USERNAME" > /dev/null 2>&1; then
        echo "✅ PostgreSQL is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ PostgreSQL failed to start"
        exit 1
    fi
    sleep 1
done

echo "⏳ Waiting for application to start..."
sleep 20

# Check application health
for i in {1..30}; do
    if curl -f http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        echo "✅ Application is healthy"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Application failed to start"
        docker-compose logs app
        exit 1
    fi
    sleep 1
done

echo ""
echo "✅ Deployment completed successfully!"
echo ""
echo "Service URLs:"
echo "  - Application: https://localhost/api"
echo "  - Health Check: https://localhost/api/actuator/health"
echo "  - Database: localhost:5432"
echo ""
echo "To view logs:"
echo "  docker-compose logs -f app"
echo ""
echo "To stop services:"
echo "  docker-compose down"
