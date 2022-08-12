## Spring boot security server side application
Implements the following features and patterns (configuration over convention - implemented manually):
* MFA using [Twilio](https://www.twilio.com) for sending one time password code as sms
* Authorization with JWT tokens using locally generate keystore
* Using refresh token to renew expired authorization token
* Sending mail for activating user account and for changing password with [Mailtrap](https://mailtrap.io/)
* Removing not activated users with scheduled job
