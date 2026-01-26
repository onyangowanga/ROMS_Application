# AWS S3 to Google Drive Migration Guide

## Overview

This document describes the changes made to migrate from AWS S3 to Google Drive for document storage in the ROMS application.

## Why Google Drive?

- **Free Tier**: Google Drive offers 15GB free storage per account
- **Cost Effective**: No charges for API usage within reasonable limits
- **Easy Setup**: Simple service account authentication
- **Sharing Capabilities**: Built-in shareable link generation
- **Reliability**: Google's infrastructure ensures high availability

---

## Changes Made

### 1. Package Structure Refactoring

**Changed:** `com.example.Roms` → `com.roms`

**Reason:** Remove "example" from production code for cleaner, professional package naming.

**Files Updated:**
- All 35 Java source files in `src/main/java/com/roms/`
- Test files in `src/test/java/com/roms/`
- `application.yaml` logging configuration
- `pom.xml` groupId

### 2. Dependency Changes

**Removed AWS S3 Dependencies:**
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.26</version>
</dependency>
```

**Added Google Drive Dependencies:**
```xml
<!-- Google Drive API -->
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>com.google.apis</groupId>
    <artifactId>google-api-services-drive</artifactId>
    <version>v3-rev20230822-2.0.0</version>
</dependency>
<dependency>
    <groupId>com.google.auth</groupId>
    <artifactId>google-auth-library-oauth2-http</artifactId>
    <version>1.19.0</version>
</dependency>
```

### 3. Configuration Changes

**application.yaml - Before (AWS S3):**
```yaml
aws:
  s3:
    bucket-name: ${AWS_S3_BUCKET:roms-documents}
    region: ${AWS_REGION:us-east-1}
    presigned-url-duration: 300
  access-key: ${AWS_ACCESS_KEY:}
  secret-key: ${AWS_SECRET_KEY:}
```

**application.yaml - After (Google Drive):**
```yaml
google:
  drive:
    application-name: ROMS-Document-Management
    credentials-file-path: ${GOOGLE_CREDENTIALS_PATH:credentials.json}
    folder-id: ${GOOGLE_DRIVE_FOLDER_ID:}
    shared-link-duration: 300
```

### 4. Entity Changes

**CandidateDocument Entity:**

**Before:**
```java
@Column(name = "s3_key", nullable = false, unique = true, length = 500)
private String s3Key;
```

**After:**
```java
@Column(name = "drive_file_id", nullable = false, unique = true, length = 500)
private String driveFileId;
```

### 5. Service Implementation

**New Service Created:** `GoogleDriveService.java`

**Key Methods:**
- `uploadFile(MultipartFile file, Long candidateId)` - Upload file to Google Drive
- `downloadFile(String fileId)` - Download file from Google Drive
- `deleteFile(String fileId)` - Delete file from Google Drive
- `generateShareableLink(String fileId)` - Generate shareable link
- `getFileMetadata(String fileId)` - Get file information
- `fileExists(String fileId)` - Check file existence

---

## Migration Steps for Existing Data

If you have existing data in AWS S3, follow these steps:

### 1. Export S3 Metadata

```sql
-- Export existing document metadata
SELECT 
    id,
    candidate_id,
    doc_type,
    file_name,
    s3_key,
    file_size,
    content_type
FROM candidate_documents
WHERE deleted_at IS NULL;
```

### 2. Download Files from S3

Use AWS CLI to download all files:
```bash
aws s3 sync s3://roms-documents ./s3-backup/
```

### 3. Upload to Google Drive

Create a migration script using the new `GoogleDriveService`:

```java
// Pseudo-code for migration
for (CandidateDocument doc : allDocuments) {
    // Download from local backup
    File localFile = new File("./s3-backup/" + doc.getS3Key());
    
    // Upload to Google Drive
    String driveFileId = googleDriveService.uploadFile(
        convertToMultipartFile(localFile), 
        doc.getCandidate().getId()
    );
    
    // Update database
    doc.setDriveFileId(driveFileId);
    candidateDocumentRepository.save(doc);
}
```

### 4. Update Database Schema

```sql
-- Rename column
ALTER TABLE candidate_documents 
RENAME COLUMN s3_key TO drive_file_id;

-- Update any existing references
-- (already handled by Hibernate/JPA with @Column annotation)
```

---

## Setup Instructions

### 1. Google Cloud Project Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create new project or select existing
3. Enable Google Drive API
4. Create Service Account credentials
5. Download JSON key file

### 2. Google Drive Folder Setup

1. Create folder "ROMS-Documents" in Google Drive
2. Share with service account email (from JSON)
3. Copy folder ID from URL

### 3. Application Configuration

**Option A: Using credentials.json file**
```bash
# Place credentials.json in project root
cp ~/Downloads/service-account-key.json ./credentials.json
```

**Option B: Using environment variables**
```bash
export GOOGLE_CREDENTIALS_PATH=/path/to/credentials.json
export GOOGLE_DRIVE_FOLDER_ID=your-folder-id-here
```

**Option C: Update application.yaml directly**
```yaml
google:
  drive:
    credentials-file-path: /absolute/path/to/credentials.json
    folder-id: 1ABCdefGHIjklMNOpqrSTUvwxYZ
