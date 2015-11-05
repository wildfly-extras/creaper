package org.wildfly.extras.creaper.commands.modules;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;

/**
 * <p>Removes a module (in the JBoss Modules sense) from the module repository. The {@code module.xml} file
 * and other resources will be removed from the module repository as well as the directory structure up to the point
 * where other modules meet.</p>
 */
public final class RemoveModule implements OnlineCommand {
    private final String moduleName;
    private final String slot;

    /**
     * @param moduleName the name of the module to be removed (assumes the {@code main} slot)
     */
    public RemoveModule(String moduleName) {
        this(moduleName, AddModule.DEFAULT_SLOT);
    }

    /**
     * @param moduleName the name of the module to be removed
     * @param slot       specifies a slot which should be removed
     */
    public RemoveModule(String moduleName, String slot) {
        if (moduleName == null) {
            throw new IllegalArgumentException("moduleName cannot be null");
        }
        if (slot == null) {
            throw new IllegalArgumentException("slot cannot be null");
        }

        this.moduleName = moduleName;
        this.slot = slot;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.options.isDomain) {
            throw new CommandFailedException("RemoveModule command isn't supported in domain mode");
        }

        ctx.client.executeCli("module remove --name=" + moduleName + " --slot=" + slot);
    }

    @Override
    public String toString() {
        return "RemoveModule " + moduleName;
    }
}
