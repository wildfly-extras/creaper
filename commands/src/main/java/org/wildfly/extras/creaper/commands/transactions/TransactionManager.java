package org.wildfly.extras.creaper.commands.transactions;

/**
 * Entrypoint for configuring transations subsystem.
 */
public final class TransactionManager {

    private TransactionManager() {}

    public static ChangeBasicTransactionAttributes.Builder basicAttributes() {
        return new ChangeBasicTransactionAttributes.Builder();
    }
}
