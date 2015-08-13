package org.wildfly.extras.creaper.core;

/**
 * A marker interface that is implemented both by
 * {@link org.wildfly.extras.creaper.core.online.OnlineCommand OnlineCommand} and
 * {@link org.wildfly.extras.creaper.core.offline.OfflineCommand OfflineCommand}. Useful when you want to defer
 * type checking from compile time to run time. This is generally discouraged, but there are valid use cases.
 */
public interface Command {
}
