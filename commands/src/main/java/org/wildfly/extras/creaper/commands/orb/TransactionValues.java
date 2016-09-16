package org.wildfly.extras.creaper.commands.orb;

public enum TransactionValues {
    ON("full", "on"),
    OFF("none", "off"),
    SPEC("spec", "spec");

    final String openjdk;
    final String jacorb;

    TransactionValues(String openjdk, String jacorb) {
        this.openjdk = openjdk;
        this.jacorb = jacorb;
    }
}
