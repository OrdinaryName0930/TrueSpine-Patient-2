# 📧 OAuth2 Email OTP Setup Guide / Gabay sa OAuth2 Email OTP Setup

## 🎯 **What This Does / Ano ang Ginagawa Nito**

This setup sends **ACTUAL OTP CODES via email** (not password reset links) using OAuth2 authentication for maximum security.

**Ang setup na ito ay nagpapadala ng TUNAY na OTP CODES sa email** (hindi password reset links) gamit ang OAuth2 authentication para sa maximum security.

## 🚨 Current Issue / Kasalukuyang Problema

Your OTP emails are not being sent because the EmailService needs OAuth2 configuration. Here's how to fix it:

Ang inyong OTP emails ay hindi napapadala dahil ang EmailService ay kailangan ng OAuth2 configuration. Narito kung paano ito ayusin:

## 🔧 Solutions / Mga Solusyon

### Option 1: Quick Testing (Development Mode)

**For immediate testing, check your Android Studio Logcat:**

Para sa mabilis na pagsubok, tingnan ang inyong Android Studio Logcat:

1. Open Android Studio
2. Go to **Logcat** tab (bottom panel)
3. Filter by "EmailService" 
4. Look for logs like:
   ```
   === OTP FOR EMAIL your-email@example.com ===
   Your OTP code is: 123456
   ==============================
   ```

### Option 2: OAuth2 Gmail Setup (Recommended - Sends Real OTP Emails)

**This method sends ACTUAL OTP codes via email using OAuth2 authentication:**

**Ang method na ito ay nagpapadala ng TUNAY na OTP codes sa email gamit ang OAuth2 authentication:**

#### A. Google Cloud Console Setup

1. **Go to Google Cloud Console**: https://console.cloud.google.com/
2. **Create or Select Project**: 
   - Create new project or select existing one
   - Enable Gmail API
3. **Create OAuth2 Credentials**:
   - Go to APIs & Services → Credentials
   - Click "Create Credentials" → OAuth 2.0 Client IDs
   - Application type: Web application
   - Authorized redirect URIs: `https://developers.google.com/oauthplayground`

#### B. Get OAuth2 Tokens

1. **Go to OAuth2 Playground**: https://developers.google.com/oauthplayground/
2. **Configure OAuth2**:
   - Click settings gear (⚙️) → Use your own OAuth credentials
   - Enter your Client ID and Client Secret
3. **Authorize Gmail API**:
   - In Step 1: Select Gmail API v1 → `https://mail.google.com/`
   - Click "Authorize APIs"
   - Sign in with your Gmail account
