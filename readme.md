# Habari Plugin Mail
## Purpose
This is a plugin which permit Habari to send mail.

## Configuration
```json
{
  "asynchronous": false,
  "bccs": [],
  "body": "{body}",
  "ccs": [],
  "connectionType": "com.qazima.habari.plugin.mail.Plugin",
  "from": {
    "address": "cbonet@klanik.com",
    "name": "Contact HABARI"
  },
  "host": "smtp host",
  "htmlBody": false,
  "login": "login",
  "password": "password",
  "port": 587,
  "replyTos": [{
    "address": "contact@habari.com",
    "name": "Contact HABARI"
  }],
  "sslEnabled": false,
  "sslProtocol": "TLSv1.2",
  "subject": "Subject: {subject}",
  "tlsEnabled": true,
  "tos": [
    {
      "address": "{toMail}",
      "name": "{toName}"
    }
  ],
  "uri": "^/api/contact"
}
```