```

---

## API Comparison

### AWS S3 vs Google Drive

| Operation | AWS S3 | Google Drive |
|-----------|--------|--------------|
| **Upload** | `s3Client.putObject()` | `driveService.files().create()` |
| **Download** | `s3Client.getObject()` | `driveService.files().get().executeMediaAsInputStream()` |
| **Delete** | `s3Client.deleteObject()` | `driveService.files().delete()` |
| **Share Link** | `s3Client.presignedGetObjectRequest()` | `driveService.permissions().create()` + `getWebViewLink()` |
| **List Files** | `s3Client.listObjectsV2()` | `driveService.files().list()` |

### Authentication

| AWS S3 | Google Drive |
|--------|--------------|
| Access Key + Secret Key | Service Account JSON |
| IAM Roles | Service Account Roles |
| Region-specific | Global |

---

## Cost Comparison

### AWS S3 (Estimated)

**Assumptions:** 1000 candidates, 5 documents each, 2MB per document

- **Storage:** 10GB × $0.023/GB = $0.23/month
- **Requests:** 5000 PUT + 10000 GET = ~$0.05/month
- **Data Transfer:** 20GB/month × $0.09/GB = $1.80/month
- **Total:** ~$2.08/month

### Google Drive (Free Tier)

- **Storage:** 15GB free per account
- **Requests:** Unlimited within quota (10,000/min/project)
- **Data Transfer:** Free
- **Total:** $0/month (within free tier)

**Savings:** ~$25/year for small-scale operations

---

## Performance Considerations

### Upload Speed
- **AWS S3:** Slightly faster (optimized for large files)
- **Google Drive:** Adequate for document uploads (< 10MB files)

### Download Speed
- **AWS S3:** Very fast with CloudFront CDN
- **Google Drive:** Good, but no CDN option

### Scalability
- **AWS S3:** Unlimited, designed for high throughput
- **Google Drive:** Limited by quotas (10,000 requests/min/project)

### Recommendation
- **Small-Medium projects (< 10,000 docs):** Google Drive (free)
- **Large enterprise (> 100,000 docs):** AWS S3 (better scalability)

---

## Security Considerations

### Google Drive Security Features

1. **Service Account Isolation:** Files owned by service account, not personal account
2. **Shared Link Permissions:** Anyone with link can view (reader role)
3. **Encryption:** Google encrypts data at rest and in transit
4. **Access Control:** Fine-grained IAM permissions

### Best Practices

1. **Don't commit credentials.json to Git:**
```bash
# Add to .gitignore
echo "credentials.json" >> .gitignore
```

2. **Use environment variables in production:**
```bash
export GOOGLE_CREDENTIALS_PATH=/secure/path/credentials.json
```

3. **Rotate service account keys periodically**

4. **Use separate service accounts for dev/staging/prod**

5. **Monitor API usage in Google Cloud Console**

---

## Troubleshooting

### Issue: "Credentials file not found"

**Solution:**
```bash
# Verify file path
ls -la credentials.json

# Check application.yaml configuration
cat src/main/resources/application.yaml | grep credentials-file-path
```

### Issue: "403 Forbidden - Insufficient Permission"

**Solution:**
1. Verify service account has access to folder
2. Check folder ID is correct
3. Ensure Drive API is enabled in Google Cloud Console

### Issue: "Folder not found"

**Solution:**
```bash
# Verify folder ID in Google Drive URL
# URL: https://drive.google.com/drive/folders/1ABC...XYZ
# Folder ID: 1ABC...XYZ

# Update application.yaml or environment variable
export GOOGLE_DRIVE_FOLDER_ID=correct-folder-id
```

### Issue: "Quota exceeded"

**Solution:**
- Google Drive API has limits: 10,000 requests per 100 seconds per project
- Implement exponential backoff retry logic
- Contact Google for quota increase if needed

---

## Testing Migration

### 1. Unit Test Google Drive Service

```bash
# Ensure credentials are configured
mvn test -Dtest=GoogleDriveServiceTest
```

### 2. Integration Test Upload/Download

```bash
# Test document upload
curl -X POST http://localhost:8080/api/candidates/1/documents \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test-document.pdf" \
  -F "docType=PASSPORT"

# Verify in Google Drive folder
# Check web UI: drive.google.com
```

### 3. Verify Database Records

```sql
SELECT id, file_name, drive_file_id, created_at 
FROM candidate_documents 
WHERE deleted_at IS NULL 
ORDER BY created_at DESC 
LIMIT 10;
```

---

## Rollback Plan

If issues arise, you can rollback to AWS S3:

### 1. Revert Code Changes

```bash
git log --oneline  # Find commit before migration
git revert <commit-hash>
```

### 2. Revert Database Schema

```sql
ALTER TABLE candidate_documents 
RENAME COLUMN drive_file_id TO s3_key;
```

### 3. Restore AWS Configuration

Revert `application.yaml` and `pom.xml` to previous versions.

### 4. Re-upload Files to S3

```bash
aws s3 sync ./s3-backup/ s3://roms-documents/
```

---

## Future Enhancements

1. **Hybrid Storage:** Use both S3 and Google Drive based on file size
2. **CDN Integration:** Use Cloudflare CDN for Google Drive links
3. **Automatic Cleanup:** Delete old files after retention period
4. **Virus Scanning:** Integrate antivirus scanning before upload
5. **Thumbnail Generation:** Auto-generate thumbnails for images
6. **Version Control:** Keep multiple versions of documents

---

## Conclusion

The migration from AWS S3 to Google Drive provides:
- ✅ Zero cost for storage (free tier)
- ✅ Simple authentication with service accounts
- ✅ Built-in sharing capabilities
- ✅ Suitable for small-medium scale operations

For large enterprise deployments with > 100,000 documents, consider AWS S3 for better scalability and performance.

---

## References

- [Google Drive API Documentation](https://developers.google.com/drive/api/v3/about-sdk)
- [Service Account Authentication](https://developers.google.com/identity/protocols/oauth2/service-account)
- [Google Drive Quotas](https://developers.google.com/drive/api/guides/limits)
- [AWS S3 Pricing](https://aws.amazon.com/s3/pricing/)

**Migration Date:** January 2024  
**ROMS Version:** 0.0.1-SNAPSHOT  
**Migrated By:** Development Team
