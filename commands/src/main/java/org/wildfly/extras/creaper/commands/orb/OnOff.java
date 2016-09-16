package org.wildfly.extras.creaper.commands.orb;

enum OnOff {
    ON(true, "on"),
    OFF(false, "off");

    final boolean openjdk;
    final String jacorb;

    OnOff(boolean openjdk, String jacorb) {
        this.openjdk = openjdk;
        this.jacorb = jacorb;
    }

    static OnOff get(boolean value) {
        return value ? OnOff.ON : OnOff.OFF;
    }
}
