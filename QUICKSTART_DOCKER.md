# Quick Start - Docker Deployment

## 🚀 5-Minute Setup

### 1. Clone and Setup
```bash
git clone https://github.com/your-username/queryx.git
cd queryx
cp .env.example .env
```

### 2. Configure Environment
Edit `.env` with your values:
```bash
GEMINI_API_KEY=your_api_key_here
SPRING_DATASOURCE_URL=your_db_url
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password
```

### 3. Deploy with Docker Compose
```bash
docker-compose up --build
```

### 4. Test
```bash
curl http://localhost:8080/schema/tables
```

---

## 📦 Build Custom Image

```bash
# Build
docker build -t queryx:latest .

# Run
docker run -d -p 8080:8080 \
  -e GEMINI_API_KEY="your-key" \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://..." \
  -e SPRING_DATASOURCE_USERNAME="user" \
  -e SPRING_DATASOURCE_PASSWORD="pass" \
  queryx:latest

# View logs
docker logs -f <container-id>
```

---

## 🔑 Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GEMINI_API_KEY` | ✅ Yes | Get from https://ai.google.dev/ |
| `SPRING_DATASOURCE_URL` | ✅ Yes | PostgreSQL connection string |
| `SPRING_DATASOURCE_USERNAME` | ✅ Yes | Database username |
| `SPRING_DATASOURCE_PASSWORD` | ✅ Yes | Database password |
| `PORT` | ❌ No | Server port (default: 8080) |

---

## 📤 Push to Registry

### Docker Hub
```bash
docker tag queryx:latest your-username/queryx:latest
docker login
docker push your-username/queryx:latest
```

### GitHub Container Registry
```bash
docker tag queryx:latest ghcr.io/your-username/queryx:latest
docker login ghcr.io -u your-username
docker push ghcr.io/your-username/queryx:latest
```

---

## 🐛 Troubleshooting

### Container won't start?
```bash
docker-compose logs queryx-app
```

### Port 8080 in use?
```bash
docker-compose down
# or use different port:
PORT=8081 docker-compose up
```

### API not responding?
```bash
docker exec queryx-app curl http://localhost:8080/schema/tables
```

### Missing API key?
```bash
# Add to .env:
GEMINI_API_KEY=your_actual_key
docker-compose restart
```

---

## 📚 Documentation

- 📖 [Full API Docs](API_DOCUMENTATION.md)
- 🐳 [Detailed Docker Guide](DOCKER_DEPLOYMENT.md)
- ✅ [Deployment Checklist](DEPLOYMENT_CHECKLIST.md)
- 🔧 [Troubleshooting Guide](NETWORK_ERROR_FIX.md)

---

## 💡 Common Commands

```bash
# Start
docker-compose up -d

# Stop
docker-compose down

# View logs
docker-compose logs -f

# Rebuild
docker-compose up --build

# Remove images
docker-compose down --rmi all

# Shell access
docker exec -it queryx-app /bin/sh

# Check health
docker-compose ps
```

---

## 🎯 Next Steps

1. ✅ Configure `.env` with your credentials
2. ✅ Run `docker-compose up --build`
3. ✅ Test API at `http://localhost:8080/schema/tables`
4. ✅ Review [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
5. ✅ Push to your registry when ready

For more details, see [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)
