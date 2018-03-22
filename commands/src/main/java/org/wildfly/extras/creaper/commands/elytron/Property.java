package org.wildfly.extras.creaper.commands.elytron;

public final class Property {

    private final String key;
    private final String value;

    public Property(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}
