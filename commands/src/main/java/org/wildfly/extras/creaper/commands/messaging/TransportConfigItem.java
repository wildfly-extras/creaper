package org.wildfly.extras.creaper.commands.messaging;

enum TransportConfigItem {
    CONNECTOR("connector"),
    ACCEPTOR("acceptor");

    final String name;

    TransportConfigItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
