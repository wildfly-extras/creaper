package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public class AddChainedPrincipalTransformer extends AbstractAddPrincipalTransformer {

    private AddChainedPrincipalTransformer(Builder builder) {
        super(builder);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address aggregatePrincipalTransformerAddress = Address.subsystem("elytron")
                .and("chained-principal-transformer", name);
        if (replaceExisting) {
            ops.removeIfExists(aggregatePrincipalTransformerAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(aggregatePrincipalTransformerAddress, Values.empty()
                .andList(String.class, "principal-transformers", principalTransformers));
    }

    public static final class Builder extends AbstractAddPrincipalTransformer.Builder<Builder> {

        public Builder(String name) {
            super(name);
        }

        @Override
        public AddChainedPrincipalTransformer build() {
            if (principalTransformers.size() < 2) {
                throw new IllegalArgumentException("There must be at least two principal-transformers");
            }
            return new AddChainedPrincipalTransformer(this);
        }

    }
}
