package com.qazima.habari.plugin.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Address {
    @Getter
    @Setter
    @JsonProperty("address")
    private String address;
    @Getter
    @Setter
    @JsonProperty("name")
    private String name;

    public InternetAddress toInternetAddress() throws UnsupportedEncodingException {
        return new InternetAddress(getAddress(), getName(), StandardCharsets.UTF_8.name());
    }
}
