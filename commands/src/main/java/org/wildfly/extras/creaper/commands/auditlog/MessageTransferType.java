package org.wildfly.extras.creaper.commands.auditlog;

public enum MessageTransferType {

    NON_TRANSPARENT_FRAMING("NON_TRANSPARENT_FRAMING"),
    OCTET_COUNTING("OCTET_COUNTING");

    private final String value;

    MessageTransferType(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
