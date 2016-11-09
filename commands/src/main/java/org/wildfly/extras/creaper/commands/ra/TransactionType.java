package org.wildfly.extras.creaper.commands.ra;


public enum TransactionType {
    XA("XATransaction"),
    NONE("NoTransaction");

    private String value;

    TransactionType(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }
}
