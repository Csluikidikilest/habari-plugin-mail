package com.qazima.habari.plugin.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class SslProtocols {
    @Getter
    @Setter
    @JsonProperty("ssl2")
    private boolean ssl2;
    @Getter
    @Setter
    @JsonProperty("ssl3")
    private boolean ssl3;
    @Getter
    @Setter
    @JsonProperty("tls")
    private boolean tls;
    @Getter
    @Setter
    @JsonProperty("tls11")
    private boolean tls11;
    @Getter
    @Setter
    @JsonProperty("tls12")
    private boolean tls12;
    @Getter
    @Setter
    @JsonProperty("tls13")
    private boolean tls13;
}
