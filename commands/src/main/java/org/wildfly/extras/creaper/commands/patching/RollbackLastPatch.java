package org.wildfly.extras.creaper.commands.patching;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command for last patch rollback.
 */
public final class RollbackLastPatch implements OnlineCommand {
    private final Boolean resetConfiguration;
    private final Boolean overrideAll;
    private final Boolean overrideModules;
    private final List<String> overridePaths;
    private final List<String> preservePaths;

    private RollbackLastPatch(Builder builder) {
        this.resetConfiguration = builder.resetConfiguration;
        this.overrideAll = builder.overrideAll;
        this.overrideModules = builder.overrideModules;
        this.overridePaths = builder.overridePaths;
        this.preservePaths = builder.preservePaths;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException {
        Operations ops = new Operations(ctx.client);
        ops.invoke("rollback-last", Address.coreService("patching"), Values.empty()
                .andOptional("reset-configuration", resetConfiguration)
                .andOptional("override-all", overrideAll)
                .andOptional("override-modules", overrideModules)
                .andListOptional(String.class, "override", overridePaths)
                .andListOptional(String.class, "preserve", preservePaths));
    }

    public static final class Builder {
        private Boolean resetConfiguration;
        private Boolean overrideAll;
        private Boolean overrideModules;
        private List<String> overridePaths;
        private List<String> preservePaths;

        public Builder() {
        }

        /**
         * Sets whether the rollback operation should reset the installation configurations to their state
         * before the patch was applied.
         */
        public Builder resetConfiguration(boolean resetConfiguration) {
            this.resetConfiguration = resetConfiguration;
            return this;
        }

        /**
         * Sets whether all conflicts should be automatically resolved by overriding.
         */
        public Builder overrideAll(boolean overrideAll) {
            this.overrideAll = overrideAll;
            return this;
        }

        /**
         * Sets whether modules shall be overridden when there is conflict in the module.
         */
        public Builder overrideModules(boolean overrideModules) {
            this.overrideModules = overrideModules;
            return this;
        }

        /**
         * Adds specified paths to the list of paths which shall be overridden.
         * For more details, see the {@code --override} option of the {@code patch apply} CLI command.
         */
        public Builder overridePaths(String... pathsToOverride) {
            if (this.overridePaths == null && pathsToOverride != null) {
                this.overridePaths = new ArrayList<String>();
            }
            if (pathsToOverride != null) {
                this.overridePaths.addAll(Arrays.asList(pathsToOverride));
            }
            return this;
        }

        /**
         * Adds paths to list of paths which should be preserved.
         * For more details, see the {@code --preserve} option of the {@code patch apply} CLI command.
         */
        public Builder preservePaths(String... pathsToPreserve) {
            if (this.preservePaths == null && pathsToPreserve != null) {
                this.preservePaths = new ArrayList<String>();
            }
            if (pathsToPreserve != null) {
                this.preservePaths.addAll(Arrays.asList(pathsToPreserve));
            }
            return this;
        }

        public RollbackLastPatch build() {
            return new RollbackLastPatch(this);
        }
    }
}
