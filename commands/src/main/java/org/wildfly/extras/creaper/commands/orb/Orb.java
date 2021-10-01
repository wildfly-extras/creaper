package org.wildfly.extras.creaper.commands.orb;

/**
 * Creaper commands entry point for configuring orb subsystem.
 */
public final class Orb {

    private Orb() {
    }

    public static ChangeOrb.Builder attributes() {
        return new ChangeOrb.Builder();
    }
}
