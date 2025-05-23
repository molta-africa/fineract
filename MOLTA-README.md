# MOLTA Backend - README

## 🔖 Overview

This project is built on top of **Apache Fineract**. The goal of this repository is to extend and customize core Fineract functionality (e.g., loan repayment notification, SMS alerts, scheduling) to meet MOLTA-specific requirements.

---

## 🚀 Current Setup

* **Base Tag:** We are currently working off the **`tags/1.11.0`** release of Apache Fineract
* **Stable Branch:** All work is branched from and merged back into the shared `stable` branch created from the `1.11.0` tag
* This `stable` branch represents the *current integration/staging branch* for all team contributions

---

## 🌱 Branching Strategy

### 1. `stable` Branch

* Created from Apache Fineract `tags/1.11.0`
* Shared integration branch for all team members
* Any updates to Fineract (e.g., newer tags like `1.11.1`, `1.12.0`) will be merged here periodically

### 2. Feature Branches

Each developer creates a branch from `stable` using the following naming pattern:

```
feat/MOLTA-<ticket-number>-<short-description>
```

#### Examples:

```bash
git checkout -b feat/MOLTA-101-repayment-notification stable
git checkout -b feat/MOLTA-205-sms-alert-service stable
```

### 3. Pull Requests

* Once work is complete, create a PR **into `stable`**
* All PRs require review and approval before merging
* Must pass all tests and quality checks
* Include unit tests for business logic changes

---

## 🔁 Keeping Up With Apache Fineract Releases

We periodically monitor new releases from Apache Fineract to stay current with improvements and security updates.

### Update Process

When a new Apache Fineract tag becomes available (e.g., `1.11.1`, `1.12.0`):

1. **Fetch the new tag:**
   ```bash
   git fetch upstream
   git checkout tags/<new-version> -b temp-upstream-<new-version>
   ```

2. **Create merge branch:**
   ```bash
   git checkout stable
   git checkout -b merge/upstream-<new-version>
   ```

3. **Merge and resolve conflicts:**
   ```bash
   git merge temp-upstream-<new-version>
   # Resolve any conflicts manually, preserving MOLTA customizations
   ```

4. **Test thoroughly:**
   - Run all unit tests
   - Test custom MOLTA features
   - Verify database migrations work correctly
   - Test SMS service integrations

5. **Create PR into `stable`** with detailed documentation of:
   - New upstream features added
   - Any breaking changes
   - MOLTA customizations that were preserved/updated

6. **Notify team** of new features and any required changes to their work

---

## 📂 Project Structure

```
.
📁 fineract-provider/
📁 ├── src/main/java/org/apache/fineract/
📁 │   ├── infrastructure/
📁 │   │   ├── sms/service_molta/          # MOLTA SMS customizations
📁 │   │   └── notification/               # Custom notification services
📁 │   ├── portfolio/
📁 │   │   ├── loanproduct/               # Loan product customizations
📁 │   │   └── loanaccount/               # Loan account customizations
📁 │   └── accounting/                     # Accounting customizations
📁 ├── src/main/resources/
📁 │   ├── sql/migrations/                # Database migration scripts
📁 │   └── application.properties         # Configuration files
📁 └── src/test/java/                     # Unit and integration tests
📁 docker/                                # Docker configurations
📁 config/                                # Environment configurations
└── MOLTA-README.md
```

---

## ✅ Contribution Workflow

### For All Contributors:

1. **Start from stable:**
   ```bash
   git checkout stable
   git pull origin stable
   ```

2. **Create feature branch:**
   ```bash
   git checkout -b feat/MOLTA-<ticket>-<description>
   ```

3. **Make your changes** following coding standards

4. **Commit with clear messages:**
   ```bash
   git commit -m "MOLTA-<ticket>: Add repayment notification feature
   
   - Implement SMS notification service
   - Add database migration for notification settings
   - Include unit tests for notification logic"
   ```

5. **Push and create PR:**
   ```bash
   git push origin feat/MOLTA-<ticket>-<description>
   ```

6. **Open PR to `stable`** with:
   - Clear description of changes
   - Link to MOLTA ticket
   - Database migration notes (if applicable)
   - API endpoint documentation (if applicable)

### ✅ Contribution Checklist

