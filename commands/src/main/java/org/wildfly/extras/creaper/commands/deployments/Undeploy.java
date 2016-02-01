package org.wildfly.extras.creaper.commands.deployments;


import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;

/**
 * Command which takes care about undeploying specified application (the content is by default also removed)
 * <p>
 * In case of domain the deployment is by default undeployed from all relevant server groups
 * (server groups having the deployment enabled)
 * </p>
 */
public final class Undeploy implements OnlineCommand {
    private final String deploymentName;
    private final boolean keepContent;

    private Undeploy(Builder builder) {
        this.deploymentName = builder.deploymentName;
        this.keepContent = builder.keepContent;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        StringBuilder cmd = new StringBuilder("undeploy ").append(deploymentName);
        if (keepContent) {
            cmd.append(" --keep-content");
        }
        if (ctx.options.isDomain) {
            cmd.append(" --all-relevant-server-groups");
        }
        ctx.client.executeCli(cmd.toString());
    }

    public static final class Builder {
        private String deploymentName;
        private boolean keepContent = false;

        public Builder(String deploymentName) {
            this.deploymentName = deploymentName;
        }

        /**
         * Defines that the content should be left in the deployment repository => only undeployed without removing it.
         */
        public Builder keepContent() {
            this.keepContent = true;
            return this;
        }


        public Undeploy build() {
            return new Undeploy(this);
        }
    }
}
