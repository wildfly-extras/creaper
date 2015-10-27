package org.wildfly.extras.creaper.commands.modules;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;

/**
 * <p>The command is used to remove modules</p>
 *
 * <p>When a module is removed, its module.xml and other resources will be removed from the module repository
 * as well as its directory structure up to the point where other modules met.</p>
 */
public final class RemoveModule implements OnlineCommand {
    private final String moduleName;
    private final String slot;

    /**
     * @param moduleName the name of the module to be removed
     * @param slot       specifies a slot which should be removed. If this argument is not specified, "main" slot
     *                   is assumed
     */
    public RemoveModule(String moduleName, String slot) {
        this.moduleName = moduleName;
        this.slot = slot;
    }

    /**
     * @param moduleName the name of the module to be removed
     */
    public RemoveModule(String moduleName) {
        this(moduleName, AddModule.DEFAULT_SLOT);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.options.isDomain) {
            throw new CommandFailedException("RemoveModule command isn't supported in domain mode");
        }

        StringBuilder cmd = new StringBuilder("module remove");
        cmd.append(" --name=" + moduleName);
        cmd.append(" --slot=" + slot);
        ctx.client.executeCli(cmd.toString());
    }

    @Override
    public String toString() {
        return "RemoveModule " + moduleName;
    }
}
