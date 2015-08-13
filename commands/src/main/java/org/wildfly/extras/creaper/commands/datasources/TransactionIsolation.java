package org.wildfly.extras.creaper.commands.datasources;

public enum TransactionIsolation {
    TRANSACTION_READ_UNCOMMITTED("TRANSACTION_READ_UNCOMMITTED"),
    TRANSACTION_READ_COMMITTED("TRANSACTION_READ_COMMITTED"),
    TRANSACTION_REPEATABLE_READ("TRANSACTION_REPEATABLE_READ"),
    TRANSACTION_SERIALIZABLE("TRANSACTION_SERIALIZABLE"),
    TRANSACTION_NONE("TRANSACTION_NONE");

    private final String value;

    TransactionIsolation(final String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
