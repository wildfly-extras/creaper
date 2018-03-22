package org.wildfly.extras.creaper.commands.elytron.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.wildfly.extras.creaper.core.online.OnlineCommand;

abstract class AbstractAddPrincipalTransformer implements OnlineCommand {
    protected final String name;
    protected final List<String> principalTransformers;
    protected final boolean replaceExisting;

    protected AbstractAddPrincipalTransformer(Builder builder) {
        this.name = builder.name;
        this.principalTransformers = builder.principalTransformers;
        this.replaceExisting = builder.replaceExisting;
    }

    abstract static class Builder<THIS extends Builder> {

        protected final String name;
        protected final List<String> principalTransformers = new ArrayList<String>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the name-decoder must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the name-decoder must not be empty value");
            }
            this.name = name;
        }

        /**
         * Sets principal transformers that should be aggregated/chained. At least 2 principal transformers must be
         * defined. It is possible to use following types as a principal transformer:
         * <ul>
         * <li>aggregate-principal-transformer</li>
         * <li>chained-principal-transformer</li>
         * <li>constant-principal-transformer</li>
         * <li>custom-principal-transformer</li>
         * <li>regex-principal-transformer</li>
         * <li>regex-validating-principal-transformer</li>
         * </ul>
         *
         * @param principalTransformers previously defined principal-decoder
         * @return builder
         */
        public final THIS principalTransformers(String... principalTransformers) {
            if (principalTransformers == null) {
                throw new IllegalArgumentException("Added principal transformer must not be null");
            }
            Collections.addAll(this.principalTransformers, principalTransformers);
            return (THIS) this;
        }

        public final THIS replaceExisting() {
            this.replaceExisting = true;
            return (THIS) this;
        }

        public abstract AbstractAddPrincipalTransformer build();
    }
}
