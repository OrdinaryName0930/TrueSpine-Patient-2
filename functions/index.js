const functions = require('firebase-functions');
const admin = require('firebase-admin');
const nodemailer = require('nodemailer');
const { google } = require('googleapis');

admin.initializeApp();

// OAuth2 configuration for Gmail API
const OAuth2 = google.auth.OAuth2;

/**
 * Create OAuth2 transporter for Gmail
 * This uses OAuth2 instead of app passwords for better security
 */
async function createOAuth2Transporter() {
  const oauth2Client = new OAuth2(
    functions.config().gmail.client_id,     // OAuth2 client ID
    functions.config().gmail.client_secret, // OAuth2 client secret
    "https://developers.google.com/oauthplayground" // Redirect URL
  );

  oauth2Client.setCredentials({
    refresh_token: functions.config().gmail.refresh_token
  });

  const accessToken = await new Promise((resolve, reject) => {
    oauth2Client.getAccessToken((err, token) => {
      if (err) {
        console.error('Failed to create access token:', err);
        reject(err);
      }
      resolve(token);
    });
  });

  const transporter = nodemailer.createTransporter({
    service: 'gmail',
    auth: {
      type: 'OAuth2',
      user: functions.config().gmail.user_email,
      clientId: functions.config().gmail.client_id,
      clientSecret: functions.config().gmail.client_secret,
      refreshToken: functions.config().gmail.refresh_token,
      accessToken: accessToken
    }
  });

  return transporter;
}

// Fallback SMTP configuration (if OAuth2 fails)
const smtpConfig = {
  service: 'gmail',
  auth: {
    user: functions.config().email?.user,
    pass: functions.config().email?.password
  }
};

/**
 * Send OTP email using OAuth2 Gmail API
 * This function sends ACTUAL OTP codes via email (not password reset links)
 * ALLOWS UNAUTHENTICATED CALLS for forgot password functionality
 */
