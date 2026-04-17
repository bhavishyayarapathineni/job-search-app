# 🚀 Job Search Platform with AI Resume Tailor

A full-stack job search platform that scrapes 23,000+ real jobs daily and uses AI to tailor your resume for each job, maximizing your ATS score.

[![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=bhavishyayarapathineni_job-search-app&metric=alert_status)](https://sonarcloud.io/project/overview?id=bhavishyayarapathineni_job-search-app)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=bhavishyayarapathineni_job-search-app&metric=security_rating)](https://sonarcloud.io/project/overview?id=bhavishyayarapathineni_job-search-app)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=bhavishyayarapathineni_job-search-app&metric=sqale_rating)](https://sonarcloud.io/project/overview?id=bhavishyayarapathineni_job-search-app)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=bhavishyayarapathineni_job-search-app&metric=reliability_rating)](https://sonarcloud.io/project/overview?id=bhavishyayarapathineni_job-search-app)

## ✨ Features

- 🔍 **23,000+ Real Jobs** — Scraped daily from Adzuna API across 15 job categories
- 🤖 **AI Resume Tailor** — Rewrites your resume to match each job description using OpenRouter AI
- 📊 **ATS Score Analysis** — Shows keyword match % before and after tailoring
- 📄 **PDF Download** — Download your tailored resume as a formatted PDF
- ❤️ **Save Jobs** — Save and track interesting jobs
- 👤 **Profile Management** — Upload resume, manage skills, set job preferences
- 🔐 **JWT Authentication** — Secure login and registration
- 📧 **Email Notifications** — Get alerted when new matching jobs are posted

## 🛠 Tech Stack

### Backend
| Technology | Purpose |
|------------|---------|
| Java 17 | Core language |
| Spring Boot 3.2 | REST API framework |
| Spring Security + JWT | Authentication & authorization |
| Apache Kafka | Real-time job event streaming |
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
| SonarQube Cloud | Code quality analysis |
| JaCoCo | Code coverage (88%) |
| JUnit 5 + Mockito | Unit testing (92 tests) |
| GitHub Actions | CI/CD pipeline |

## 🏗 Architecture
┌─────────────────────────────────────────────────────────┐
│                   React 18 Frontend                      │
│              (TypeScript + Axios)                        │
└──────────────────────┬──────────────────────────────────┘
│ REST API (JWT Auth)
┌──────────────────────▼──────────────────────────────────┐
│                 Spring Boot Backend                       │
│                                                          │
│  ┌─────────────┐  ┌────────────┐  ┌─────────────────┐  │
│  │ AuthService │  │ JobService │  │ AIResumeService │  │
│  └─────────────┘  └────────────┘  └─────────────────┘  │
│  ┌─────────────┐  ┌────────────┐  ┌─────────────────┐  │
│  │ProfileService│ │JobScraper  │  │ EmailService    │  │
│  └─────────────┘  └─────┬──────┘  └─────────────────┘  │
└────────────────────────┬┼─────────────────────────────── ┘
││
┌───────────────┘│
│                │
┌────────▼───────┐  ┌─────▼──────┐  ┌─────────────┐
│  PostgreSQL 15 │  │   Kafka    │  │   Redis 7   │
│  (Primary DB)  │  │(Streaming) │  │  (Cache)    │
└────────────────┘  └─────┬──────┘  └─────────────┘
│
┌───────────▼──────────┐
│   OpenRouter AI API  │
│  (Resume Tailoring)  │
└──────────────────────┘
## 🚀 Quick Start

### Prerequisites
- Java 17
- Node.js 18+
- Docker Desktop
- Maven 3.8+

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/bhavishyayarapathineni/job-search-app.git
cd job-search-app
```

2. **Start infrastructure (PostgreSQL, Redis, Kafka)**
```bash
docker-compose up -d
```

3. **Configure application properties**
```bash
cd backend/src/main/resources
# Edit application.properties and add your API keys:
# openrouter.api.key=your_key_here
# adzuna.app.id=your_id
# adzuna.app.key=your_key
```

4. **Start Backend**
```bash
cd backend
mvn spring-boot:run -DskipTests
```

5. **Start Frontend**
```bash
cd frontend
npm install
npm start
```

6. **Open browser**
## 🧪 Testing

```bash
cd backend

# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

**Test Results:**
- ✅ 92 unit tests
- ✅ 0 failures
- ✅ 88% code coverage
- ✅ Service layer: 86%
- ✅ Controller layer: 92%
- ✅ Security layer: 96%

## 📊 Code Quality

Analyzed with SonarQube Cloud:
- 🔒 **Security Rating: A**
- 🐛 **Reliability Rating: A**
- 🔧 **Maintainability Rating: A**
- 📋 **0 Open Issues** (resolved 41 issues)
- 📦 **0.7% Code Duplication**

## 🔌 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login user |

### Jobs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/jobs` | Get all jobs (paginated) |
| GET | `/api/jobs/search` | Search jobs by keyword |
| GET | `/api/jobs/filter/type` | Filter by job type |
| GET | `/api/jobs/filter/experience` | Filter by experience level |

### Profile
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/profile` | Get user profile |
| PUT | `/api/profile` | Update user profile |
| POST | `/api/profile/jobs/{id}/save` | Save a job |
| GET | `/api/profile/jobs/saved` | Get saved jobs |
| POST | `/api/resume/upload` | Upload resume file |

### AI Resume Tailor
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/ai/tailor-resume` | Tailor resume for a job |

## 🐳 Docker Compose Services

```yaml
Services:
  - PostgreSQL 15    (port 5432)
  - Redis 7          (port 6379)
  - Zookeeper        (port 2181)
  - Kafka            (port 9092)
```

## 👨‍💻 Author

**Bhavishya Yarapathineni**
- 📧 bhavishya123yarapathineni@gmail.com
- 💼 [LinkedIn](https://www.linkedin.com/in/bhavi-chowdary-748569403/)
- 🐙 [GitHub](https://github.com/bhavishyayarapathineni)
- 🌐 [Portfolio](https://bhavishyayarapathineni.github.io)

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
