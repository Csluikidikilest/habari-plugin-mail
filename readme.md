# Habari Plugin Mail
## Purpose
This is a plugin which permit Habari to send mail.

## Configuration
```json
{
  "asynchronous": false,
  "bccs": [],
  "body": "Test Message",
  "ccs": [],
  "connectionType": "com.qazima.habari.plugin.mail.Plugin",
  "from": {
    "address": "contact@habari.com",
    "name": "Contact HABARI"
  },
  "host": "smtp.gmail.com",
  "htmlBody": false,
  "login": "",
  "password": "",
  "port": 587,
  "replyTo": {
    "address": "contact@habari.com",
    "name": "Contact HABARI"
  },
  "sslProtocols": {
    "ssl2": true,
    "ssl3": true,
    "tls": true,
    "tls11": true,
    "tls12": true,
    "tls13": true
  },
  "subject": "mail subject : {subject}",
  "tos": [
    {
      "address": "{toMail}",
      "name": "{toName}"
    }
  ],
  "uri": "^/api/contact"
}
```
