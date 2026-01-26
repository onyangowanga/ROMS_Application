# 📋 ROMS Deployment Checklist

## Pre-Deployment Checklist

Use this checklist before deploying ROMS to production.

---

## ✅ Phase 1: Development Setup

### Environment Setup
- [ ] Java 17+ installed and configured
- [ ] Maven 3.6+ installed
- [ ] PostgreSQL 15+ installed and running
- [ ] IDE configured (IntelliJ IDEA / Eclipse / VS Code)
- [ ] Git repository initialized
- [ ] Docker installed (optional but recommended)

### Project Build
- [ ] `mvn clean install` completes successfully
- [ ] No compilation errors
- [ ] All dependencies resolved
- [ ] Application starts without errors
- [ ] Can access http://localhost:8080

---

## ✅ Phase 2: Database Configuration

### PostgreSQL Setup
- [ ] Database `roms_db` created
- [ ] User `roms_user` created with correct password
- [ ] Permissions granted to user
- [ ] Connection tested successfully
- [ ] Database schema auto-created (check tables exist)
- [ ] Audit tables (`*_AUD`) created
- [ ] Views created successfully
- [ ] Functions created successfully

### Database Verification
```sql
-- Run these queries to verify setup
SELECT tablename FROM pg_tables WHERE schemaname = 'public';
SELECT * FROM pg_views WHERE schemaname = 'public';
SELECT routine_name FROM information_schema.routines WHERE routine_schema = 'public';
```

- [ ] Main tables exist (users, candidates, job_orders, etc.)
- [ ] Audit tables exist (*_aud tables)
- [ ] Views exist (v_active_candidates, etc.)
- [ ] Functions exist (get_candidate_balance, etc.)

---

## ✅ Phase 3: Security Configuration

### JWT Configuration
- [ ] JWT secret is at least 32 characters
- [ ] JWT secret is stored securely (environment variable)
- [ ] JWT secret is different from default
- [ ] Token expiration time configured (default: 24 hours)
- [ ] Refresh token expiration configured (default: 7 days)

### Password Security
- [ ] BCrypt password encoder configured
- [ ] Minimum password length enforced (6 characters)
- [ ] Default admin password changed

### HTTPS/SSL (Production)
- [ ] SSL certificate obtained
- [ ] SSL configured in application
- [ ] HTTP to HTTPS redirect enabled
- [ ] Certificate auto-renewal configured

---

## ✅ Phase 4: Application Configuration

### application.yaml
- [ ] Database URL updated
- [ ] Database credentials updated
- [ ] JWT secret updated
- [ ] AWS credentials configured (if using S3)
- [ ] Logging level set appropriately
- [ ] Server port configured
- [ ] Profile selected (dev/staging/prod)

### Environment Variables
- [ ] `.env` file created from `.env.example`
- [ ] All sensitive data moved to environment variables
- [ ] Environment variables loaded correctly
- [ ] No hardcoded secrets in code

---

## ✅ Phase 5: API Testing

### Authentication Tests
- [ ] Can register new user
- [ ] Can login with correct credentials
- [ ] Cannot login with wrong credentials
- [ ] JWT token received on successful login
- [ ] Token contains correct role information
- [ ] Refresh token works
- [ ] Token expires after configured time

### Candidate Management Tests
- [ ] Can create candidate with SUPER_ADMIN role
- [ ] Can create candidate with OPERATIONS_STAFF role
- [ ] Cannot create candidate with APPLICANT role
- [ ] Can retrieve candidate list
- [ ] Can retrieve single candidate
- [ ] Can update candidate details
- [ ] Can soft delete candidate
- [ ] Cannot access without authentication

### Workflow Tests
- [ ] Candidate starts in APPLIED status
- [ ] Can transition: APPLIED → DOCS_SUBMITTED
- [ ] Can transition: DOCS_SUBMITTED → INTERVIEWED
- [ ] Can transition: INTERVIEWED → MEDICAL_PASSED (with valid passport)
- [ ] Cannot transition to MEDICAL_PASSED with expired passport
- [ ] Can transition: MEDICAL_PASSED → OFFER_ISSUED (with medical status PASSED)
- [ ] Cannot transition to OFFER_ISSUED without medical clearance
- [ ] Can transition: OFFER_ISSUED → OFFER_ACCEPTED
- [ ] Can transition: OFFER_ACCEPTED → PLACED (with job order capacity)
- [ ] Cannot transition to PLACED if job order is filled
- [ ] Can check transition validity with can-transition endpoint

