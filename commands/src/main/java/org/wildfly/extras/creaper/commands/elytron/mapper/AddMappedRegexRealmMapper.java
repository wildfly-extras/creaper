package org.wildfly.extras.creaper.commands.elytron.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddMappedRegexRealmMapper implements OnlineCommand {

    private final String name;
    private final String pattern;
    private final String delegateRealmMapper;
    private final List<RealmMapping> realmMappings;
    private final boolean replaceExisting;

    private AddMappedRegexRealmMapper(Builder builder) {
        this.name = builder.name;
        this.pattern = builder.pattern;
        this.delegateRealmMapper = builder.delegateRealmMapper;
        this.realmMappings = builder.realmMappings;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address mapperAddress = Address.subsystem("elytron").and("mapped-regex-realm-mapper", name);
        if (replaceExisting) {
            ops.removeIfExists(mapperAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ModelNode realmMapNode = new ModelNode();
        for (RealmMapping realmMapping : realmMappings) {
            realmMapNode.add(realmMapping.getFrom(), realmMapping.getTo());
        }

        ops.add(mapperAddress, Values.empty()
                .and("pattern", pattern)
                .andOptional("delegate-realm-mapper", delegateRealmMapper)
                .and("realm-map", realmMapNode.asObject()));
    }

    public static final class Builder {

        private String name;
        private String pattern;
        private String delegateRealmMapper;
        private List<RealmMapping> realmMappings = new ArrayList<RealmMapping>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the mapped-regex-realm-mapper must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the mapped-regex-realm-mapper must not be empty value");
            }
            this.name = name;
        }

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder delegateRealmMapper(String delegateRealmMapper) {
            this.delegateRealmMapper = delegateRealmMapper;
            return this;
        }

        public Builder addRealmMappings(RealmMapping... realmMapping) {
            if (realmMapping == null) {
                throw new IllegalArgumentException("RealmMapping added to mapped-regex-realm-mapper must not be null");
            }
            Collections.addAll(this.realmMappings, realmMapping);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddMappedRegexRealmMapper build() {
            if (pattern == null || pattern.isEmpty()) {
                throw new IllegalArgumentException("pattern must not be null or empty");
            }
            if (realmMappings == null || realmMappings.isEmpty()) {
                throw new IllegalArgumentException("realm-mapping must not be null and must include at least one entry");
            }
            return new AddMappedRegexRealmMapper(this);
        }
    }

    public static final class RealmMapping {

        private final String from;
        private final String to;

        public RealmMapping(String from, String to) {
            if (from == null) {
                throw new IllegalArgumentException("from must not be null");
            }
            if (to == null) {
                throw new IllegalArgumentException("to must not be null");
            }
            this.from = from;
            this.to = to;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

    }

}
