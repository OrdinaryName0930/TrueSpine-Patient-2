#!/bin/bash

# 📧 OAuth2 Email Setup Script for BrightCare Patient
# This script helps you configure OAuth2 email sending for OTP codes

echo "🔐 BrightCare Patient - OAuth2 Email Setup"
echo "=========================================="
echo ""

# Check if Firebase CLI is installed
if ! command -v firebase &> /dev/null; then
    echo "❌ Firebase CLI not found. Installing..."
    npm install -g firebase-tools
fi

echo "📋 You need the following from Google Cloud Console:"
echo "   1. OAuth2 Client ID"
echo "   2. OAuth2 Client Secret" 
echo "   3. Refresh Token (from OAuth2 Playground)"
echo "   4. Your Gmail address"
echo ""

# Get OAuth2 credentials
read -p "🔑 Enter your OAuth2 Client ID: " CLIENT_ID
read -p "🔐 Enter your OAuth2 Client Secret: " CLIENT_SECRET
read -p "🔄 Enter your Refresh Token: " REFRESH_TOKEN
read -p "📧 Enter your Gmail address: " USER_EMAIL

echo ""
echo "⚙️  Configuring Firebase Functions..."

# Set OAuth2 configuration
firebase functions:config:set gmail.client_id="$CLIENT_ID"
firebase functions:config:set gmail.client_secret="$CLIENT_SECRET" 
firebase functions:config:set gmail.refresh_token="$REFRESH_TOKEN"
firebase functions:config:set gmail.user_email="$USER_EMAIL"

echo ""
echo "📦 Installing dependencies..."
cd functions
npm install

echo ""
echo "🚀 Deploying functions..."
cd ..
firebase deploy --only functions

echo ""
echo "✅ Setup complete!"
echo ""
echo "📧 Your app will now send ACTUAL OTP codes via email using OAuth2!"
echo ""
echo "🧪 Test your setup:"
echo "   1. Run your app"
echo "   2. Try the forgot password feature"
echo "   3. Check your email for the OTP code"
echo ""
echo "🔍 Debug commands:"
echo "   firebase functions:config:get  # View configuration"
echo "   firebase functions:log         # View function logs"
echo ""
echo "📖 For detailed setup instructions, see EMAIL_SETUP_GUIDE.md"