### RBAC Tests
- [ ] SUPER_ADMIN can access all endpoints
- [ ] FINANCE_MANAGER can access payment endpoints
- [ ] FINANCE_MANAGER cannot manage users
- [ ] OPERATIONS_STAFF can manage candidates
- [ ] OPERATIONS_STAFF cannot access payments
- [ ] EMPLOYER can only see own job orders
- [ ] APPLICANT can only see own profile
- [ ] Unauthorized requests return 401
- [ ] Forbidden requests return 403

---

## ✅ Phase 6: Data Integrity

### Soft Delete
- [ ] Deleted candidates have `deleted_at` timestamp
- [ ] Deleted candidates have `is_active = false`
- [ ] Can re-register same passport after deletion
- [ ] Queries only return active records by default
- [ ] Audit trail preserved for deleted records

### Financial Immutability
- [ ] Payment records cannot be deleted
- [ ] Payment records cannot be updated
- [ ] Reversal pattern works correctly
- [ ] Reversed transactions link to original
- [ ] Balance calculations exclude reversals
- [ ] All monetary values use BigDecimal

### Audit Trail
- [ ] All entity changes logged to *_AUD tables
- [ ] created_by populated automatically
- [ ] created_at populated automatically
- [ ] last_modified_by updated on changes
- [ ] last_modified_at updated on changes
- [ ] Can query audit history

---

## ✅ Phase 7: Performance & Optimization

### Database Indexes
- [ ] Unique index on passport_no (where deleted_at IS NULL)
- [ ] Index on candidate status
- [ ] Index on job_order_ref
- [ ] Index on payment candidate_id
- [ ] Index on payment date
- [ ] All foreign keys indexed

### Query Performance
- [ ] No N+1 query issues
- [ ] Lazy loading configured correctly
- [ ] Eager loading used where appropriate
- [ ] Connection pool configured
- [ ] Query timeout configured

### Caching (Optional)
- [ ] Spring Cache configured
- [ ] Redis/Memcached setup (if needed)
- [ ] Cache eviction strategy defined

---

## ✅ Phase 8: Error Handling & Logging

### Exception Handling
- [ ] Global exception handler configured
- [ ] All exceptions return proper HTTP status codes
- [ ] Error messages are user-friendly
- [ ] Sensitive data not leaked in errors
- [ ] Validation errors return field details

### Logging
- [ ] Application logs to file
- [ ] Log rotation configured
- [ ] Log level appropriate for environment
- [ ] SQL queries logged in dev (disabled in prod)
- [ ] Sensitive data not logged
- [ ] Request/response logging configured

---

## ✅ Phase 9: Documentation

### Technical Documentation
- [ ] README.md complete and up-to-date
- [ ] QUICKSTART.md with step-by-step guide
- [ ] ARCHITECTURE.md with diagrams
- [ ] API endpoints documented
- [ ] Database schema documented
- [ ] Environment variables documented

### API Documentation (Phase 2)
- [ ] Swagger/OpenAPI configured
- [ ] API endpoints documented
- [ ] Request/response examples provided
- [ ] Authentication documented
- [ ] Error codes documented

---

## ✅ Phase 10: Production Readiness

### Infrastructure
- [ ] Production database setup
- [ ] Database backups configured
- [ ] Backup restoration tested
- [ ] Disaster recovery plan documented
- [ ] Monitoring configured (Prometheus/Grafana)
- [ ] Health check endpoint available
- [ ] Load balancer configured (if needed)

### Security Hardening
- [ ] SQL injection prevention verified
- [ ] XSS protection enabled
- [ ] CSRF protection configured
- [ ] Rate limiting configured
- [ ] Input validation on all endpoints
- [ ] File upload restrictions (size, type)
- [ ] Dependency vulnerabilities checked (`mvn dependency-check`)

