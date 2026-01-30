# Cloudinary Setup for ROMS

This guide explains how to set up Cloudinary for document storage in production.

## Why Cloudinary?

- **Free tier**: 25GB storage, 25GB bandwidth/month
- **Persistent storage**: Files don't disappear on restart
- **Global CDN**: Fast file delivery worldwide
- **Simple setup**: Just environment variables, no credentials file needed

## Setup Steps

### 1. Create Cloudinary Account

1. Go to https://cloudinary.com/users/register_free
2. Sign up for a free account
3. Verify your email

### 2. Get Your Credentials

After logging in:
1. Go to your **Dashboard** (https://console.cloudinary.com/)
2. You'll see your credentials:
   - **Cloud Name**: e.g., `dxxx12345`
   - **API Key**: e.g., `123456789012345`
   - **API Secret**: e.g., `abcdefghijklmnopqrstuvwxyz123`

### 3. Configure Render Backend

Go to your Render backend service → **Environment** → Add these variables:

```
ROMS_STORAGE_MODE=cloud
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

**Example:**
```
ROMS_STORAGE_MODE=cloud
CLOUDINARY_CLOUD_NAME=dxxx12345
CLOUDINARY_API_KEY=123456789012345
CLOUDINARY_API_SECRET=abcdefghijklmnopqrstuvwxyz123
```

### 4. Save and Redeploy

Click **"Save Changes"** and Render will automatically redeploy your backend with Cloudinary enabled.

## How It Works

- **Upload**: Files are uploaded directly to Cloudinary and organized by candidate ID
- **Storage**: Files are stored in folders like `roms/candidate_123/`
- **Download**: Users get redirected to Cloudinary's secure CDN URLs
- **Delete**: Files are removed from Cloudinary when deleted from the app

## File Organization

Files are organized in Cloudinary with this structure:
```
roms/documents/
  └── candidate_1/
      ├── 20260130_123456_passport.pdf
      ├── 20260130_123500_cv.pdf
      └── 20260130_123530_certificate.pdf
```

## Testing

After deployment:
1. Upload a document through your app
2. Check your Cloudinary dashboard → **Media Library**
3. You should see the file in the `roms/documents` folder

## Troubleshooting

**Error: "Failed to upload file to Cloudinary"**
- Check that all 3 environment variables are set correctly
- Verify your API credentials in Cloudinary dashboard
- Check Render logs for detailed error messages

**Files not appearing:**
- Check `ROMS_STORAGE_MODE=cloud` is set
- Verify Cloudinary credentials are correct
- Check application logs for upload errors

## Free Tier Limits

- **Storage**: 25GB
- **Bandwidth**: 25GB/month
- **Transformations**: 25,000/month

For a recruitment agency demo, this should be more than sufficient.

## Local Development

For local development, you can either:

**Option 1: Keep using local storage** (default)
- Files stored in `uploads/` folder
- No configuration needed

**Option 2: Use Cloudinary locally**
- Set environment variables in your IDE or `.env` file:
  ```
  ROMS_STORAGE_MODE=cloud
  CLOUDINARY_CLOUD_NAME=your-cloud-name
  CLOUDINARY_API_KEY=your-api-key
  CLOUDINARY_API_SECRET=your-api-secret
  ```

## Need Help?

- Cloudinary Documentation: https://cloudinary.com/documentation
- Cloudinary Support: https://support.cloudinary.com/
