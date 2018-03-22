package org.wildfly.extras.creaper.commands.elytron.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.wildfly.extras.creaper.core.online.OnlineCommand;

abstract class AbstractAddPrincipalDecoder implements OnlineCommand {

    protected final String name;
    protected final List<String> principalDecoders;
    protected final boolean replaceExisting;

    protected AbstractAddPrincipalDecoder(Builder builder) {
        this.name = builder.name;
        this.principalDecoders = builder.principalDecoders;
        this.replaceExisting = builder.replaceExisting;
    }

    abstract static class Builder<THIS extends Builder> {

        protected final String name;
        protected final List<String> principalDecoders = new ArrayList<String>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the aggregate-principal-decoder must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the aggregate-principal-decoder must not be empty value");
            }
            this.name = name;
        }

        /**
         * Sets principal decoders that should be aggregated/concatenated. At least 2 principal decoders
         * must be defined. It is possible to use following types as a principal decoder:
         * <ul>
         * <li>customPrincipalDecoderType</li>
         * <li>aggregate-principal-decoder</li>
         * <li>concatenating-principal-decoder</li>
         * <li>constant-principal-decoder</li>
         * <li>x500-attribute-principal-decoder</li>
         * </ul>
         *
         * @param principalDecoders previously defined principal-decoder
         * @return builder
         */
        public final THIS principalDecoders(String... principalDecoders) {
            if (principalDecoders == null) {
                throw new IllegalArgumentException("Principal decoder added to aggregate-principal-decoder must not be null");
            }
            Collections.addAll(this.principalDecoders, principalDecoders);
            return (THIS) this;
        }

        public final THIS replaceExisting() {
            this.replaceExisting = true;
            return (THIS) this;
        }

        public abstract AbstractAddPrincipalDecoder build();
    }

}
