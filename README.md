# 🚀 Job Search Platform with AI Resume Tailor

A full-stack job search platform that scrapes 23,000+ real jobs daily and uses AI to tailor your resume for each job, maximizing your ATS score.

[![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=bhavishyayarapathineni_job-search-app&metric=alert_status)](https://sonarcloud.io/project/overview?id=bhavishyayarapathineni_job-search-app)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=bhavishyayarapathineni_job-search-app&metric=security_rating)](https://sonarcloud.io/project/overview?id=bhavishyayarapathineni_job-search-app)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=bhavishyayarapathineni_job-search-app&metric=sqale_rating)](https://sonarcloud.io/project/overview?id=bhavishyayarapathineni_job-search-app)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=bhavishyayarapathineni_job-search-app&metric=reliability_rating)](https://sonarcloud.io/project/overview?id=bhavishyayarapathineni_job-search-app)

## Features

- 23,000+ Real Jobs scraped daily from Adzuna API across 15 job categories
- AI Resume Tailor rewrites your resume to match each job description using OpenRouter AI
- ATS Score Analysis shows keyword match percentage before and after tailoring
- PDF Download of tailored resume
- Save Jobs to track interesting opportunities
- Profile Management with resume upload and skills management
- JWT Authentication for secure login and registration
- Email Notifications when new matching jobs are posted

## Tech Stack

### Backend
| Technology | Purpose |
|------------|---------|
| Java 17 | Core language |
| Spring Boot 3.2 | REST API framework |
| Spring Security + JWT | Authentication |
| Apache Kafka | Real-time job streaming |
| PostgreSQL | Primary database |
| Redis | Caching layer |
| PDFBox | Resume text extraction |
| Docker + Docker Compose | Containerization |

### Frontend
| Technology | Purpose |
|------------|---------|
| React 18 | UI framework |
| TypeScript | Type safety |
| Axios | HTTP client |

### DevOps & Quality
| Technology | Purpose |
|------------|---------|
| SonarQube Cloud | Code quality |
| JaCoCo | Code coverage 88% |
| JUnit 5 + Mockito | Unit testing 92 tests |
| GitHub Actions | CI/CD pipeline |

## Architecture
React 18 Frontend (TypeScript)
|
| REST API (JWT Auth)
|
Spring Boot Backend

AuthService
JobService
AIResumeService
ProfileService
JobScraperService
EmailService
|
|
|     |     |
PostgreSQL  Kafka  Redis
(Database) (Stream) (Cache)
|
OpenRouter AI
(Resume Tailor)
## Quick Start

### Prerequisites
- Java 17
- Node.js 18+
- Docker Desktop
- Maven 3.8+

### Setup

1. Clone the repository
```bash
git clone https://github.com/bhavishyayarapathineni/job-search-app.git
cd job-search-app
```

2. Start infrastructure
```bash
docker-compose up -d
```

3. Add API keys to backend/src/main/resources/application.properties
openrouter.api.key=your_key_here
adzuna.app.id=your_id
adzuna.app.key=your_key

4. Start Backend
```bash
cd backend
mvn spring-boot:run -DskipTests
```

5. Start Frontend
```bash
cd frontend
npm install
npm start
```

6. Open http://localhost:3000

## Testing

```bash
cd backend
mvn test
mvn test jacoco:report
open target/site/jacoco/index.html
```

Test Results:
- 92 unit tests
- 0 failures
- 88% code coverage
- Service layer 86%
- Controller layer 92%
- Security layer 96%

## Code Quality

Analyzed with SonarQube Cloud:
- Security Rating A
- Reliability Rating A
- Maintainability Rating A
- 0 Open Issues resolved 41 issues
- 0.7% Code Duplication

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login user |

### Jobs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/jobs | Get all jobs paginated |
| GET | /api/jobs/search | Search jobs by keyword |
| GET | /api/jobs/filter/type | Filter by job type |
| GET | /api/jobs/filter/experience | Filter by experience |

### Profile
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/profile | Get user profile |
| PUT | /api/profile | Update user profile |
| POST | /api/profile/jobs/{id}/save | Save a job |
| GET | /api/profile/jobs/saved | Get saved jobs |
| POST | /api/resume/upload | Upload resume file |

### AI Resume Tailor
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/ai/tailor-resume | Tailor resume for a job |

## Docker Services

- PostgreSQL 15 port 5432
- Redis 7 port 6379
- Zookeeper port 2181
- Kafka port 9092

## Author

Bhavishya Yarapathineni
- bhavishya123yarapathineni@gmail.com
- LinkedIn https://www.linkedin.com/in/bhavi-chowdary-748569403/
- GitHub https://github.com/bhavishyayarapathineni
- Portfolio https://bhavishyayarapathineni.github.io

## License

MIT License
