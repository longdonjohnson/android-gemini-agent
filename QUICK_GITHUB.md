# 🚀 GitHub Actions Quick Start

Build your APK automatically without a computer!

## Steps (5 minutes)

### 1. Push to GitHub

```bash
cd ~/android-gemini-agent
./push-to-github.sh
```

The script will guide you through:
- Creating GitHub repository
- Pushing code
- Setting up secrets

### 2. Add API Key

After pushing, add your Gemini API key:

1. Go to: `https://github.com/YOUR_USERNAME/android-gemini-agent/settings/secrets/actions`
2. Click **"New repository secret"**
3. Name: `GEMINI_API_KEY`
4. Value: Your API key from https://aistudio.google.com/apikey
5. Click **"Add secret"**

### 3. Build APK

**Automatic (on every push):**
```bash
git add .
git commit -m "Update"
git push
```

**Manual trigger:**
1. Go to **Actions** tab on GitHub
2. Click **"Build Android APK"**
3. Click **"Run workflow"** → **"Run workflow"**

### 4. Download APK

1. Go to **Actions** tab
2. Click the completed workflow run (green checkmark)
3. Scroll to **Artifacts**
4. Download **app-debug**
5. Extract ZIP and transfer APK to your phone

### 5. Install

On your Android device:
1. Open file manager
2. Tap the APK file
3. Enable "Install from Unknown Sources" if prompted
4. Install and enjoy!

## What Gets Built

- **Build time:** ~5-10 minutes (first time), ~2-3 minutes (subsequent)
- **APK size:** ~5-8 MB
- **Retention:** 30 days

## Troubleshooting

**Build fails?**
- Check that `GEMINI_API_KEY` secret is added correctly
- Review build logs in Actions tab

**Can't download artifact?**
- Sign in to GitHub
- Artifacts expire after 30 days - re-run workflow

**APK won't install?**
- Enable "Install from Unknown Sources" in Android settings
- Ensure Android 8.0+

## Advanced

**Auto-release on tags:**
```bash
git tag v1.0.0
git push origin v1.0.0
```
APK automatically attached to release!

**Direct install link:**
After creating a release:
```
https://github.com/YOUR_USERNAME/android-gemini-agent/releases/latest/download/app-debug.apk
```

---

Need help? See [GITHUB_SETUP.md](GITHUB_SETUP.md) for detailed guide.
