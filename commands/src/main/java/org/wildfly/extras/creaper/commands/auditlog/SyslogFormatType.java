package org.wildfly.extras.creaper.commands.auditlog;

public enum SyslogFormatType {

    RFC5424("RFC5424"),
    RFC3164("RFC3164");

    private final String value;

    SyslogFormatType(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }

}
