package com.qazima.habari.plugin.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.qazima.habari.plugin.core.Content;
import com.sun.net.httpserver.HttpExchange;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpStatus;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@JsonTypeName("com.qazima.habari.plugin.mail.Plugin")
public class Plugin extends com.qazima.habari.plugin.core.Plugin {
    @Getter
    @Setter
    @JsonProperty("configuration")
    private com.qazima.habari.plugin.mail.Configuration configuration;

    public int process(HttpExchange httpExchange, Content content) {
        List<Address> bccs = new ArrayList<>();
        String body = getConfiguration().getBody();
        List<Address> ccs = new ArrayList<>();
        Address from = new Address();
        List<Address> replyTos = new ArrayList<>();
        String subject = getConfiguration().getSubject();
        List<Address> tos = new ArrayList<>();

        Address address;
        for (Address bcc : getConfiguration().getBccs()){
            address = new Address();
            address.setAddress(bcc.getAddress());
            address.setName(bcc.getName());
            bccs.add(address);
        }
        for (Address cc : getConfiguration().getCcs()){
            address = new Address();
            address.setAddress(cc.getAddress());
            address.setName(cc.getName());
            ccs.add(address);
        }
        from.setAddress(getConfiguration().getFrom().getAddress());
        from.setName(getConfiguration().getFrom().getName());
        for (Address replyTo : getConfiguration().getReplyTos()){
            address = new Address();
            address.setAddress(replyTo.getAddress());
            address.setName(replyTo.getName());
            replyTos.add(address);
        }
        for (Address to : getConfiguration().getTos()){
            address = new Address();
            address.setAddress(to.getAddress());
            address.setName(to.getName());
            tos.add(address);
        }

        try {
            String queryString;
            Map<String, List<String>> parameters;
            if("GET".equalsIgnoreCase(httpExchange.getRequestMethod())) {
                queryString = httpExchange.getRequestURI().getQuery();
                parameters = splitQuery(queryString);
            } else {
                queryString = new String(httpExchange.getRequestBody().readAllBytes());
                parameters = splitRequestBody(queryString);
            }
            for (String parameterKey : parameters.keySet()) {
                List<String> parameterValues = parameters.get(parameterKey);
                if(!parameterValues.isEmpty()) {
                    for (String parameterValue : parameterValues) {
                        for(Address bcc : bccs) {
                            bcc.setAddress(bcc.getAddress().replace("{" + parameterKey + "}", parameterValue));
                            bcc.setName(bcc.getName().replace("{" + parameterKey + "}", parameterValue));
                        }
                        body = body.replace("{" + parameterKey + "}", parameterValue);
                        for(Address cc : ccs) {
                            cc.setAddress(cc.getAddress().replace("{" + parameterKey + "}", parameterValue));
                            cc.setName(cc.getName().replace("{" + parameterKey + "}", parameterValue));
                        }
                        from.setAddress(from.getAddress().replace("{" + parameterKey + "}", parameterValue));
                        from.setName(from.getName().replace("{" + parameterKey + "}", parameterValue));
                        for(Address replyTo : replyTos) {
                            replyTo.setAddress(replyTo.getAddress().replace("{" + parameterKey + "}", parameterValue));
                            replyTo.setName(replyTo.getName().replace("{" + parameterKey + "}", parameterValue));
                        }
                        subject = subject.replace("{" + parameterKey + "}", parameterValue);
                        for(Address to : tos) {
                            to.setAddress(to.getAddress().replace("{" + parameterKey + "}", parameterValue));
                            to.setName(to.getName().replace("{" + parameterKey + "}", parameterValue));
                        }
                    }
                }
            }

            Properties properties = new Properties();
            properties.put("mail.smtp.auth", !isNullOrWhiteSpace(getConfiguration().getLogin()) && !isNullOrWhiteSpace(getConfiguration().getPassword()));
            properties.put("mail.smtp.ssl.enable", getConfiguration().isSslEnabled());
            properties.put("mail.smtp.ssl.protocols", getConfiguration().getSslProtocol());
            properties.put("mail.smtp.starttls.enable", getConfiguration().isTlsEnabled());
            properties.put("mail.smtp.host", getConfiguration().getHost());
            properties.put("mail.smtp.port", getConfiguration().getPort());
            properties.put("mail.smtp.ssl.trust", getConfiguration().getHost());

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getConfiguration().getLogin(), getConfiguration().getPassword());
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(from.toInternetAddress());
            List<InternetAddress> list = new ArrayList<>();
            for (Address replyTo : replyTos) {
                InternetAddress toInternetAddress = replyTo.toInternetAddress();
                list.add(toInternetAddress);
            }
            message.setReplyTo(list.toArray(new javax.mail.Address[0]));
            message.setSubject(subject);
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            if(getConfiguration().isHtmlBody()) {
                mimeBodyPart.setContent(body, "text/html; charset=utf-8");
            } else {
                mimeBodyPart.setContent(body, "text/plain; charset=utf-8");
            }
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            message.setContent(multipart);

            for (Address bcc : bccs) {
                message.setRecipient(Message.RecipientType.BCC, bcc.toInternetAddress());
            }
            for (Address cc : ccs) {
                message.setRecipient(Message.RecipientType.CC, cc.toInternetAddress());
            }
            for (Address to : tos) {
                message.setRecipient(Message.RecipientType.TO, to.toInternetAddress());
            }

            if(getConfiguration().isAsynchronous()) {
                Thread thread = new Thread(() -> {
                    try {
                        Transport.send(message);
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                });
                thread.start();
            } else {
                Transport.send(message);
            }
            String contentBody = "";
            contentBody += "mail from: " + from.getName() + "<" + from.getAddress() + ">" + System.lineSeparator();
            contentBody += "mail subject: " + subject + System.lineSeparator();
            contentBody += "mail body: " + System.lineSeparator();
            contentBody += "--------------------------------------------------------------------------" + System.lineSeparator();
            contentBody += body + System.lineSeparator();
            contentBody += "--------------------------------------------------------------------------" + System.lineSeparator();

            for (Address bcc : bccs) {
                contentBody += "mail bcc: " + bcc.getName() + "<" + bcc.getAddress() + ">" + System.lineSeparator();
            }
            for (Address cc : ccs) {
                contentBody += "mail cc: " + cc.getName() + "<" + cc.getAddress() + ">" + System.lineSeparator();
            }
            for (Address to : tos) {
                contentBody += "mail to: " + to.getName() + "<" + to.getAddress() + ">" + System.lineSeparator();
            }

            content.setStatusCode(HttpStatus.SC_OK);
            content.setType("text/plain");
            content.setBody(contentBody.getBytes(StandardCharsets.UTF_8));
        } catch (MessagingException | IOException e) {
            content.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            content.setType("text/plain");
            content.setBody(e.toString().getBytes(StandardCharsets.UTF_8));
        }

        return content.getStatusCode();
    }
}
