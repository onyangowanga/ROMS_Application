# ROMS Deployment Guide - Render.com (FREE)

## 🎯 Overview
Deploy your ROMS application (Spring Boot + React + PostgreSQL) to Render.com completely **FREE** for your pitch.

---

## 📋 Prerequisites

1. **GitHub Account** - Your code must be on GitHub
2. **Render Account** - Sign up at https://render.com (free)
3. **Your ROMS application** - Current codebase

---

## 🚀 Deployment Steps

### Step 1: Prepare Your Code

#### 1.1 Update Frontend API URL

Edit `frontend/src/api/axios.ts`:

```typescript
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
```

This allows the frontend to use environment variables for the backend URL.

#### 1.2 Add .gitignore entries

Ensure your `.gitignore` includes:
```
node_modules/
frontend/node_modules/
frontend/dist/
target/
*.log
.env
```

---

### Step 2: Push to GitHub

```bash
# If not already a git repo
git init
git add .
git commit -m "Prepare for Render deployment"

# Create a new GitHub repository and push
git remote add origin https://github.com/YOUR_USERNAME/roms.git
git branch -M main
git push -u origin main
```

---

### Step 3: Deploy PostgreSQL Database

1. **Go to Render Dashboard** → https://dashboard.render.com
2. Click **"New +"** → Select **"PostgreSQL"**
3. Configure:
   - **Name**: `roms-postgres`
   - **Database**: `roms_db`
   - **User**: `postgres`
   - **Region**: Choose closest to your location
   - **Plan**: **Free**
4. Click **"Create Database"**
5. **IMPORTANT**: Copy the **Internal Database URL** (starts with `postgresql://`)

---

### Step 4: Deploy Spring Boot Backend

1. Click **"New +"** → Select **"Web Service"**
2. Connect your GitHub repository
3. Configure:
   - **Name**: `roms-backend`
   - **Region**: Same as database
   - **Branch**: `main`
   - **Root Directory**: Leave blank (or `.` if needed)
   - **Runtime**: **Docker**
   - **Plan**: **Free**

4. **Environment Variables** (Click "Advanced"):
   ```
   SPRING_DATASOURCE_URL = [Paste Internal Database URL from Step 3]
   SPRING_DATASOURCE_USERNAME = postgres
   SPRING_DATASOURCE_PASSWORD = [From database credentials]
   SPRING_JPA_HIBERNATE_DDL_AUTO = update
   SPRING_JPA_SHOW_SQL = false
   PORT = 8080
   ```

5. **Health Check Path**: `/actuator/health`
6. Click **"Create Web Service"**
7. Wait 5-10 minutes for build to complete
8. **Copy the backend URL** (e.g., `https://roms-backend.onrender.com`)

---

### Step 5: Build & Deploy React Frontend

**Option A: Deploy as Static Site on Render**

1. Click **"New +"** → Select **"Static Site"**
2. Connect your GitHub repository
3. Configure:
   - **Name**: `roms-frontend`
   - **Branch**: `main`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `dist`

4. **Environment Variables**:
   ```
   VITE_API_URL = https://roms-backend.onrender.com
   ```

5. Click **"Create Static Site"**
6. Your frontend will be live at `https://roms-frontend.onrender.com`

**Option B: Serve Frontend from Spring Boot (Simpler)**

Build the frontend locally and copy to Spring Boot:

```bash
# In frontend directory
npm install
npm run build

# Copy dist folder to Spring Boot static resources
# This is already handled if you have frontend in src/main/resources/static
```

Update `pom.xml` to include frontend build in Maven:

```xml
<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <version>1.12.1</version>
    <executions>
        <execution>
            <id>install node and npm</id>
            <goals>
                <goal>install-node-and-npm</goal>
            </goals>
            <configuration>
                <nodeVersion>v18.17.0</nodeVersion>
            </configuration>
        </execution>
        <execution>
            <id>npm install</id>
            <goals>
                <goal>npm</goal>
            </goals>
            <configuration>
                <arguments>install</arguments>
                <workingDirectory>frontend</workingDirectory>
            </configuration>
        </execution>
        <execution>
            <id>npm build</id>
            <goals>
                <goal>npm</goal>
            </goals>
            <configuration>
                <arguments>run build</arguments>
                <workingDirectory>frontend</workingDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
<plugin>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-frontend</id>
            <phase>process-resources</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <outputDirectory>${basedir}/target/classes/static</outputDirectory>
                <resources>
                    <resource>
                        <directory>frontend/dist</directory>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Then just deploy the backend - it will serve the frontend too!

---

## 🎨 Access Your Application

After deployment:
- **Backend API**: `https://roms-backend.onrender.com`
- **Frontend**: `https://roms-frontend.onrender.com` (Option A) or same as backend (Option B)

---

## ⚠️ Important Notes

### Free Tier Limitations:
- **Sleep after 15 min inactivity** - First request after sleep takes ~30 seconds
- **750 hours/month** - Enough for demos/pitching
- **Database**: 90-day data retention on free tier

### For Your Pitch:
1. **Wake up services before demo** - Visit URLs 1-2 minutes before presenting
2. **Keep a tab open** - Prevents sleep during presentation
3. **Have backup screenshots** - In case of connectivity issues

---

## 🔧 Troubleshooting

### Backend won't start:
- Check environment variables are correct
- Verify database URL is the **Internal URL**
- Check build logs in Render dashboard

### Frontend can't reach backend:
- Ensure `VITE_API_URL` points to backend
- Check CORS configuration in Spring Boot
- Verify backend is running (check health endpoint)

### Database connection errors:
- Use **Internal Database URL** (not External)
- Check database is in same region as backend
- Verify credentials are correct

---

## 🎯 Quick Start (Combined Deployment)

If you want the **SIMPLEST** deployment:

1. **Deploy PostgreSQL** (Step 3)
2. **Deploy Backend ONLY** with frontend built-in (Step 4 + Option B)
3. **Access everything** from backend URL

This gives you a single URL for your pitch: `https://roms-backend.onrender.com`

---

## 📊 What to Show Your Client

Your live demo will have:
- ✅ Public URL (looks professional)
- ✅ Real database (data persists)
- ✅ Full workflow (all features working)
- ✅ HTTPS (secure)
- ✅ No cost (completely free)

**Perfect for pitching!** 🚀

---

## 🔄 Updates After Deployment

Any changes you push to GitHub will automatically trigger redeployment:

```bash
git add .
git commit -m "Update feature"
git push origin main
```

Render will rebuild and redeploy automatically!

---

## 💡 Pro Tips

1. **Before pitch**: Visit your site 2-3 minutes early to wake it up
2. **Demo account**: Pre-create test accounts for your client to use
3. **Sample data**: Add realistic sample data for demo
4. **Screenshots**: Have backup screenshots in case of issues

---

Need help? Check Render docs: https://render.com/docs
