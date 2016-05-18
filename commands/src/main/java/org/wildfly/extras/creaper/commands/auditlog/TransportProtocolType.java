package org.wildfly.extras.creaper.commands.auditlog;

enum TransportProtocolType {

    UDP("udp"),
    TCP("tcp"),
    TLS("tls");

    private final String value;

    TransportProtocolType(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
