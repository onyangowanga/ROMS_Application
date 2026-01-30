# IntelliJ IDEA Setup for ROMS Project

## This will fix the Lombok issue

### 1. Install IntelliJ Lombok Plugin
```
File → Settings → Plugins → Search "Lombok" → Install → Restart
```

### 2. Enable Annotation Processing (CRITICAL)
```
File → Settings (Ctrl+Alt+S)
→ Build, Execution, Deployment
  → Compiler
    → Annotation Processors
      → ✅ Enable annotation processing
      → Processor path: Use classpath
      → ✅ Obtain processors from project classpath
→ Apply → OK
```

### 3. Set JDK 17
```
File → Project Structure (Ctrl+Alt+Shift+S)
→ Project
  → SDK: 17 (C:\Program Files\Java\jdk-17)
  → Language level: 17
→ OK
```

### 4. Import Maven Project
```
Right-click pom.xml → Maven → Reload Project
Wait for indexing to complete
```

### 5. Run Application
```
Find: src/main/java/com/roms/RomsApplication.java
Right-click → Run 'RomsApplication.main()'
```

## Expected Result
✅ Application starts on http://localhost:8080
✅ No Lombok errors
✅ All getters/setters/builders work

## Troubleshooting
If still errors:
1. File → Invalidate Caches → Invalidate and Restart
2. Delete .idea folder
3. Reimport project
