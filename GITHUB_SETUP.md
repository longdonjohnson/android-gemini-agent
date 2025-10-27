# GitHub Actions Build Setup

This guide shows how to automatically build your APK using GitHub Actions.

## 🚀 Quick Setup (5 minutes)

### Step 1: Create GitHub Repository

```bash
# In Termux, navigate to project
cd ~/android-gemini-agent

# Initialize git (if not already)
git init
git add .
git commit -m "Initial commit: Android Gemini Agent"

# Create repo on GitHub.com, then:
git remote add origin https://github.com/YOUR_USERNAME/android-gemini-agent.git
git branch -M main
git push -u origin main
```

### Step 2: Add Gemini API Key as Secret

1. Go to your repository on GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add:
   - **Name:** `GEMINI_API_KEY`
   - **Value:** `your_actual_gemini_api_key`
5. Click **Add secret**

### Step 3: Trigger Build

**Option A: Automatic (on push)**
```bash
# Any push to main/master will trigger build
git add .
git commit -m "Trigger build"
git push
```

**Option B: Manual**
1. Go to **Actions** tab on GitHub
2. Click **Build Android APK**
3. Click **Run workflow**
4. Select branch and click **Run workflow**

### Step 4: Download APK

1. Go to **Actions** tab
2. Click on the latest workflow run
3. Scroll to **Artifacts** section
4. Download **app-debug.apk**
5. Transfer to your Android device and install!

## 📋 Workflows Included

### 1. `build-apk.yml` (Debug Build)
- **Triggers:** Push to main/master, manual trigger
- **Builds:** Debug APK (for testing)
- **Output:** `app-debug.apk` in Artifacts
- **Retention:** 30 days

### 2. `build-release.yml` (Release Build)
- **Triggers:** Manual trigger, release creation
- **Builds:** Release APK (optimized)
- **Output:** `app-release.apk` in Artifacts
- **Retention:** 90 days

## 🔒 Security Best Practices

### Required Secret
- `GEMINI_API_KEY` - Your Gemini API key (REQUIRED)

### Optional Secrets (for signed releases)
- `KEYSTORE_FILE` - Base64 encoded keystore file
- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Key alias
- `KEY_PASSWORD` - Key password

## 📦 Adding Signing for Release Builds

If you want signed APKs (for Play Store), create a keystore:

```bash
# On your computer with Android SDK
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias

# Encode keystore to base64
base64 my-release-key.jks > keystore.txt
```

Add to GitHub Secrets:
- `KEYSTORE_FILE` = contents of keystore.txt
- `KEYSTORE_PASSWORD` = your keystore password
- `KEY_ALIAS` = your key alias
- `KEY_PASSWORD` = your key password

## 🎯 Using the Built APK

### Download from GitHub Actions

1. **Via Web:**
   - Actions → Select workflow run → Download artifact

2. **Via GitHub CLI:**
   ```bash
   gh run download --name app-debug
   ```

### Install on Android Device

**Method 1: ADB**
```bash
adb install app-debug.apk
```

**Method 2: Manual**
1. Transfer APK to phone
2. Open file manager
3. Tap APK
4. Enable "Install from Unknown Sources" if prompted
5. Install

## 🔄 Automatic Releases

### Create Tagged Release

```bash
# Tag a version
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin v1.0.0

# Create release on GitHub
# Go to Releases → Create new release
# Select the tag, fill in details
# APK will be automatically attached!
```

## 🛠️ Workflow Features

✅ Automatic dependency caching (faster builds)
✅ Build on every push to main
✅ Manual workflow trigger
✅ APK artifact upload (30-90 days retention)
✅ Automatic release attachment
✅ Support for signed APKs
✅ Build logs and error reporting

## 📊 Monitoring Builds

### View Build Status

**Badge in README:**
Add to your README.md:
```markdown
![Build Status](https://github.com/YOUR_USERNAME/android-gemini-agent/workflows/Build%20Android%20APK/badge.svg)
```

**Email Notifications:**
- GitHub → Settings → Notifications → Actions
- Enable notifications for workflow failures

## 🐛 Troubleshooting

### Build Fails: "GEMINI_API_KEY not set"
- Check that secret is added correctly
- Secret name must be exactly: `GEMINI_API_KEY`

### Build Fails: Gradle errors
- Check `build.gradle` syntax
- Review build logs in Actions tab
- Ensure all dependencies are available

### Build Succeeds but APK doesn't work
- Check that API key in secret is valid
- Verify Android version compatibility
- Review logs in the app after installing

### Can't download artifact
- Artifacts expire after retention period
- Re-run the workflow to generate new artifact

## 💡 Tips

1. **Branch Protection:**
   - Use pull requests to test builds before merging to main
   - Set up required checks

2. **Build Optimization:**
   - Gradle cache speeds up subsequent builds
   - First build takes ~5-10 minutes
   - Subsequent builds: ~2-3 minutes

3. **Testing:**
   - Use manual workflow trigger to test without pushing
   - Create a `develop` branch for experimental builds

4. **Multiple APKs:**
   - Modify workflow to build multiple variants (debug, release, etc.)
   - Each will be a separate artifact

## 📱 Direct Install Links

After building, you can share direct install links:

```
https://github.com/YOUR_USERNAME/android-gemini-agent/releases/latest/download/app-debug.apk
```

## 🔗 Useful GitHub Actions

### Check Build Status
```bash
gh run list --workflow=build-apk.yml
```

### Download Latest Artifact
```bash
gh run download
```

### Trigger Manual Build
```bash
gh workflow run build-apk.yml
```

## 📝 Example: Complete Setup

```bash
# 1. Navigate to project
cd ~/android-gemini-agent

# 2. Initialize git
git init
git add .
git commit -m "Initial commit"

# 3. Create repo on GitHub, then push
git remote add origin https://github.com/yourusername/android-gemini-agent.git
git branch -M main
git push -u origin main

# 4. Add secret on GitHub:
#    Settings → Secrets → New secret
#    Name: GEMINI_API_KEY
#    Value: your_api_key

# 5. Wait for automatic build or trigger manually
#    Actions → Build Android APK → Run workflow

# 6. Download APK from Actions → Artifacts
```

## ✨ Advanced: Auto-Deploy to Telegram/Discord

You can extend the workflow to automatically send APKs to Telegram or Discord channels. See GitHub Actions marketplace for integrations.

---

**Need Help?**
- Check [GitHub Actions Documentation](https://docs.github.com/en/actions)
- Review build logs in Actions tab
- Open an issue in your repository
