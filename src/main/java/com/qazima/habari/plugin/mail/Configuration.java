package com.qazima.habari.plugin.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Configuration extends com.qazima.habari.plugin.core.Configuration {
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
}
