package org.wildfly.extras.creaper.commands.elytron.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.wildfly.extras.creaper.core.ServerVersion;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddX500AttributePrincipalDecoder implements OnlineCommand {

    private final String name;
    private final String oid;
    private final String attributeName;
    private final String joiner;
    private final Integer startSegment;
    private final Integer maximumSegments;
    private final Boolean reverse;
    private final Boolean convert;
    private final List<String> requiredOids;
    private final List<String> requiredAttributes;
    private final boolean replaceExisting;

    private AddX500AttributePrincipalDecoder(Builder builder) {
        this.name = builder.name;
        this.oid = builder.oid;
        this.attributeName = builder.attributeName;
        this.joiner = builder.joiner;
        this.startSegment = builder.startSegment;
        this.maximumSegments = builder.maximumSegments;
        this.reverse = builder.reverse;
        this.convert = builder.convert;
        this.requiredOids = builder.requiredOids;
        this.requiredAttributes = builder.requiredAttributes;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address x500AttributePrincipalDecoderAddress = Address.subsystem("elytron")
                .and("x500-attribute-principal-decoder", name);
        if (replaceExisting) {
            ops.removeIfExists(x500AttributePrincipalDecoderAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(x500AttributePrincipalDecoderAddress, Values.empty()
                .andOptional("oid", oid)
                .andOptional("attribute-name", attributeName)
                .andOptional("joiner", joiner)
                .andOptional("start-segment", startSegment)
                .andOptional("maximum-segments", maximumSegments)
                .andOptional("reverse", reverse)
                .andOptional("convert", convert)
                .andListOptional(String.class, "required-oids", requiredOids)
                .andListOptional(String.class, "required-attributes", requiredAttributes));

    }

    public static final class Builder {

        private final String name;
        private String oid;
        private String attributeName;
        private String joiner;
        private Integer startSegment;
        private Integer maximumSegments;
        private Boolean reverse;
        private Boolean convert;
        private List<String> requiredOids;
        private List<String> requiredAttributes;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the x500-attribute-principal-decoder must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the x500-attribute-principal-decoder must not be empty value");
            }
            this.name = name;
        }

        public Builder oid(String oid) {
            this.oid = oid;
            return this;
        }

        public Builder attributeName(String attributeName) {
            this.attributeName = attributeName;
            return this;
        }

        public Builder joiner(String joiner) {
            this.joiner = joiner;
            return this;
        }

        public Builder startSegment(int startSegment) {
            this.startSegment = startSegment;
            return this;
        }

        public Builder maximumSegments(int maximumSegments) {
            this.maximumSegments = maximumSegments;
            return this;
        }

        public Builder reverse(boolean reverse) {
            this.reverse = reverse;
            return this;
        }

        public Builder convert(boolean convert) {
            this.convert = convert;
            return this;
        }

        public Builder addRequiredOids(String... requiredOids) {
            if (requiredOids == null) {
                throw new IllegalArgumentException("Required OIDs added to x500-attribute-principal-decoder must not be null");
            }
            if (this.requiredOids == null) {
                this.requiredOids = new ArrayList<String>();
            }

            Collections.addAll(this.requiredOids, requiredOids);
            return this;
        }

        public Builder addRequiredAttributes(String... requiredAttributes) {
            if (requiredAttributes == null) {
                throw new IllegalArgumentException("Required attributes added to x500-attribute-principal-decoder must not be null");
            }
            if (this.requiredAttributes == null) {
                this.requiredAttributes = new ArrayList<String>();
            }

            Collections.addAll(this.requiredAttributes, requiredAttributes);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddX500AttributePrincipalDecoder build() {
            boolean isOidEmpty = oid == null || oid.isEmpty();
            boolean isAttributeNameEmpty = attributeName == null || attributeName.isEmpty();

            if ((isOidEmpty && isAttributeNameEmpty) || (!isOidEmpty && !isAttributeNameEmpty)) {
                throw new IllegalArgumentException("Exactly one of [oid, attribute-name] must be configured.");
            }

            return new AddX500AttributePrincipalDecoder(this);
        }
    }
}
