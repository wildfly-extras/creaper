package org.wildfly.extras.creaper.commands.orb;

public enum SecurityValues {
    IDENTITY("identity", "identity"),
    CLIENT("client", "client"),
    NONE("none", "off");

    final String openjdk;
    final String jacorb;

    SecurityValues(String openjdk, String jacorb) {
        this.openjdk = openjdk;
        this.jacorb = jacorb;
    }
}
