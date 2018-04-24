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

public final class AddSimplePermissionMapper implements OnlineCommand {

    private final String name;
    private final MappingMode mappingMode;
    private final List<PermissionMapping> permissionMappings;
    private final boolean replaceExisting;

    private AddSimplePermissionMapper(Builder builder) {
        this.name = builder.name;
        this.mappingMode = builder.mappingMode;
        this.permissionMappings = builder.permissionMappings;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address mapperAddress = Address.subsystem("elytron").and("simple-permission-mapper", name);
        if (replaceExisting) {
            ops.removeIfExists(mapperAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        List<ModelNode> permissionMappingsModelNodeList = null;
        if (permissionMappings != null && !permissionMappings.isEmpty()) {
            permissionMappingsModelNodeList = new ArrayList<ModelNode>();
            for (PermissionMapping mapping : permissionMappings) {
                ModelNode configNode = new ModelNode();
                if (mapping.getMatchAll() != null) {
                    configNode.add("match-all", mapping.getMatchAll());
                }
                if (mapping.getRoles() != null && !mapping.getRoles().isEmpty()) {
                    ModelNode rolesList = new ModelNode().setEmptyList();
                    for (String role : mapping.getRoles()) {
                        rolesList.add(role);
                    }
                    configNode.add("roles", rolesList);
                }
                if (mapping.getPrincipals() != null && !mapping.getPrincipals().isEmpty()) {
                    ModelNode principalsList = new ModelNode().setEmptyList();
                    for (String principal : mapping.getPrincipals()) {
                        principalsList.add(principal);
                    }
                    configNode.add("principals", principalsList);
                }
                if (mapping.getPermissions() != null && !mapping.getPermissions().isEmpty()) {
                    ModelNode permissionsModelNodeList = new ModelNode().setEmptyList();
                    for (Permission permission : mapping.getPermissions()) {
                        ModelNode permissionNode = new ModelNode()
                                .add("class-name", permission.getClassName());
                        if (permission.getAction() != null && !permission.getAction().isEmpty()) {
                            permissionNode.add("action", permission.getAction());
                        }
                        if (permission.getModule() != null && !permission.getModule().isEmpty()) {
                            permissionNode.add("module", permission.getModule());
                        }
                        if (permission.getTargetName() != null && !permission.getTargetName().isEmpty()) {
                            permissionNode.add("target-name", permission.getTargetName());
                        }
                        permissionNode = permissionNode.asObject();
                        permissionsModelNodeList.add(permissionNode);
                    }
                    configNode.add("permissions", permissionsModelNodeList);
                }
                if (mapping.getPermissionSets() != null && !mapping.getPermissionSets().isEmpty()) {
                    if (ctx.version.lessThan(ServerVersion.VERSION_7_0_0)) {
                        throw new AssertionError("permission-set is available since WildFly 13.");
                    }
                    ModelNode permissionSetModelNodeList = new ModelNode().setEmptyList();
                    for (String permissionSet : mapping.getPermissionSets()) {
                        ModelNode permissionSetNode = new ModelNode()
                                .add("permission-set", permissionSet);
                        permissionSetNode = permissionSetNode.asObject();
                        permissionSetModelNodeList.add(permissionSetNode);
                    }
                    configNode.add("permission-sets", permissionSetModelNodeList);
                }
                configNode = configNode.asObject();
                permissionMappingsModelNodeList.add(configNode);
            }
        }

        String mappingModeValue = mappingMode == null ? null : mappingMode.name();

        ops.add(mapperAddress, Values.empty()
                .andOptional("mapping-mode", mappingModeValue)
                .andListOptional(ModelNode.class, "permission-mappings", permissionMappingsModelNodeList));
    }


    public static final class Builder {

        private final String name;
        private MappingMode mappingMode;
        private List<PermissionMapping> permissionMappings = new ArrayList<PermissionMapping>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the simple-permission-mapper must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the simple-permission-mapper must not be empty value");
            }
            this.name = name;
        }

        public Builder mappingMode(MappingMode mappingMode) {
            this.mappingMode = mappingMode;
            return this;
        }

        public Builder addPermissionMappings(PermissionMapping... permissionMappings) {
            if (permissionMappings == null) {
                throw new IllegalArgumentException("PermissionMapping added to simple-permission-mapper must not be null");
            }
            Collections.addAll(this.permissionMappings, permissionMappings);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddSimplePermissionMapper build() {
            return new AddSimplePermissionMapper(this);
        }

    }

    public static final class PermissionMapping {

        private final Boolean matchAll;
        private final List<String> roles;
        private final List<String> principals;
        private final List<Permission> permissions;
        private final List<String> permissionSets;

        private PermissionMapping(PermissionMappingBuilder builder) {
            this.matchAll = builder.matchAll;
            this.roles = builder.roles;
            this.principals = builder.principals;
            this.permissions = builder.permissions;
            this.permissionSets = builder.permissionSets;
        }

        public Boolean getMatchAll() {
            return matchAll;
        }

        public List<String> getRoles() {
            return roles;
        }

        public List<String> getPrincipals() {
            return principals;
        }

        public List<Permission> getPermissions() {
            return permissions;
        }

        public List<String> getPermissionSets() {
            return permissionSets;
        }

    }

    public static final class PermissionMappingBuilder {

        private Boolean matchAll;
        private List<String> roles = new ArrayList<String>();
        private List<String> principals = new ArrayList<String>();
        private List<Permission> permissions = new ArrayList<Permission>();
        private List<String> permissionSets = new ArrayList<String>();

        public PermissionMappingBuilder matchAll(Boolean matchAll) {
            this.matchAll = matchAll;
            return this;
        }

        public PermissionMappingBuilder addRoles(String... roles) {
            if (roles == null) {
                throw new IllegalArgumentException("Roles added to permission-mapping of simple-permission-mapper must not be null");
            }
            Collections.addAll(this.roles, roles);
            return this;
        }

        public PermissionMappingBuilder addPrincipals(String... principals) {
            if (principals == null) {
                throw new IllegalArgumentException("Principals added to permission-mapping of simple-permission-mapper must not be null");
            }
            Collections.addAll(this.principals, principals);
            return this;
        }

        public PermissionMappingBuilder addPermissions(Permission... permissions) {
            if (permissions == null) {
                throw new IllegalArgumentException("Permissions added to permission-mapping of simple-permission-mapper must not be null");
            }
            Collections.addAll(this.permissions, permissions);
            return this;
        }

        public PermissionMappingBuilder addPermissionSets(String... permissionSets) {
            if (permissionSets == null) {
                throw new IllegalArgumentException("Permission sets added to permission-mapping of simple-permission-mapper must not be null");
            }
            Collections.addAll(this.permissionSets, permissionSets);
            return this;
        }

        public PermissionMapping build() {
            if (matchAll != null && !principals.isEmpty()) {
                throw new IllegalArgumentException("Only one of principal and match-all can be used.");
            }
            if (matchAll != null && !roles.isEmpty()) {
                throw new IllegalArgumentException("Only one of roles and match-all can be used.");
            }
            if (!permissions.isEmpty() && !permissionSets.isEmpty()) {
                throw new IllegalArgumentException("Only one of permissions and permission-sets can be used.");
            }
            return new PermissionMapping(this);
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

    public static enum MappingMode {

        AND, FIRST, OR, UNLESS, XOR
    }


}
