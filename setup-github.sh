#!/bin/bash

echo "╔════════════════════════════════════════════════════════════╗"
echo "║                                                            ║"
echo "║          GitHub Authentication & Auto-Push Setup          ║"
echo "║                                                            ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Initialize git if not done
if [ ! -d .git ]; then
    echo "📦 Initializing git repository..."
    git init
    git add .
    git commit -m "Initial commit: Android Gemini Agent with runtime API key"
fi

# Check if authenticated
if gh auth status 2>/dev/null | grep -q "Logged in"; then
    echo "✅ Already authenticated with GitHub!"
    echo ""
else
    echo "🔐 Not authenticated. Let's log in to GitHub..."
    echo ""
    echo "Method 1: Browser Authentication (Easiest)"
    echo "  1. Run: gh auth login"
    echo "  2. Choose: HTTPS"
    echo "  3. Choose: Login with a web browser"
    echo "  4. Copy the code"
    echo "  5. Open: https://github.com/login/device"
    echo "  6. Paste code and authorize"
    echo ""
    echo "Method 2: Personal Access Token"
    echo "  1. Go to: https://github.com/settings/tokens"
    echo "  2. Generate new token (classic)"
    echo "  3. Select: repo, workflow"
    echo "  4. Copy token"
    echo "  5. Run: gh auth login"
    echo "  6. Choose: Paste an authentication token"
    echo ""
    
    read -p "Press Enter to start authentication..."
    gh auth login
fi

echo ""
echo "════════════════════════════════════════════════════════════"
echo ""

if gh auth status 2>/dev/null | grep -q "Logged in"; then
    echo "✅ Authentication successful!"
    echo ""
    
    # Get GitHub username
    GH_USER=$(gh api user -q .login)
    echo "👤 Logged in as: $GH_USER"
    echo ""
    
    # Ask for repository name
    read -p "📦 Repository name (default: android-gemini-agent): " REPO_NAME
    REPO_NAME=${REPO_NAME:-android-gemini-agent}
    
    echo ""
    echo "Creating repository and pushing..."
    echo ""
    
    # Create repo if it doesn't exist
    if gh repo view "$GH_USER/$REPO_NAME" >/dev/null 2>&1; then
        echo "ℹ️  Repository already exists: $GH_USER/$REPO_NAME"
    else
        echo "📦 Creating repository: $GH_USER/$REPO_NAME"
        gh repo create "$REPO_NAME" --public --source=. --remote=origin --description "Android AI agent using Gemini 2.5 Computer Use Preview"
    fi
    
    # Set remote if not set
    if ! git remote get-url origin >/dev/null 2>&1; then
        git remote add origin "https://github.com/$GH_USER/$REPO_NAME.git"
    fi
    
    # Rename branch to main
    CURRENT_BRANCH=$(git branch --show-current)
    if [ "$CURRENT_BRANCH" != "main" ]; then
        git branch -M main
    fi
    
    # Push to GitHub
    echo "⬆️  Pushing to GitHub..."
    git push -u origin main
    
    echo ""
    echo "════════════════════════════════════════════════════════════"
    echo "  ✅ SUCCESS! Project pushed to GitHub"
    echo "════════════════════════════════════════════════════════════"
    echo ""
    echo "📦 Repository: https://github.com/$GH_USER/$REPO_NAME"
    echo "🔧 Settings: https://github.com/$GH_USER/$REPO_NAME/settings"
    echo "🚀 Actions: https://github.com/$GH_USER/$REPO_NAME/actions"
    echo ""
    echo "════════════════════════════════════════════════════════════"
    echo ""
    echo "🎯 NEXT STEP: Add API Key Secret"
    echo ""
    echo "1. Go to: https://github.com/$GH_USER/$REPO_NAME/settings/secrets/actions"
    echo "2. Click: 'New repository secret'"
    echo "3. Name: GEMINI_API_KEY"
    echo "4. Value: [Your Gemini API key from ai.google.dev]"
    echo "5. Click: 'Add secret'"
    echo ""
    echo "Then the build will start automatically!"
    echo ""
    echo "Or trigger manually:"
    echo "  gh workflow run build-apk.yml"
    echo ""
    echo "Watch build:"
    echo "  gh run watch"
    echo ""
    
else
    echo "❌ Authentication failed. Please try again."
    exit 1
fi
