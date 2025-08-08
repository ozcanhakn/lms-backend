# LMS Backend - Learning Management System

**Aday:** Münür Hakan Özcan  
**Teslim Tarihi:** 08.08.2025

A comprehensive Learning Management System backend built with Spring Boot, featuring advanced RBAC (Role-Based Access Control), comprehensive audit logging, and robust security features.

## 🚀 Features

### Core Features
- **User Management**: Complete user lifecycle management with profile types (Super Admin, Teacher, Student)
- **Organization Management**: Multi-tenant support with organization and brand hierarchy
- **Classroom Management**: Classroom creation and assignment management
- **Course Management**: Course creation and assignment to classrooms
- **JWT Authentication**: Secure token-based authentication with refresh tokens

### 🔐 Advanced Security Features

#### Role-Based Access Control (RBAC)
- **Flexible Permission System**: Granular permissions with resource-action pairs
- **Role Management**: Create, update, and manage roles with specific permissions
- **User Role Assignment**: Assign multiple roles to users with audit trail
- **Permission Checking**: Real-time permission validation for API endpoints
- **Backward Compatibility**: Maintains legacy role system while adding advanced RBAC

#### Login Attempt Throttling (Rate Limiting)
- **Email-based Rate Limiting**: Configurable limits per email address
- **IP-based Rate Limiting**: Additional protection against brute force attacks
- **Bucket4j Integration**: Efficient token bucket algorithm implementation
- **Configurable Windows**: Adjustable time windows for rate limiting
- **Attempt Tracking**: Comprehensive logging of all login attempts

#### Audit Logging System
- **Comprehensive Activity Tracking**: Log all user actions with detailed context
- **AOP-based Logging**: Automatic method-level audit logging with custom annotations
- **Rich Metadata**: IP addresses, user agents, timestamps, and operation details
- **Flexible Queries**: Search and filter audit logs by user, resource, action, or time range
- **Performance Monitoring**: Track method execution times and success/failure rates

### 📊 Testing & Quality

#### Unit Test Coverage (>80%)
- **Comprehensive Service Tests**: Full coverage of business logic
- **Repository Layer Testing**: Database operation testing with TestContainers
- **Security Testing**: Authentication and authorization test coverage
- **Mock-based Testing**: Efficient unit testing with Mockito
- **Integration Tests**: End-to-end testing with real database

### 🛠 Technical Stack

- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 3.5.4**: Latest stable version with Spring Security 6
- **Spring Security**: Advanced security with JWT and RBAC
- **Spring Data JPA**: Database abstraction with Hibernate
- **PostgreSQL**: Primary database with UUID primary keys
- **Redis**: Caching and session management
- **Docker**: Containerization support
- **Maven**: Build and dependency management
- **Lombok**: Reduced boilerplate code
- **Swagger/OpenAPI**: API documentation

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+
- Docker (optional)

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd lms-backend
```

### 2. Database Setup
```sql
-- Create database
CREATE DATABASE lms_backend;

-- Create user (optional)
CREATE USER lms_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE lms_backend TO lms_user;
```

### 3. Configuration
Create `application.yml` in `src/main/resources/`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lms_backend
    username: lms_user
    password: your_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  redis:
    host: localhost
    port: 6379
    password: # if required

  security:
    jwt:
      secret: your-super-secret-jwt-key-here-make-it-long-and-secure
      expiration: 86400000 # 24 hours
      refresh-expiration: 604800000 # 7 days

lms:
  rate-limit:
    login:
      max-attempts: 5
      window-minutes: 15
    ip:
      max-attempts: 10
      window-minutes: 15

logging:
  level:
    com.lms: DEBUG
    org.springframework.security: DEBUG
```

### 4. Run the Application
```bash
# Using Maven
mvn spring-boot:run

# Using Docker
docker-compose up -d
```

### 5. Access the Application
- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

### 6. Example Users

#### SuperAdmin User
```json
{
  "email": "admin@lms.com",
  "password": "Admin123!",
  "firstName": "Super",
  "lastName": "Admin",
  "profileId": 0
}
```

#### Teacher User
```json
{
  "email": "teacher@lms.com",
  "password": "Teacher123!",
  "firstName": "John",
  "lastName": "Doe",
  "profileId": 1,
  "organizationId": "your-organization-uuid"
}
```

