# Deployment Checklist

## Pre-Deployment

### 1. Security
- [ ] Remove hardcoded API keys from code
- [ ] Remove hardcoded database credentials from code
- [ ] All sensitive data now uses environment variables
- [ ] API key set as `GEMINI_API_KEY` environment variable
- [ ] Database credentials set as environment variables
- [ ] `.env` file added to `.gitignore`
- [ ] Review `.env.example` for all required variables

### 2. Configuration Verification
- [ ] `application.properties` uses environment variable syntax
- [ ] All required environment variables documented in `.env.example`
- [ ] Default values appropriate for production
- [ ] Database connection string verified
- [ ] API endpoint URLs correct

### 3. Docker Setup
- [ ] `Dockerfile` created with multi-stage build
- [ ] `docker-compose.yml` created for easy deployment
- [ ] `.dockerignore` configured to exclude unnecessary files
- [ ] Image size optimized (Alpine base)
- [ ] Health checks configured

### 4. Documentation
- [ ] `DOCKER_DEPLOYMENT.md` created with complete guide
- [ ] `README.md` updated with Docker instructions
- [ ] API endpoints documented in `API_DOCUMENTATION.md`
- [ ] Troubleshooting guide updated in `NETWORK_ERROR_FIX.md`

## Local Testing

- [ ] Application builds successfully: `mvn clean package`
- [ ] Application runs locally: `mvn spring-boot:run`
- [ ] API endpoints tested locally
- [ ] Database connection works locally
- [ ] Gemini API integration works with test API key

## Docker Testing

- [ ] Docker image builds: `docker build -t queryx:latest .`
- [ ] Container runs: `docker run -d -p 8080:8080 queryx:latest`
- [ ] Health check passes: `docker exec <container-id> curl http://localhost:8080/schema/tables`
- [ ] Logs readable: `docker logs queryx-app`
- [ ] All environment variables work correctly
- [ ] Docker Compose works: `docker-compose up --build`

## Server Preparation

### VPS/Cloud VM Setup
- [ ] Server OS configured (Ubuntu/Debian recommended)
- [ ] SSH access verified
- [ ] Firewall rules set (allow port 8080 or reverse proxy port)
- [ ] Docker installed on server
- [ ] Docker Compose installed on server
- [ ] Non-root user created with Docker permissions

### Database Setup
- [ ] PostgreSQL database created (if not using managed service)
- [ ] Database user created with appropriate permissions
- [ ] Connection string tested from server
- [ ] Backups configured (if applicable)
- [ ] Database migrations run (if any)

### Secrets Management
- [ ] Create `.env` file on server with production values
- [ ] Gemini API key added to `.env`
- [ ] Database credentials added to `.env`
- [ ] Permissions set on `.env` (read-only for app user)
- [ ] `.env` file NOT committed to repository

## Deployment

### Option 1: Docker Compose Deployment
```bash
# Step 1: Clone repository
git clone https://github.com/your-username/queryx.git
cd queryx

# Step 2: Create .env file with production values
cp .env.example .env
nano .env  # Edit with actual values

# Step 3: Deploy
docker-compose up -d

# Step 4: Verify
docker-compose ps
docker-compose logs -f
```

- [ ] Repository cloned on server
- [ ] `.env` file created with production values
- [ ] `docker-compose up -d` executed successfully
- [ ] All services running: `docker-compose ps`
- [ ] Health check passing
- [ ] No errors in logs: `docker-compose logs`

### Option 2: Docker Registry Deployment
```bash
# Step 1: Push to registry
docker tag queryx:latest your-username/queryx:latest
docker push your-username/queryx:latest

# Step 2: Pull and run on server
docker pull your-username/queryx:latest
docker run -d --name queryx-app \
  -p 8080:8080 \
  -e GEMINI_API_KEY="your-key" \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://..." \
  your-username/queryx:latest
```

- [ ] Image pushed to Docker Hub / GHCR
- [ ] Image pulled successfully on server
- [ ] Container started with all environment variables
- [ ] Health check passing

## Post-Deployment

### Verification
- [ ] Application health check: `/schema/tables`
- [ ] API endpoints responding: Test each endpoint
- [ ] Database queries working
- [ ] Gemini API integration working (test AI query)
- [ ] Logs clean, no errors
- [ ] Performance acceptable

### Monitoring
- [ ] Container resource usage checked: `docker stats`
- [ ] Memory usage acceptable
- [ ] CPU usage acceptable
- [ ] Disk space available
- [ ] Network connectivity stable

### Maintenance
- [ ] Set up log rotation (if needed)
- [ ] Configure automated restarts
- [ ] Plan backup strategy
- [ ] Set up monitoring/alerting (optional)
- [ ] Document admin procedures

## Scaling & Optimization

### Container Optimization
- [ ] Memory limits set
- [ ] CPU limits set (if using orchestration)
- [ ] Resource requests configured
- [ ] Auto-restart policy enabled

### Application Optimization
- [ ] Connection pooling configured
- [ ] Query timeouts set
- [ ] Caching enabled (if applicable)
- [ ] Logging level appropriate (not too verbose)

### Infrastructure
- [ ] Load balancer configured (if multiple instances)
- [ ] SSL/TLS termination configured
- [ ] CDN configured (if applicable)
- [ ] Database backups automated

## Troubleshooting Readiness

- [ ] Access to container logs
- [ ] SSH access to server
- [ ] Database access for queries
- [ ] API testing tools available
- [ ] Troubleshooting guide reviewed

## Final Checklist

- [ ] All environment variables set correctly
- [ ] No hardcoded secrets in code
- [ ] Application deployed successfully
- [ ] All API endpoints working
- [ ] Database connection stable
- [ ] Gemini API integration functional
- [ ] Health checks passing
- [ ] Logs monitoring in place
- [ ] Rollback plan documented
- [ ] Team notified of deployment
- [ ] Status page updated (if applicable)
- [ ] Documentation updated

## Rollback Plan

If deployment fails or issues arise:

1. Check logs: `docker-compose logs -f`
2. Stop containers: `docker-compose down`
3. Revert to previous version: `git checkout <previous-commit>`
4. Rebuild: `docker-compose up --build`
5. Contact support team if needed

## After First Week

- [ ] Monitor application performance
- [ ] Check for any errors in logs
- [ ] Review API usage patterns
- [ ] Confirm backups working
- [ ] Gather user feedback
- [ ] Plan optimizations if needed

---

**Deployment Date**: _______________  
**Deployed By**: _______________  
**Environment**: Production / Staging / Development  
**Notes**: _______________________________________________