### Performance
- [ ] Load testing completed
- [ ] Response time acceptable (< 200ms for most endpoints)
- [ ] Concurrent user testing done
- [ ] Memory leaks checked
- [ ] Database connection pool tuned
- [ ] JVM parameters optimized

### Compliance
- [ ] GDPR compliance verified (if applicable)
- [ ] Data retention policy configured
- [ ] Personal data encrypted
- [ ] Audit requirements met
- [ ] User consent mechanisms in place

---

## ✅ Phase 11: Deployment

### Pre-Deployment
- [ ] All tests passing
- [ ] Code reviewed
- [ ] Database migration scripts ready
- [ ] Rollback plan prepared
- [ ] Deployment window scheduled
- [ ] Stakeholders notified

### Deployment Steps
- [ ] Application built for production (`mvn clean package -Pprod`)
- [ ] JAR file uploaded to server
- [ ] Environment variables configured on server
- [ ] Database migrations executed
- [ ] Application started successfully
- [ ] Health check passes
- [ ] Smoke tests completed

### Post-Deployment
- [ ] Application accessible
- [ ] All endpoints responding
- [ ] No errors in logs
- [ ] Database connections stable
- [ ] Performance metrics normal
- [ ] Backup job running
- [ ] Monitoring alerts configured
- [ ] Documentation updated
- [ ] Deployment notes recorded

---

## ✅ Phase 12: User Acceptance

### Initial Setup
- [ ] Super admin account created
- [ ] Finance manager account created
- [ ] Operations staff accounts created
- [ ] Test employer accounts created
- [ ] Test applicant accounts created

### User Training
- [ ] Admin user training completed
- [ ] Finance staff training completed
- [ ] Operations staff training completed
- [ ] User manuals distributed
- [ ] Support contact information provided

### Acceptance Tests
- [ ] End-to-end workflow tested
- [ ] Real data imported (if migrating)
- [ ] Reports verified
- [ ] Performance acceptable
- [ ] UAT sign-off received

---

## 🚨 Critical Pre-Production Checklist

**DO NOT deploy to production without these:**

- [ ] JWT secret changed from default
- [ ] Database passwords are strong and unique
- [ ] HTTPS/SSL configured
- [ ] CORS configured correctly
- [ ] Database backups automated
- [ ] Monitoring and alerting configured
- [ ] Error logging to external service
- [ ] Health checks configured
- [ ] Rollback plan documented
- [ ] On-call support arranged

---

## 📊 Success Metrics

After deployment, verify:

- [ ] Zero authentication bypasses
- [ ] Zero unauthorized data access
- [ ] 100% audit trail coverage
- [ ] All workflow transitions enforced
- [ ] No data integrity violations
- [ ] Response time < 200ms (95th percentile)
- [ ] Uptime > 99.9%
- [ ] Zero data loss

---

## 🔄 Ongoing Maintenance

Weekly:
- [ ] Review error logs
- [ ] Check database size
- [ ] Verify backups
- [ ] Review security alerts

Monthly:
- [ ] Dependency updates
- [ ] Security patches
- [ ] Performance review
- [ ] Capacity planning
- [ ] User feedback review

Quarterly:
- [ ] Security audit
- [ ] Load testing
- [ ] Disaster recovery drill
- [ ] Documentation review

---

## 📞 Support Contacts

Before going live, ensure you have:

- [ ] Database administrator contact
- [ ] Infrastructure team contact
- [ ] Security team contact
- [ ] On-call developer contact
- [ ] Escalation procedures documented

---

## ✨ Phase 2 Preparation

If ready for Phase 2 development:

- [ ] Phase 1 features fully tested
- [ ] Phase 1 deployed to production
- [ ] User feedback collected
- [ ] Phase 2 requirements finalized
- [ ] S3 bucket created and configured
- [ ] Email/SMS provider selected
- [ ] M-Pesa credentials obtained (if applicable)

---

**Remember:** This is an enterprise system handling sensitive personal and financial data. Take your time with each checklist item. Better to be thorough than to deploy with issues.

**Good luck with your deployment! 🚀**

---

Last Updated: January 2026  
Version: 1.1  
Status: Phase 1 Complete