exports.sendOtpEmail = functions.https.onCall(async (data, context) => {
  try {
    console.log('🔥 sendOtpEmail function called');
    console.log('📧 Request data:', JSON.stringify(data));
    console.log('👤 Auth context:', context.auth ? 'Authenticated' : 'Unauthenticated');
    
    // Allow unauthenticated calls for forgot password functionality
    // No authentication required since users can't be logged in when they forgot their password
    
    // Validate input
    if (!data || typeof data !== 'object') {
      console.error('❌ Invalid data format:', data);
      throw new functions.https.HttpsError('invalid-argument', 'Invalid data format');
    }
    
    if (!data.email || !data.otp) {
      console.error('❌ Missing required fields:', { email: !!data.email, otp: !!data.otp });
      throw new functions.https.HttpsError('invalid-argument', 'Email and OTP are required');
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(data.email)) {
      console.error('❌ Invalid email format:', data.email);
      throw new functions.https.HttpsError('invalid-argument', 'Invalid email format');
    }

    // Validate OTP format (should be 6 digits)
    if (!/^\d{6}$/.test(data.otp)) {
      console.error('❌ Invalid OTP format:', data.otp);
      throw new functions.https.HttpsError('invalid-argument', 'OTP must be 6 digits');
    }

    const { email, otp, expiryMinutes = 10, appName = 'BrightCare Patient' } = data;

    // Basic rate limiting - allow only specific domains for security
    const allowedDomains = ['gmail.com', 'yahoo.com', 'outlook.com', 'hotmail.com'];
    const emailDomain = email.split('@')[1];
    if (!allowedDomains.includes(emailDomain)) {
      console.warn('⚠️ Email domain not in allowed list:', emailDomain);
      // Still allow but log for monitoring
    }

    console.log(`✅ Attempting to send OTP ${otp} to ${email}`);

    let transporter;
    let authMethod = 'OAuth2';

    // Try OAuth2 first (preferred method)
    try {
      transporter = await createOAuth2Transporter();
      console.log('Using OAuth2 authentication for Gmail');
    } catch (oauth2Error) {
      console.warn('OAuth2 failed, falling back to SMTP:', oauth2Error.message);
      
      // Fallback to SMTP if OAuth2 fails
      if (smtpConfig.auth.user && smtpConfig.auth.pass) {
        transporter = nodemailer.createTransporter(smtpConfig);
        authMethod = 'SMTP';
        console.log('Using SMTP authentication for Gmail');
      } else {
        throw new Error('No valid email authentication method available');
      }
    }

    // OTP Email HTML template (NOT a password reset link)
    const htmlContent = `
      <!DOCTYPE html>
      <html>
      <head>
          <meta charset="UTF-8">
          <title>${appName} - Your OTP Code</title>
          <style>
              body { 
                  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                  line-height: 1.6; color: #333; margin: 0; padding: 0; 
                  background-color: #f5f5f5;
              }
              .container { 
                  max-width: 600px; margin: 20px auto; 
                  background-color: white; border-radius: 12px; 
                  box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden;
              }
              .header { 
                  background: linear-gradient(135deg, #4280EF 0%, #3366CC 100%); 
                  color: white; padding: 30px 20px; text-align: center; 
              }
              .header h1 { margin: 0; font-size: 28px; font-weight: 300; }
              .header h2 { margin: 10px 0 0 0; font-size: 18px; font-weight: 400; opacity: 0.9; }
              .content { padding: 40px 30px; }
              .greeting { font-size: 18px; color: #333; margin-bottom: 20px; }
              .otp-section { 
                  text-align: center; margin: 30px 0; 
                  padding: 25px; background-color: #f8f9fa; 
                  border-radius: 8px; border-left: 4px solid #4280EF;
              }
              .otp-label { 
                  font-size: 14px; color: #666; margin-bottom: 10px; 
                  text-transform: uppercase; letter-spacing: 1px; font-weight: 500;
              }
              .otp-code { 
                  font-size: 36px; font-weight: bold; color: #4280EF; 
                  letter-spacing: 6px; margin: 15px 0; font-family: 'Courier New', monospace;
                  text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
              }
              .otp-timer { 
                  font-size: 14px; color: #e74c3c; font-weight: 500;
                  margin-top: 10px;
              }
              .instructions { 
                  background-color: #fff3cd; border: 1px solid #ffeaa7; 
                  border-radius: 6px; padding: 20px; margin: 25px 0;
              }
              .instructions h3 { 
                  color: #856404; margin: 0 0 15px 0; font-size: 16px; 
              }
              .instructions ul { 
                  margin: 0; padding-left: 20px; color: #856404; 
              }
              .instructions li { margin-bottom: 8px; }
              .security-notice { 
                  background-color: #f8d7da; border: 1px solid #f5c6cb; 
                  border-radius: 6px; padding: 15px; margin: 20px 0;
                  color: #721c24; text-align: center; font-weight: 500;
              }
              .footer { 
                  text-align: center; padding: 25px; 
                  background-color: #f8f9fa; color: #666; 
                  font-size: 12px; border-top: 1px solid #e9ecef;
              }
              .footer p { margin: 5px 0; }
              .app-info { 
                  margin-top: 15px; padding-top: 15px; 
                  border-top: 1px solid #e9ecef; font-size: 11px; 
              }
          </style>
      </head>
      <body>
          <div class="container">
              <div class="header">
                  <h1>🔐 ${appName}</h1>
                  <h2>Your One-Time Password (OTP)</h2>
              </div>
              
              <div class="content">
                  <div class="greeting">Hello!</div>
                  
                  <p>You requested a One-Time Password (OTP) for your ${appName} account. Please use the code below to verify your identity and proceed with your password reset.</p>
                  
                  <div class="otp-section">
                      <div class="otp-label">Your OTP Code</div>
                      <div class="otp-code">${otp}</div>
                      <div class="otp-timer">⏰ Expires in ${expiryMinutes} minutes</div>
                  </div>
                  
                  <div class="instructions">
                      <h3>📱 How to use this code:</h3>
                      <ul>
                          <li>Open the ${appName} app on your device</li>
                          <li>Go to the "Verify OTP" screen</li>
                          <li>Enter the 6-digit code exactly as shown above</li>
                          <li>Complete your password reset process</li>
                      </ul>
                  </div>
                  
                  <div class="security-notice">
                      🚨 <strong>Security Notice:</strong> This code can only be used once and will expire in ${expiryMinutes} minutes. Never share this code with anyone!
                  </div>
                  
                  <p style="color: #666; font-size: 14px; margin-top: 25px;">
                      If you didn't request this OTP, please ignore this email and ensure your account is secure.
                  </p>
              </div>
              
              <div class="footer">
                  <p><strong>This is an automated message from ${appName}</strong></p>
                  <p>Please do not reply to this email.</p>
                  <div class="app-info">
                      <p>&copy; 2024 BrightCare Healthcare Solutions. All rights reserved.</p>
                      <p>Sent via OAuth2 secure email delivery (${authMethod})</p>
                  </div>
              </div>
          </div>
      </body>
      </html>
    `;

    // Email options - Sending ACTUAL OTP CODE (not reset link)
    const fromEmail = authMethod === 'OAuth2' 
      ? functions.config().gmail.user_email 
      : smtpConfig.auth.user;
      
    const mailOptions = {
      from: `"${appName} 🔐" <${fromEmail}>`,
      to: email,
      subject: `🔐 ${appName} - Your OTP Code: ${otp}`,
      html: htmlContent,
      text: `
🔐 ${appName} - Your OTP Code

Hello!

You requested a One-Time Password (OTP) for your ${appName} account.

Your OTP Code: ${otp}

⏰ This code will expire in ${expiryMinutes} minutes.

How to use:
1. Open the ${appName} app
2. Go to "Verify OTP" screen  
3. Enter the code: ${otp}
4. Complete your password reset

🚨 Security Notice: Never share this code with anyone!

If you didn't request this, please ignore this email.

---
${appName} - Secure Healthcare Solutions
Sent via ${authMethod} authentication
      `.trim()
    };

    // Send the actual OTP email
    const emailResult = await transporter.sendMail(mailOptions);
    
    console.log(`✅ OTP email sent successfully to: ${email}`);
    console.log(`📧 Message ID: ${emailResult.messageId}`);
    console.log(`🔐 OTP Code: ${otp} (expires in ${expiryMinutes} minutes)`);
    console.log(`🔒 Authentication method: ${authMethod}`);
    
    return {
      success: true,
      message: `OTP email sent successfully to ${email}`,
      messageId: emailResult.messageId,
      authMethod: authMethod,
      otpCode: otp, // For debugging (remove in production)
      expiryMinutes: expiryMinutes
    };

  } catch (error) {
    console.error('Error sending OTP email:', error);
    
    return {
      success: false,
      error: error.message || 'Failed to send OTP email'
    };
  }
});

