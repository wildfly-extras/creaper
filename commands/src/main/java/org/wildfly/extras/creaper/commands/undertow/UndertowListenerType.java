package org.wildfly.extras.creaper.commands.undertow;

public enum UndertowListenerType {
    HTTP_LISTENER("http-listener"),
    HTTPS_LISTENER("https-listener"),
    AJP_LISTENER("ajp-listener");

    private final String listenerTypeName;

    UndertowListenerType(String listenerTypeName) {
        this.listenerTypeName = listenerTypeName;
    }

    public String listenerTypeName() {
        return listenerTypeName;
    }
}
