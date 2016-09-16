package org.wildfly.extras.creaper.commands.orb;

public enum SupportedValues {
    NONE("none"),
    SUPPORTED("supported"),
    REQUIRED("required");

    final String value;

    SupportedValues(String value) {
        this.value = value;
    }
}
