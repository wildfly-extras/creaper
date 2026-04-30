package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddConcatenatingPrincipalDecoder extends AbstractAddPrincipalDecoder {

    private final String joiner;

    private AddConcatenatingPrincipalDecoder(Builder builder) {
        super(builder);
        this.joiner = builder.joiner;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address constantPrincipalDecoderAddress = Address.subsystem("elytron")
                .and("concatenating-principal-decoder", name);
        if (replaceExisting) {
            ops.removeIfExists(constantPrincipalDecoderAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(constantPrincipalDecoderAddress, Values.empty()
                .andList(String.class, "principal-decoders", principalDecoders)
                .andOptional("joiner", joiner));
    }

    public static final class Builder extends AbstractAddPrincipalDecoder.Builder<Builder> {

        private String joiner;

        public Builder(String name) {
            super(name);
        }

        public Builder joiner(String joiner) {
            if (joiner == null || joiner.isEmpty()) {
                throw new IllegalArgumentException("Joiner must not be null and must have a minimum length of 1 character");
            }
            this.joiner = joiner;
            return this;
        }

        @Override
        public AddConcatenatingPrincipalDecoder build() {
            if (principalDecoders.size() < 2) {
                throw new IllegalArgumentException("There must be at least two principal-decoders");
            }
            return new AddConcatenatingPrincipalDecoder(this);
        }
    }

}
