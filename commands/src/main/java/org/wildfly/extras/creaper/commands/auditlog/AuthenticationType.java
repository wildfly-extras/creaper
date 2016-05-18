package org.wildfly.extras.creaper.commands.auditlog;

public enum AuthenticationType {

    TRUSTSTORE("truststore"),
    CLIENT_CERTIFICATE_STORE("client-certificate-store");

    private final String value;

    AuthenticationType(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
