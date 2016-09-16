package org.wildfly.extras.creaper.commands.orb;

public enum AuthValues {
    NONE("None"),
    SERVER_AUTH("ServerAuth"),
    CLIENT_AUTH("ClientAuth"),
    MUTUAL_AUTH("MutualAuth");

    final String value;

    AuthValues(String auth) {
        this.value = auth;
    }
}
