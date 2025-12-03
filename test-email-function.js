// Test script to call Firebase Function directly
const admin = require('firebase-admin');

// Initialize Firebase Admin (you may need to set GOOGLE_APPLICATION_CREDENTIALS)
admin.initializeApp({
  projectId: 'truespine-e8576'
});

async function testEmailFunction() {
  try {
    console.log('🧪 Testing Firebase Function sendOtpEmail...');
    
    const functions = admin.functions();
    
    const data = {
      email: 'michaeljoshuataleon.edu@gmail.com',
      otp: '123456',
      expiryMinutes: 10,
      appName: 'BrightCare Patient Test'
    };
    
    console.log('📧 Calling function with data:', data);
    
    // Call the function
    const result = await functions.httpsCallable('sendOtpEmail')(data);
    
    console.log('✅ Function result:', result.data);
    
    if (result.data.success) {
      console.log('🎉 Email sent successfully!');
      console.log('📧 Message ID:', result.data.messageId);
      console.log('🔐 Auth Method:', result.data.authMethod);
    } else {
      console.log('❌ Email failed:', result.data.error);
    }
    
  } catch (error) {
    console.error('🚨 Test failed:', error.message);
    console.error('Details:', error);
  }
}

testEmailFunction();



