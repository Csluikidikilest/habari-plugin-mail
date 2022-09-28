package com.qazima.habari.plugin.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@JsonTypeName("com.qazima.habari.plugin.mail.Plugin")
public class Plugin extends com.qazima.habari.plugin.core.Plugin {
    @Getter
    @Setter
    @JsonProperty("asynchronous")
    private boolean asynchronous;
    @Getter
    @JsonProperty("bccs")
    private final List<Address> bccs = new ArrayList<>();
    @Getter
    @Setter
    @JsonProperty("body")
    private String body;
    @Getter
    @JsonProperty("ccs")
    private final List<Address> ccs = new ArrayList<>();
    @Getter
    @Setter
    @JsonProperty("from")
    private Address from;
    @Getter
    @Setter
    @JsonProperty("host")
    private String host;
    @Getter
    @Setter
    @JsonProperty("htmlBody")
    private boolean htmlBody;
    @Getter
    @Setter
    @JsonProperty("login")
    private String login;
    @Getter
    @Setter
    @JsonProperty("password")
    private String password;
    @Getter
    @Setter
    @JsonProperty("port")
    private int port;
    @Getter
    @JsonProperty("replyTos")
    private final List<Address> replyTos = new ArrayList<>();
    @Getter
    @Setter
    @JsonProperty("sslEnabled")
    private boolean sslEnabled;
    @Getter
    @Setter
    @JsonProperty("sslProtocol")
    private String sslProtocol;
    @Getter
    @Setter
    @JsonProperty("subject")
    private String subject;
    @Getter
    @Setter
    @JsonProperty("tlsEnabled")
    private boolean tlsEnabled;
    @Getter
    @JsonProperty("tos")
    private final List<Address> tos = new ArrayList<>();

    private boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    private boolean isNullOrWhiteSpace(String string) {
        return isNullOrEmpty(string) || string.trim().isEmpty();
    }

    private Map<String, String> splitRequestBody(String parameters) throws IOException {
        Map<String, String> result = Collections.emptyMap();
        if (!isNullOrWhiteSpace(parameters)) {
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readValue(parameters, Map.class);
        }
        return result;
    }

    public static Map<String, String> splitQuery(String parameters) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String[] pairs = parameters.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
        }
        return query_pairs;
    }

    public int process(HttpExchange httpExchange, Content content) {
        List<Address> bccs = new ArrayList<>();
        String body = getBody();
        List<Address> ccs = new ArrayList<>();
        Address from = new Address();
        List<Address> replyTos = new ArrayList<>();
        String subject = getSubject();
        List<Address> tos = new ArrayList<>();

        Address address;
        for (Address bcc : getBccs()){
            address = new Address();
            address.setAddress(bcc.getAddress());
            address.setName(bcc.getName());
            bccs.add(address);
        }
        for (Address cc : getCcs()){
            address = new Address();
            address.setAddress(cc.getAddress());
            address.setName(cc.getName());
            ccs.add(address);
        }
        from.setAddress(getFrom().getAddress());
        from.setName(getFrom().getName());
        for (Address replyTo : getReplyTos()){
            address = new Address();
            address.setAddress(replyTo.getAddress());
            address.setName(replyTo.getName());
            replyTos.add(address);
        }
        for (Address to : getTos()){
            address = new Address();
            address.setAddress(to.getAddress());
            address.setName(to.getName());
            tos.add(address);
        }

        try {
            String queryString;
            Map<String, String> parameters;
            if("GET".equalsIgnoreCase(httpExchange.getRequestMethod())) {
                queryString = httpExchange.getRequestURI().getQuery();
                parameters = splitQuery(queryString);
            } else {
                queryString = new String(httpExchange.getRequestBody().readAllBytes());
                parameters = splitRequestBody(queryString);
            }
            for (String parameterKey : parameters.keySet()) {
                String parameterValue = parameters.get(parameterKey);
                if(!isNullOrWhiteSpace(parameterValue)) {
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

            Properties properties = new Properties();
            properties.put("mail.smtp.auth", !isNullOrWhiteSpace(getLogin()) && !isNullOrWhiteSpace(getPassword()));
            properties.put("mail.smtp.ssl.enable", isSslEnabled());
            properties.put("mail.smtp.ssl.protocols", getSslProtocol());
            properties.put("mail.smtp.starttls.enable", isTlsEnabled());
            properties.put("mail.smtp.host", getHost());
            properties.put("mail.smtp.port", getPort());
            properties.put("mail.smtp.ssl.trust", getHost());

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getLogin(), getPassword());
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
            if(isHtmlBody()) {
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

            if(isAsynchronous()) {
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
