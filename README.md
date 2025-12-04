# QueryX - AI-Powered SQL Query Generator

An intelligent SQL query generator and executor that uses Google Gemini AI to convert natural language into SQL queries. Built with Spring Boot, PostgreSQL, and integrated with Google's Generative AI API.

## Features

- **AI-Powered Query Generation**: Convert natural language to SQL using Gemini AI
- **Query Execution**: Execute both human-written and AI-generated SQL queries
- **Schema Management**: Create, read, update, and delete database tables
- **Data Operations**: Insert data into tables efficiently
- **RESTful API**: Complete REST API for all operations
- **Docker Ready**: Pre-configured for containerization and cloud deployment
- **Comprehensive Logging**: Debug-friendly logging for troubleshooting
- **CORS Enabled**: Ready for frontend integration

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL database
- Google Gemini API key

### Local Development

1. **Clone the repository**:
```bash
git clone https://github.com/your-username/queryx.git
cd queryx
```

2. **Set up environment variables** (create `.env` file):
```bash
cp .env.example .env
# Edit .env with your actual values
```

3. **Run the application**:
```bash
mvn spring-boot:run
```

The server will start at `http://localhost:8080`

4. **Test the API**:
```bash
curl -X GET "http://localhost:8080/schema/tables"
```

## Docker Deployment

### Using Docker Compose (Recommended)

```bash
# Build and start
docker-compose up --build

# View logs
docker-compose logs -f

# Stop
docker-compose down
```

### Using Docker CLI

```bash
# Build image
docker build -t queryx:latest .

# Run container
docker run -d -p 8080:8080 \
  -e GEMINI_API_KEY="your-api-key" \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://..." \
  queryx:latest

# View logs
docker logs -f <container-id>
```

### Push to Registry

```bash
# Docker Hub
docker tag queryx:latest your-username/queryx:latest
docker push your-username/queryx:latest

# GitHub Container Registry
docker tag queryx:latest ghcr.io/your-username/queryx:latest
docker push ghcr.io/your-username/queryx:latest
```

## API Documentation

### Query APIs

**Execute Human SQL Query**
```bash
POST /query/execute?query=SELECT * FROM users LIMIT 10
```

**Execute AI-Enhanced Query**
```bash
POST /query/ai-query
Content-Type: application/json

{
  "naturalLanguageQuery": "Show all users with gmail email addresses",
  "tableNames": ["users"],
  "queryType": "ai-enhanced"
}
```

### Schema APIs

```bash
GET  /schema/tables              # List all tables
GET  /schema/tablesSchema        # Get schema for all tables
GET  /schema/{tableName}         # Get schema for specific table
POST /schema/create              # Create table
POST /schema/update              # Update table
DELETE /schema/delete/{tableName} # Delete table
```

### Data APIs

```bash
POST /tabledata/insert           # Insert data into table
```

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete API reference with examples.

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `GEMINI_API_KEY` | Google Gemini API key | (required) |
| `PORT` | Server port | 8080 |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | localhost |
| `SPRING_DATASOURCE_USERNAME` | Database username | (required) |
| `SPRING_DATASOURCE_PASSWORD` | Database password | (required) |
| `LOGGING_LEVEL_COM_EXAMPLE_DEMO` | Application logging level | DEBUG |

See [.env.example](.env.example) for all configuration options.

## Project Structure

```
queryx/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── entity/          # Database entities
│   │   │   ├── services/        # Business logic
│   │   │   └── DemoApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/                    # Unit tests
├── Dockerfile                   # Docker build configuration
├── docker-compose.yml           # Docker Compose configuration
├── pom.xml                      # Maven dependencies
├── API_DOCUMENTATION.md         # Complete API reference
├── DOCKER_DEPLOYMENT.md         # Deployment guide
└── README.md                    # This file
```

## Getting Gemini API Key

1. Go to [Google AI Studio](https://ai.google.dev/)
2. Click "Create API Key"
3. Copy the generated key
4. Set it in your `.env` file as `GEMINI_API_KEY`

## Database Setup

The application connects to PostgreSQL. Ensure your database is running and accessible with the credentials specified in environment variables.

### Example PostgreSQL Connection

```
URL: jdbc:postgresql://localhost:5432/queryx
Username: postgres
Password: your_password
```

## Troubleshooting

### Network Error
- Check if Gemini API key is set correctly
- Verify internet connection
- Check logs: `docker logs queryx-app`

### Database Connection Error
- Verify PostgreSQL is running
- Check database credentials in `.env`
- Ensure database URL is correct and accessible

### Container Issues
- Check Docker logs: `docker logs -f queryx-app`
- Verify environment variables are set
- Ensure port 8080 is not in use

See [NETWORK_ERROR_FIX.md](NETWORK_ERROR_FIX.md) for detailed debugging guide.

## Development

### Build

```bash
mvn clean package
```

### Run Tests

```bash
mvn test
```

### Run with Hot Reload

```bash
mvn spring-boot:run
```

### Format Code

```bash
mvn spotless:apply
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Support

For issues and questions:
- Open an issue on GitHub
- Check the [API Documentation](API_DOCUMENTATION.md)
- Review the [Docker Deployment Guide](DOCKER_DEPLOYMENT.md)

## Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- Uses [Google Gemini AI](https://ai.google.dev/)
- PostgreSQL database via [Neon](https://neon.tech/)