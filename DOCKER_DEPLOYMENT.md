# Docker Deployment Guide for QueryX

## Overview
This guide explains how to build and deploy the QueryX application using Docker.

## Prerequisites
- Docker installed (version 20.10+)
- Docker Compose installed (version 1.29+)
- Gemini API key from Google Cloud
- PostgreSQL database credentials

## Environment Variables

The application uses the following environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | 8080 |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | See default in properties |
| `SPRING_DATASOURCE_USERNAME` | Database username | neondb_owner |
| `SPRING_DATASOURCE_PASSWORD` | Database password | See default in properties |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME` | JDBC driver | org.postgresql.Driver |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL mode | validate |
| `GEMINI_API_KEY` | Google Gemini API key | (empty - REQUIRED) |
| `GEMINI_API_URL` | Gemini API endpoint URL | https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent |
| `LOGGING_LEVEL_ROOT` | Root logging level | INFO |
| `LOGGING_LEVEL_COM_EXAMPLE_DEMO` | Application logging level | DEBUG |

## Build Instructions

### Option 1: Using Docker Compose (Recommended)

1. **Create `.env` file** in project root with your configuration:
```bash
GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
SPRING_DATASOURCE_URL=jdbc:postgresql://your-host:5432/your-db?sslmode=require
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
```

2. **Build and start the application**:
```bash
docker-compose up --build
```

3. **Access the application**:
```
http://localhost:8080
```

4. **Stop the application**:
```bash
docker-compose down
```

### Option 2: Using Docker CLI

1. **Build the image**:
```bash
docker build -t queryx:latest .
```

2. **Run the container**:
```bash
docker run -d \
  --name queryx-app \
  -p 8080:8080 \
  -e GEMINI_API_KEY="YOUR_GEMINI_API_KEY" \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://your-host:5432/your-db?sslmode=require" \
  -e SPRING_DATASOURCE_USERNAME="your_username" \
  -e SPRING_DATASOURCE_PASSWORD="your_password" \
  queryx:latest
```

3. **View logs**:
```bash
docker logs -f queryx-app
```

4. **Stop the container**:
```bash
docker stop queryx-app
docker rm queryx-app
```

## Docker Image Details

### Base Images
- **Build Stage**: `maven:3.9.0-eclipse-temurin-17` - for compiling the application
- **Runtime Stage**: `eclipse-temurin:17-jre-alpine` - lightweight JRE for running the application

### Image Size
- Approximately 300-400 MB (Alpine base is much smaller than full JDK)

### Health Check
- Endpoint: `GET /schema/tables`
- Interval: 30 seconds
- Timeout: 10 seconds
- Retries: 3
- Start period: 40 seconds

## Pushing to Docker Registry

### Push to Docker Hub

1. **Tag the image**:
```bash
docker tag queryx:latest your-username/queryx:latest
```

2. **Login to Docker Hub**:
```bash
docker login
```

3. **Push the image**:
```bash
docker push your-username/queryx:latest
```

### Push to GitHub Container Registry

1. **Create Personal Access Token** (PAT) with `write:packages` permission

2. **Login to GHCR**:
```bash
echo $YOUR_PAT | docker login ghcr.io -u your-username --password-stdin
```

3. **Tag the image**:
```bash
docker tag queryx:latest ghcr.io/your-username/queryx:latest
```

4. **Push the image**:
```bash
docker push ghcr.io/your-username/queryx:latest
```

## Server Deployment

### Deploy on VPS (Ubuntu/Debian)

1. **Install Docker** on the server:
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

2. **Install Docker Compose**:
```bash
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

3. **Clone or transfer your project** to the server:
```bash
git clone https://github.com/your-username/queryx.git
cd queryx
```

4. **Create `.env` file** with production values:
```bash
nano .env
```

5. **Deploy with Docker Compose**:
```bash
docker-compose up -d
```

6. **Verify deployment**:
```bash
docker-compose ps
docker-compose logs -f
```

### Deploy on Kubernetes

1. **Create deployment manifest** (`k8s-deployment.yaml`):
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: queryx-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: queryx
  template:
    metadata:
      labels:
        app: queryx
    spec:
      containers:
      - name: queryx
        image: your-username/queryx:latest
        ports:
        - containerPort: 8080
        env:
        - name: GEMINI_API_KEY
          valueFrom:
            secretKeyRef:
              name: queryx-secrets
              key: gemini-api-key
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://your-db-host:5432/neondb?sslmode=require"
        - name: SPRING_DATASOURCE_USERNAME
          value: "neondb_owner"
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: queryx-secrets
              key: db-password
        livenessProbe:
          httpGet:
            path: /schema/tables
            port: 8080
          initialDelaySeconds: 40
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /schema/tables
            port: 8080
          initialDelaySeconds: 40
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: queryx-service
spec:
  selector:
    app: queryx
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

2. **Create secrets**:
```bash
kubectl create secret generic queryx-secrets \
  --from-literal=gemini-api-key=YOUR_GEMINI_API_KEY \
  --from-literal=db-password=YOUR_DB_PASSWORD
```

3. **Deploy**:
```bash
kubectl apply -f k8s-deployment.yaml
```

## Troubleshooting

### Container won't start
```bash
docker logs queryx-app
```

### Health check failing
```bash
docker exec queryx-app curl http://localhost:8080/schema/tables
```

### Out of memory
```bash
docker run -d --memory=1g --memory-swap=2g ...
```

### Network issues
```bash
docker network ls
docker network inspect bridge
```

## Production Checklist

- [ ] Replace hardcoded credentials with environment variables
- [ ] Set `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` (never use `update` in production)
- [ ] Enable HTTPS/TLS at the reverse proxy level
- [ ] Set up proper logging and monitoring
- [ ] Configure resource limits (memory, CPU)
- [ ] Set up automated backups for database
- [ ] Test health checks and restart policies
- [ ] Document API endpoints for frontend team
- [ ] Set up CI/CD pipeline for automatic builds and deployments
- [ ] Monitor application metrics and logs

## Quick Reference Commands

```bash
# Build image
docker build -t queryx:latest .

# Run container
docker run -d -p 8080:8080 -e GEMINI_API_KEY="key" queryx:latest

# View logs
docker logs -f queryx-app

# Stop container
docker stop queryx-app

# Remove container
docker rm queryx-app

# Docker Compose
docker-compose up --build
docker-compose down
docker-compose logs -f

# Push to registry
docker tag queryx:latest your-username/queryx:latest
docker push your-username/queryx:latest
```