- [ ] Branch created from latest `stable`
- [ ] Feature branch follows naming convention: `feat/MOLTA-<ticket>-<description>`
- [ ] Code follows Java coding standards and Fineract conventions
- [ ] Unit tests added for new business logic
- [ ] Integration tests updated (if APIs changed)
- [ ] Database migrations included (if schema changes)
- [ ] Configuration updated (if new properties needed)
- [ ] PR description includes API documentation
- [ ] Reviewer(s) assigned
- [ ] All CI/CD checks pass

---

## 🧪 Testing Strategy

### Unit Tests
- Business logic in service classes
- Data validation and transformation
- Custom notification services
- SMS service integrations

### Integration Tests
- API endpoints for MOLTA features
- Database operations and migrations
- External service integrations (SMS providers)
- Loan lifecycle with custom notifications

### Testing Commands
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "SMSNotificationServiceTest"

# Run integration tests
./gradlew integrationTest
```

---

## 🚀 Development Environment

### Prerequisites
- Java 17+
- MySQL 8.0+
- Gradle 7.6+
- Docker (optional)

### Local Setup
```bash
# Clone repository
git clone <repo-url>
cd molta-backend

# Setup database
mysql -u root -p < fineract-provider/src/main/resources/sql/migrations/core_db/V1__Base_setup.sql

# Build project
./gradlew build

# Run application
./gradlew bootRun
```

### Environment Configuration
- **Development:** `application-dev.properties`
- **Staging:** `application-staging.properties`
- **Production:** `application-prod.properties`

---

## 🔧 MOLTA Custom Features

### SMS Notification Service
- **Location:** `infrastructure/sms/service_molta/`
- **Purpose:** Custom SMS alerts for loan events
- **Configuration:** SMS provider settings in application properties

### Loan Repayment Notifications
- **Location:** `portfolio/loanaccount/service/`
- **Purpose:** Automated reminders and confirmations
- **Features:** Configurable timing, multiple notification channels

### Custom Scheduling
- **Location:** `infrastructure/jobs/service/`
- **Purpose:** MOLTA-specific batch jobs and scheduling
- **Features:** Custom cron expressions, job monitoring

---

## 📋 API Documentation

### Custom Endpoints

```
POST /api/v1/molta/sms/send
GET  /api/v1/molta/notifications/{loanId}
PUT  /api/v1/molta/notifications/settings
```

### Authentication
All MOLTA custom endpoints follow Fineract's authentication model using:
- Basic Authentication (Development)
- OAuth2 (Production)

---

## 🐛 Troubleshooting

### Common Issues

1. **Database Migration Fails**
   ```bash
   # Check migration status
   ./gradlew flywayInfo
   
   # Repair if needed
   ./gradlew flywayRepair
   ```

2. **SMS Service Not Working**
   - Check SMS provider configuration in application.properties
   - Verify network connectivity to SMS gateway
   - Check logs for authentication errors

3. **Build Failures**
   ```bash
   # Clean and rebuild
   ./gradlew clean build --refresh-dependencies
   ```

---

## 📞 Support & Communication

### Questions or Issues?
- **Technical Issues:** Create GitHub issue with `backend` label
- **Feature Requests:** Follow internal ticketing system (MOLTA-XXX)
- **Code Reviews:** Tag appropriate backend team members
- **Urgent Issues:** Contact backend team leads

### Backend Team Contacts
- **Lead Developer:** [Name/Contact]
- **Senior Developers:** [Names/Contacts]
- **DevOps:** [Name/Contact] for deployment issues

---

## 📋 Quick Reference

### Useful Commands

```bash
# Development workflow
git checkout stable && git pull origin stable
git checkout -b feat/MOLTA-XXX-feature-name
./gradlew bootRun

# Testing
./gradlew test
./gradlew integrationTest

# Database
./gradlew flywayMigrate
./gradlew flywayInfo

# Build and deploy
./gradlew build
./gradlew bootJar
```

### Important Links
- [Apache Fineract Documentation](https://fineract.apache.org/)
- [Apache Fineract GitHub](https://github.com/apache/fineract)
- [MOLTA API Documentation](link-to-api-docs)
- [Development Guidelines](link-to-dev-guidelines)

---

*Last Updated: [Current Date] | Version: 1.0 | Base: Apache Fineract 1.11.0*