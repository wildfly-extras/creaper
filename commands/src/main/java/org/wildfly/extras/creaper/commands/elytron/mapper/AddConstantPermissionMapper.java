package org.wildfly.extras.creaper.commands.elytron.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddConstantPermissionMapper implements OnlineCommand {

    private final String name;
    private final List<Permission> permissions;
    private final boolean replaceExisting;

    private AddConstantPermissionMapper(Builder builder) {
        this.name = builder.name;
        this.permissions = builder.permissions;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address mapperAddress = Address.subsystem("elytron").and("constant-permission-mapper", name);
        if (replaceExisting) {
            ops.removeIfExists(mapperAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        List<ModelNode> mechanismConfigurationsNodeList = null;
        if (permissions != null && !permissions.isEmpty()) {
            mechanismConfigurationsNodeList = new ArrayList<ModelNode>();
            for (Permission permission : permissions) {
                ModelNode node = new ModelNode();
                node.add("class-name", permission.getClassName());
                addOptionalToModelNode(node, "module", permission.getModule());
                addOptionalToModelNode(node, "target-name", permission.getTargetName());
                addOptionalToModelNode(node, "action", permission.getAction());
                node = node.asObject();
                mechanismConfigurationsNodeList.add(node);
            }
        }

        ops.add(mapperAddress, Values.empty()
                .andListOptional(ModelNode.class, "permissions", mechanismConfigurationsNodeList));
    }

    private void addOptionalToModelNode(ModelNode node, String name, String value) {
        if (value != null && !value.isEmpty()) {
            node.add(name, value);
        }
    }

    public static final class Builder {

        private final String name;
        private List<Permission> permissions = new ArrayList<Permission>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the constant-permission-mapper must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the constant-permission-mapper must not be empty value");
            }
            this.name = name;
        }

        public Builder addPermissions(Permission... permissions) {
            if (permissions == null) {
                throw new IllegalArgumentException("Permissions added to permission-mapping of constant-permission-mapper must not be null");
            }
            Collections.addAll(this.permissions, permissions);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddConstantPermissionMapper build() {
            return new AddConstantPermissionMapper(this);
        }
    }

    public static final class Permission {

        private final String className;
        private final String module;
        private final String targetName;
        private final String action;

        private Permission(PermissionBuilder builder) {
            this.className = builder.className;
            this.module = builder.module;
            this.targetName = builder.targetName;
            this.action = builder.action;
        }

        public String getClassName() {
            return className;
        }

        public String getModule() {
            return module;
        }

        public String getTargetName() {
            return targetName;
        }

        public String getAction() {
            return action;
        }

    }

    public static final class PermissionBuilder {

        private String className;
        private String module;
        private String targetName;
        private String action;

        public PermissionBuilder className(String className) {
            this.className = className;
            return this;
        }

        public PermissionBuilder module(String module) {
            this.module = module;
            return this;
        }

        public PermissionBuilder targetName(String targetName) {
            this.targetName = targetName;
            return this;
        }

        public PermissionBuilder action(String action) {
            this.action = action;
            return this;
        }

        public Permission build() {
            if (className == null || className.isEmpty()) {
                throw new IllegalArgumentException("class-name must not be null and must have a minimum length of 1 characters");
            }
            return new Permission(this);
        }
    }


}
