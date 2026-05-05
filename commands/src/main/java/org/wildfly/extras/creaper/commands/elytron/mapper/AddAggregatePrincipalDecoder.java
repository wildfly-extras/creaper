package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddAggregatePrincipalDecoder extends AbstractAddPrincipalDecoder {

    private AddAggregatePrincipalDecoder(Builder builder) {
        super(builder);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address constantPrincipalDecoderAddress = Address.subsystem("elytron")
                .and("aggregate-principal-decoder", name);
        if (replaceExisting) {
            ops.removeIfExists(constantPrincipalDecoderAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(constantPrincipalDecoderAddress, Values.empty()
                .andList(String.class, "principal-decoders", principalDecoders));
    }

    public static final class Builder extends AbstractAddPrincipalDecoder.Builder<Builder> {

        public Builder(String name) {
            super(name);
        }

        @Override
        public AddAggregatePrincipalDecoder build() {
            if (principalDecoders.size() < 2) {
                throw new IllegalArgumentException("There must be at least two principal-decoders");
            }
            return new AddAggregatePrincipalDecoder(this);
        }

    }

}
