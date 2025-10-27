#!/bin/bash

# Script to push Android Gemini Agent to GitHub

echo "=========================================="
echo "  Push to GitHub & Auto-Build APK"
echo "=========================================="
echo ""

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo "❌ Git is not installed. Installing..."
    pkg install git -y
fi

# Check if gh CLI is available (optional)
if command -v gh &> /dev/null; then
    USE_GH_CLI=true
else
    USE_GH_CLI=false
    echo "💡 Tip: Install GitHub CLI for easier setup: pkg install gh"
fi

echo "This script will help you:"
echo "1. Initialize git repository"
echo "2. Create GitHub repository"
echo "3. Push code to GitHub"
echo "4. Set up automatic APK builds"
echo ""

read -p "Continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Cancelled."
    exit 1
fi

# Get GitHub username
echo ""
read -p "Enter your GitHub username: " GITHUB_USER

# Get repository name
echo ""
read -p "Enter repository name (default: android-gemini-agent): " REPO_NAME
REPO_NAME=${REPO_NAME:-android-gemini-agent}

# Get Gemini API key for GitHub Secrets
echo ""
echo "⚠️  You'll need to add your Gemini API key as a GitHub Secret after pushing."
echo "Get your key from: https://aistudio.google.com/apikey"
read -p "Enter your Gemini API key (will be shown in instructions): " GEMINI_KEY

# Initialize git if not already
if [ ! -d .git ]; then
    echo ""
    echo "📦 Initializing git repository..."
    git init
    git add .
    git commit -m "Initial commit: Android Gemini Agent with auto-build"
else
    echo ""
    echo "✅ Git repository already initialized"
fi

# Check if remote exists
if git remote get-url origin &> /dev/null; then
    echo "⚠️  Remote 'origin' already exists. Removing..."
    git remote remove origin
fi

# Add remote
echo ""
echo "🔗 Adding GitHub remote..."
git remote add origin "https://github.com/$GITHUB_USER/$REPO_NAME.git"

# Rename branch to main if needed
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "main" ]; then
    echo "🔄 Renaming branch to 'main'..."
    git branch -M main
fi

echo ""
echo "=========================================="
echo "  Next Steps"
echo "=========================================="
echo ""
echo "1️⃣  Create the repository on GitHub:"
echo ""

if [ "$USE_GH_CLI" = true ]; then
    echo "   Run this command to create repo with GitHub CLI:"
    echo "   gh repo create $REPO_NAME --public --source=. --remote=origin --push"
    echo ""
    read -p "   Create repo now with gh CLI? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        gh repo create "$GITHUB_USER/$REPO_NAME" --public --source=. --remote=origin --push
        REPO_CREATED=true
    fi
else
    echo "   a) Go to: https://github.com/new"
    echo "   b) Repository name: $REPO_NAME"
    echo "   c) Make it Public (or Private)"
    echo "   d) Don't add README, .gitignore, or license"
    echo "   e) Click 'Create repository'"
    echo ""
    read -p "   Press Enter after creating the repository..."
fi

if [ "$REPO_CREATED" != true ]; then
    echo ""
    echo "2️⃣  Push your code:"
    echo "   git push -u origin main"
    echo ""
    read -p "   Push now? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git push -u origin main
    fi
fi

echo ""
echo "3️⃣  Add Gemini API Key as GitHub Secret:"
echo "   a) Go to: https://github.com/$GITHUB_USER/$REPO_NAME/settings/secrets/actions"
echo "   b) Click 'New repository secret'"
echo "   c) Name: GEMINI_API_KEY"
echo "   d) Value: $GEMINI_KEY"
echo "   e) Click 'Add secret'"
echo ""

echo "4️⃣  Trigger the build:"
echo "   a) Go to: https://github.com/$GITHUB_USER/$REPO_NAME/actions"
echo "   b) Click 'Build Android APK'"
echo "   c) Click 'Run workflow' → 'Run workflow'"
echo "   d) Wait ~5-10 minutes for first build"
echo ""

echo "5️⃣  Download your APK:"
echo "   a) After build completes, go to Actions"
echo "   b) Click the completed workflow run"
echo "   c) Scroll to 'Artifacts'"
echo "   d) Download 'app-debug'"
echo "   e) Extract and install APK on your Android device!"
echo ""

echo "=========================================="
echo "  Quick Links"
echo "=========================================="
echo ""
echo "📦 Repository:    https://github.com/$GITHUB_USER/$REPO_NAME"
echo "⚙️  Settings:      https://github.com/$GITHUB_USER/$REPO_NAME/settings"
echo "🔒 Secrets:       https://github.com/$GITHUB_USER/$REPO_NAME/settings/secrets/actions"
echo "🚀 Actions:       https://github.com/$GITHUB_USER/$REPO_NAME/actions"
echo "📥 Releases:      https://github.com/$GITHUB_USER/$REPO_NAME/releases"
echo ""

echo "✅ Setup complete!"
echo ""
echo "📖 For more details, see GITHUB_SETUP.md"
echo ""