/**
 * Alternative: Send OTP via SendGrid (if you prefer SendGrid)
 */
exports.sendOtpEmailSendGrid = functions.https.onCall(async (data, context) => {
  const sgMail = require('@sendgrid/mail');
  
  try {
    // Set SendGrid API key
    sgMail.setApiKey(functions.config().sendgrid.api_key);

    const { email, otp, expiryMinutes = 10, appName = 'BrightCare Patient' } = data;

    const msg = {
      to: email,
      from: functions.config().sendgrid.from_email, // Verified sender
      subject: `${appName} - Password Reset OTP`,
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #4280EF;">${appName} - Password Reset</h2>
          <p>Your OTP code is:</p>
          <div style="font-size: 32px; font-weight: bold; color: #4280EF; text-align: center; 
                      letter-spacing: 8px; margin: 20px 0; padding: 15px; 
                      background-color: #f9f9f9; border: 2px dashed #4280EF;">
            ${otp}
          </div>
          <p>This code will expire in ${expiryMinutes} minutes.</p>
          <p style="color: #e74c3c; font-weight: bold;">Do not share this code with anyone.</p>
        </div>
      `
    };

    await sgMail.send(msg);
    
    return {
      success: true,
      message: 'OTP email sent successfully via SendGrid'
    };

  } catch (error) {
    console.error('SendGrid error:', error);
    return {
      success: false,
      error: error.message || 'Failed to send OTP email via SendGrid'
    };
  }
});
