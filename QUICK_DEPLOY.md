# 🚀 Quick Deploy to Render (5 Minutes)

## What You Need
- GitHub account
- Render.com account (free signup)

---

## Step-by-Step (Easiest Way)

### 1️⃣ Push to GitHub (2 minutes)

```bash
# In your project root
git init
git add .
git commit -m "Ready for deployment"

# Create repo on GitHub, then:
git remote add origin https://github.com/YOUR_USERNAME/roms.git
git branch -M main
git push -u origin main
```

---

### 2️⃣ Deploy Database (1 minute)

1. Go to https://dashboard.render.com
2. Click **New +** → **PostgreSQL**
3. Settings:
   - Name: `roms-db`
   - Database: `roms_db`
   - User: `postgres`
   - Region: Choose nearest
   - **Plan: FREE**
4. Click **Create Database**
5. **Copy the "Internal Database URL"** (you'll need it next)

---

### 3️⃣ Deploy Backend (2 minutes)

1. Click **New +** → **Web Service**
2. Connect your GitHub → Select your `roms` repository
3. Settings:
   - Name: `roms-backend`
   - Region: Same as database
   - Branch: `main`
   - Runtime: **Docker**
   - **Plan: FREE**
4. **Environment Variables** → Advanced:
   ```
   SPRING_DATASOURCE_URL = [paste Internal Database URL from step 2]
   SPRING_DATASOURCE_USERNAME = postgres
   SPRING_DATASOURCE_PASSWORD = [from database page]
   SPRING_JPA_HIBERNATE_DDL_AUTO = update
   PORT = 8080
   ```
5. Click **Create Web Service**
6. **Wait 5-10 minutes** for build
7. **Copy your backend URL** (e.g., `https://roms-backend-xyz.onrender.com`)

---

### 4️⃣ Deploy Frontend (1 minute)

1. Click **New +** → **Static Site**
2. Connect GitHub → Select `roms` repo
3. Settings:
   - Name: `roms-frontend`
   - Branch: `main`
   - Root Directory: `frontend`
   - Build Command: `npm install && npm run build`
   - Publish Directory: `dist`
   - **Plan: FREE**
4. **Environment Variables**:
   ```
   VITE_API_URL = https://roms-backend-xyz.onrender.com
   ```
   (Use the URL from Step 3)
5. Click **Create Static Site**

---

## ✅ Done!

Your app will be live at:
- **Frontend**: `https://roms-frontend.onrender.com`
- **Backend**: `https://roms-backend-xyz.onrender.com`

---

## 🎯 Before Your Pitch

1. **Wake up the services** (free tier sleeps after 15 min):
   - Visit your frontend URL 2 minutes before pitch
   - It takes ~30 seconds to wake up

2. **Create demo accounts**:
   - Super Admin
   - A test applicant
   - A test employer

3. **Add sample data**:
   - A few job orders
   - Sample candidates in different workflow stages

---

## 🔧 If Something Goes Wrong

### Backend won't start?
- Check environment variables in Render dashboard
- Make sure you used **Internal Database URL** (not External)
- Check logs in Render dashboard → Your Service → Logs

### Frontend can't connect to backend?
- Verify `VITE_API_URL` matches your backend URL exactly
- Check backend is running (visit `/actuator/health`)
- Look for CORS errors in browser console

### First request is slow?
- Normal! Free tier sleeps. Just wait 30 seconds.

---

## 💡 Pro Tips

- **Auto-deploy**: Any push to `main` branch auto-deploys
- **Custom domain**: Can add your own domain for free
- **Logs**: Check Render dashboard for all logs
- **Database**: Free tier keeps data for 90 days

---

## 📞 Support

Need help?
- Render Docs: https://render.com/docs
- Render Community: https://community.render.com

---

**That's it! Your ROMS app is live and ready for pitching!** 🎉
