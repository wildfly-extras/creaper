package org.wildfly.extras.creaper.commands.transactions;

/**
 * Entry point for configuring transactions subsystem.
 */
public final class TransactionManager {

    private TransactionManager() {
    }

    /**
     * If changing something is not needed, do not set it (it won't be changed)
     */
    public static ChangeBasicTransactionAttributes.Builder basicAttributes() {
        return new ChangeBasicTransactionAttributes.Builder();
    }

    /** If changing something is not needed, do not set it (it won't be changed). */
    public static ChangeJdbcTransactionAttributes.Builder jdbc() {
        return new ChangeJdbcTransactionAttributes.Builder();
    }
}