4. **Get Tokens**:
   - In Step 2: Click "Exchange authorization code for tokens"
   - Copy the **Refresh Token** (you'll need this)

#### C. Configure Firebase Functions

```bash
# Set OAuth2 credentials
firebase functions:config:set gmail.client_id="your-client-id.apps.googleusercontent.com"
firebase functions:config:set gmail.client_secret="your-client-secret"
firebase functions:config:set gmail.refresh_token="your-refresh-token"
firebase functions:config:set gmail.user_email="your-gmail@gmail.com"

# Fallback SMTP (optional)
firebase functions:config:set email.user="your-gmail@gmail.com"
firebase functions:config:set email.password="your-app-password"
```

#### B. SendGrid (Professional)

1. **Create SendGrid account** at https://sendgrid.com/
2. **Get API Key**:
   - Go to Settings → API Keys
   - Create API Key with "Mail Send" permissions
3. **Configure Firebase Functions**:
   ```bash
   firebase functions:config:set sendgrid.api_key="your-sendgrid-api-key"
   firebase functions:config:set sendgrid.from_email="noreply@yourdomain.com"
   ```

#### C. AWS SES (Enterprise)

1. **Set up AWS SES** in your AWS Console
2. **Verify your domain/email**
3. **Get AWS credentials**
4. **Configure Firebase Functions**:
   ```bash
   firebase functions:config:set aws.access_key="your-access-key"
   firebase functions:config:set aws.secret_key="your-secret-key"
   firebase functions:config:set aws.region="us-east-1"
   ```

## 🚀 Deployment Steps / Mga Hakbang sa Deployment

### Step 1: Install Firebase CLI

```bash
npm install -g firebase-tools
```

### Step 2: Login to Firebase

```bash
firebase login
```

### Step 3: Initialize Functions (if not done)

```bash
firebase init functions
```

### Step 4: Install Dependencies

```bash
cd functions
npm install
```

### Step 5: Configure OAuth2 (Choose Method A or B)

**Method A: OAuth2 Gmail (Recommended)**
```bash
# Replace with your actual values from Google Cloud Console
firebase functions:config:set gmail.client_id="123456789-abc.apps.googleusercontent.com"
firebase functions:config:set gmail.client_secret="GOCSPX-your-client-secret"
firebase functions:config:set gmail.refresh_token="1//04-your-refresh-token"
firebase functions:config:set gmail.user_email="your-email@gmail.com"
```

**Method B: SMTP Fallback (Backup)**
```bash
firebase functions:config:set email.user="your-email@gmail.com"
firebase functions:config:set email.password="your-16-char-app-password"
```

### Step 6: Deploy Functions

```bash
firebase deploy --only functions
```

### Step 7: Test the Function

```bash
# Test locally first
firebase emulators:start --only functions

# Then deploy to production
firebase deploy --only functions
```

### Step 8: Verify Configuration

```bash
# Check your configuration
firebase functions:config:get

# View function logs
firebase functions:log
```

## 🧪 Testing / Pagsubok

### Test with Gmail SMTP:

1. Set up Gmail app password (see above)
2. Configure Firebase Functions:
   ```bash
   firebase functions:config:set email.user="your-gmail@gmail.com"
   firebase functions:config:set email.password="your-16-char-app-password"
   ```
3. Deploy functions: `firebase deploy --only functions`
4. Test OTP in your app

### Test with SendGrid:

1. Get SendGrid API key
2. Configure Firebase Functions:
   ```bash
   firebase functions:config:set sendgrid.api_key="SG.your-api-key"
   firebase functions:config:set sendgrid.from_email="noreply@yourdomain.com"
   ```
3. Deploy functions: `firebase deploy --only functions`
4. Test OTP in your app

## 🔍 Troubleshooting / Pag-troubleshoot

### Common Issues:

#### 1. "Cloud Function not available"
- **Solution**: Deploy functions first: `firebase deploy --only functions`
- **Check**: Firebase Console → Functions tab

#### 2. "Gmail authentication failed"
- **Solution**: Use App Password, not regular password
- **Check**: 2-Factor Authentication is enabled

#### 3. "SendGrid API error"
- **Solution**: Verify API key permissions
- **Check**: Sender email is verified in SendGrid

#### 4. "Firebase Functions timeout"
- **Solution**: Increase timeout in functions configuration
- **Check**: Function logs in Firebase Console

### Debug Commands:

```bash
# View function logs
firebase functions:log

# Test function locally
firebase emulators:start --only functions

# Check function configuration
firebase functions:config:get
```

## 📱 App Integration

The updated EmailService now tries multiple methods:

1. **Firebase Cloud Functions** (Production) - Real emails
2. **Firebase Auth Password Reset** (Fallback) - Reset link
3. **Development Logging** (Last resort) - Console logs

Ang na-update na EmailService ay sumusubok ng maraming paraan:

1. **Firebase Cloud Functions** (Production) - Tunay na emails
2. **Firebase Auth Password Reset** (Fallback) - Reset link
3. **Development Logging** (Last resort) - Console logs

## 🔒 Security Notes / Mga Tala sa Security

- **Never commit** email credentials to version control
- **Use environment variables** for sensitive data
- **Enable email rate limiting** to prevent abuse
- **Validate email addresses** before sending
- **Log email attempts** for monitoring

## 📞 Support / Suporta

If you need help with setup:

Kung kailangan ninyo ng tulong sa setup:

1. Check Firebase Console → Functions → Logs
2. Review Android Studio Logcat for errors
3. Test with Firebase Emulator first
4. Verify email service provider settings

## 🎯 Next Steps / Susunod na Hakbang

1. ✅ Choose email provider (Gmail/SendGrid/AWS SES)
2. ✅ Configure Firebase Functions
3. ✅ Deploy functions to Firebase
4. ✅ Test OTP delivery
5. ✅ Monitor email delivery rates
6. ✅ Set up email templates
7. ✅ Configure rate limiting
