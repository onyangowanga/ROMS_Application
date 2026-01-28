# Google Cloud Storage Setup Guide

This guide will help you configure Google Drive/Cloud Storage for document uploads in ROMS.

## Prerequisites
- A Google account
- Access to Google Cloud Console

## Step 1: Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click "Select a project" → "New Project"
3. Enter project name: `ROMS-Document-Storage`
4. Click "Create"

## Step 2: Enable Google Drive API

1. In your project, go to "APIs & Services" → "Library"
2. Search for "Google Drive API"
3. Click on it and click "Enable"

## Step 3: Create Service Account Credentials

1. Go to "APIs & Services" → "Credentials"
2. Click "Create Credentials" → "Service Account"
3. Fill in the details:
   - **Service account name**: `roms-drive-uploader`
   - **Service account ID**: (auto-generated)
   - **Description**: `Service account for ROMS document uploads`
4. Click "Create and Continue"
5. Grant role: "Editor" (or create custom role with Drive permissions)
6. Click "Continue" → "Done"

## Step 4: Download Credentials JSON

1. Click on the service account you just created
2. Go to "Keys" tab
3. Click "Add Key" → "Create new key"
4. Select "JSON" format
5. Click "Create"
6. Save the downloaded JSON file as `credentials.json` in your project root:
   ```
   C:\Programing\Realtime projects\ROMS\Roms\Roms\credentials.json
   ```

## Step 5: Create Google Drive Folder (Optional)

1. Go to [Google Drive](https://drive.google.com/)
2. Create a new folder called "ROMS-Documents"
3. Right-click the folder → "Share"
4. Add the service account email (found in credentials.json: `client_email`)
5. Give it "Editor" permission
6. Copy the folder ID from the URL:
   - URL format: `https://drive.google.com/drive/folders/FOLDER_ID_HERE`
7. Save this folder ID for the next step

## Step 6: Configure Application

### Option A: Environment Variables (Recommended for Production)

Set these environment variables:
```bash
GOOGLE_CREDENTIALS_PATH=C:\Programing\Realtime projects\ROMS\Roms\Roms\credentials.json
GOOGLE_DRIVE_FOLDER_ID=your-folder-id-from-step-5
```

### Option B: Update application.yaml (For Testing)

Edit `src/main/resources/application.yaml`:

```yaml
google:
  drive:
    credentials-file-path: credentials.json
    folder-id: your-folder-id-from-step-5  # Or leave empty for root
```

## Step 7: Switch to Google Cloud Storage

Edit `src/main/resources/application.yaml`:

```yaml
roms:
  storage:
    mode: cloud  # Change from 'local' to 'cloud'
```

## Step 8: Test the Configuration

1. Restart your Spring Boot application
2. Check the logs for: `"Google Drive Service initialized successfully"`
3. Try uploading a document through the application
4. Verify the file appears in your Google Drive folder

## Switch Back to Local Storage

To revert to local file storage for development:

Edit `src/main/resources/application.yaml`:

```yaml
roms:
  storage:
    mode: local  # Change from 'cloud' to 'local'
```

Restart the application.

## Troubleshooting

### "Credentials file not found"
- Ensure `credentials.json` is in the correct location
- Check the path in `application.yaml` or environment variable

### "Access denied" errors
- Verify the service account has permission to the folder
- Check that Google Drive API is enabled in your project

### Files upload but don't appear in folder
- If no folder ID is specified, files go to the service account's root drive
- Share the folder with the service account email

## Security Notes

⚠️ **Important**: 
- Never commit `credentials.json` to version control
- Add it to `.gitignore`
- For production, use environment variables or secret management services
- Rotate credentials periodically

## Cost Considerations

- Google Drive API is free for most use cases
- Check [Google Drive API Quotas](https://developers.google.com/drive/api/guides/limits)
- Monitor usage in Google Cloud Console

---

**Need Help?** Check the application logs for detailed error messages.
