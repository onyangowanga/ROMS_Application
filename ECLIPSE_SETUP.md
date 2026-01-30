# Eclipse Setup for ROMS Project (FREE Alternative to IntelliJ)

## Step 1: Install Lombok in Eclipse

### Download Lombok
1. Go to https://projectlombok.org/download
2. Download `lombok.jar`
3. Save it somewhere (e.g., Downloads folder)

### Install Lombok into Eclipse
1. Double-click `lombok.jar` (or run: `java -jar lombok.jar`)
2. Lombok installer will open
3. Click "Specify location..." and browse to your Eclipse installation
   - Usually: `C:\Program Files\Eclipse\eclipse.exe` or `C:\Users\YourName\eclipse\eclipse.exe`
4. Click "Install/Update"
5. Click "Quit Installer"
6. **Restart Eclipse**

## Step 2: Import ROMS Project

1. Open Eclipse
2. **File → Import**
3. Select **Maven → Existing Maven Projects**
4. Click **Next**
5. Browse to: `C:\Programing\Realtime projects\ROMS\Roms\Roms`
6. Make sure `pom.xml` is checked
7. Click **Finish**
8. Wait for Maven to download dependencies (check bottom-right progress)

## Step 3: Configure Java 17

1. Right-click project → **Properties**
2. Go to **Java Build Path → Libraries**
3. If not Java 17:
   - Remove old JRE
   - Click **Add Library → JRE System Library**
   - Select **Alternate JRE** → Choose **jdk-17**
   - Click **Finish**
4. Go to **Java Compiler**
   - Set **Compiler compliance level: 17**
5. Click **Apply and Close**

## Step 4: Enable Annotation Processing

1. Right-click project → **Properties**
2. Go to **Java Compiler → Annotation Processing**
3. Check ✅ **Enable annotation processing**
4. Check ✅ **Enable processing in editor**
5. Click **Apply and Close**

## Step 5: Clean and Build

1. **Project → Clean**
2. Select **ROMS project**
3. Check **Build Automatically**
4. Click **Clean**

## Step 6: Run Application

1. Find `RomsApplication.java` in:
   ```
   src/main/java → com.roms → RomsApplication.java
   ```
2. Right-click → **Run As → Java Application**

## Expected Result
- ✅ No Lombok errors
- ✅ Application starts on http://localhost:8080
- ✅ Console shows Spring Boot banner

## Troubleshooting

### If Lombok still not working:
1. Verify Lombok is installed:
   - Check `eclipse.ini` file (in Eclipse installation folder)
   - Should have line: `-javaagent:lombok.jar`
2. Restart Eclipse
3. Project → Clean → Build

### If compilation errors:
1. Right-click project → **Maven → Update Project**
2. Check **Force Update of Snapshots/Releases**
3. Click **OK**

## Eclipse is FREE Forever!
Unlike IntelliJ trial, Eclipse is completely free and works great with Lombok once properly configured.

## Bonus: AI Coding Assistants for Eclipse

Eclipse supports AI assistants similar to GitHub Copilot in VS Code:

### Option 1: GitHub Copilot (Recommended if you have subscription)
1. **Help → Eclipse Marketplace**
2. Search for **"GitHub Copilot"**
3. Click **Install**
4. Restart Eclipse
5. Sign in with your GitHub account

### Option 2: Codeium (FREE Alternative)
1. **Help → Eclipse Marketplace**
2. Search for **"Codeium"**
3. Click **Install**
4. Restart Eclipse
5. Sign up for free account

### Option 3: Tabnine (Free/Paid)
1. **Help → Eclipse Marketplace**
2. Search for **"Tabnine"**
3. Click **Install**
4. Choose free or pro plan

**You'll still have AI assistance in Eclipse!** The AI will help with:
- ✅ Code completion
- ✅ Function suggestions
- ✅ Bug fixes
- ✅ Code explanations
- ✅ Unit test generation