#### Student User
```json
{
  "email": "student@lms.com",
  "password": "Student123!",
  "firstName": "Jane",
  "lastName": "Smith",
  "profileId": 2,
  "organizationId": "your-organization-uuid",
  "classroomId": "your-classroom-uuid"
}
```

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh-token` - Refresh JWT token
- `POST /api/v1/auth/register` - User registration (optional)
- `POST /api/v1/auth/logout` - User logout (optional)

### SuperAdmin Endpoints
- `POST /api/v1/brands` - Create new brand
- `GET /api/v1/brands` - Get all brands
- `PUT /api/v1/brands/{id}` - Update brand
- `DELETE /api/v1/brands/{id}` - Delete brand
- `POST /api/v1/organizations` - Create new organization
- `GET /api/v1/organizations` - Get all organizations
- `POST /api/v1/users` - Create new user (profile_id: 1 or 2)
- `GET /api/v1/users` - Get all users
- `POST /api/v1/classrooms` - Create new classroom
- `POST /api/v1/courses` - Create new course
- `POST /api/v1/courses/assign` - Assign course to classroom
- `POST /api/v1/teachers/assign-classroom` - Assign teacher to classroom

### Teacher Endpoints
- `GET /api/v1/teachers/my-classes` - Get teacher's assigned classrooms
- `GET /api/v1/teachers/my-students` - Get students in teacher's classrooms
- `GET /api/v1/teachers/my-courses` - Get courses in teacher's classrooms

### Student Endpoints
- `GET /api/v1/students/my-courses` - Get student's assigned courses

### RBAC Management (Advanced Features)
- `GET /api/rbac/roles` - Get all roles
- `POST /api/rbac/roles` - Create new role
- `PUT /api/rbac/roles/{id}` - Update role
- `DELETE /api/rbac/roles/{id}` - Delete role
- `GET /api/rbac/permissions` - Get all permissions
- `POST /api/rbac/permissions` - Create new permission
- `POST /api/rbac/users/{userId}/roles` - Assign role to user
- `DELETE /api/rbac/users/{userId}/roles/{roleId}` - Remove role from user

### Audit Logs
- `GET /api/audit/users/{userId}` - Get user activity
- `GET /api/audit/resources/{type}/{id}` - Get resource activity
- `GET /api/audit/actions/{action}` - Get activity by action
- `GET /api/audit/timerange` - Get activity by time range

### Rate Limiting
- `GET /api/rate-limit/attempts/email/{email}` - Get login attempts by email
- `GET /api/rate-limit/attempts/ip/{ip}` - Get login attempts by IP
- `POST /api/rate-limit/clear` - Clear rate limit buckets

## 🔧 Development

### Running Tests
```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AuditServiceTest

# Run integration tests
mvn test -Dtest=*IntegrationTest
```

### Code Quality
```bash
# Check code style
mvn checkstyle:check

# Run SonarQube analysis
mvn sonar:sonar
```

### Database Migrations
The application uses Hibernate's `ddl-auto: update` for automatic schema management. For production, consider using Flyway or Liquibase for proper migration management.

## 🏗 Project Structure

```
src/
├── main/
│   ├── java/com/lms/
│   │   ├── annotation/          # Custom annotations
│   │   ├── aspect/             # AOP aspects for audit logging
│   │   ├── config/             # Configuration classes
│   │   ├── controller/         # REST controllers
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── entity/            # JPA entities
│   │   ├── exception/         # Custom exceptions
│   │   ├── repository/        # Data access layer
│   │   ├── security/          # Security configuration
│   │   └── service/           # Business logic
│   └── resources/
│       ├── application.yml    # Application configuration
│       └── db/               # Database scripts
└── test/
    └── java/com/lms/
        ├── config/            # Test configuration
        └── service/           # Unit tests
```

## 🔐 Security Features

### JWT Authentication
- Secure token-based authentication
- Configurable token expiration
- Refresh token mechanism
- Token blacklisting support

### RBAC Implementation
- **Permissions**: Granular resource-action permissions
- **Roles**: Collections of permissions
- **User Roles**: Many-to-many relationship between users and roles
- **Authorization**: Real-time permission checking

### Rate Limiting
- **Email-based**: Limits login attempts per email
- **IP-based**: Additional protection against attacks
- **Configurable**: Adjustable limits and time windows
- **Monitoring**: Comprehensive attempt tracking

### Audit Logging
- **Automatic Logging**: AOP-based method interception
- **Rich Context**: IP, user agent, timestamps, operation details
- **Performance Tracking**: Method execution times
- **Flexible Queries**: Multiple search and filter options

## 📊 Monitoring & Observability

### Health Checks
- Database connectivity
- Redis connectivity
- Application status

### Metrics
- Request/response times
- Error rates
- Rate limiting statistics
- Audit log volumes

### Logging
- Structured logging with JSON format
- Audit trail for all operations
- Security event logging
- Performance monitoring

## 🚀 Deployment

### Docker Deployment
```bash
# Build image
docker build -t lms-backend .

# Run container
docker run -p 8080:8080 lms-backend
```

### Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f lms-backend
```

### Production Considerations
- Use external PostgreSQL and Redis instances
- Configure proper JWT secrets
- Set up monitoring and alerting
- Implement proper backup strategies
- Use HTTPS in production
- Configure rate limiting appropriately

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation

## 🔄 Version History

### v1.0.0
- Initial release with core LMS functionality
- JWT authentication
- Basic user and organization management
- Classroom and course management

### v1.1.0
- Advanced RBAC system
- Comprehensive audit logging
- Login attempt throttling
- Enhanced security features
- 80%+ unit test coverage
- Postman collection

---

**Note**: This is a production-ready LMS backend with enterprise-grade security features. Make sure to review and customize the configuration according to your specific requirements.
#   l m s - b a c k e n d  
 