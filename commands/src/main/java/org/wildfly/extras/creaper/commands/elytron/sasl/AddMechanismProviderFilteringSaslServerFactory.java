package org.wildfly.extras.creaper.commands.elytron.sasl;

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

public final class AddMechanismProviderFilteringSaslServerFactory implements OnlineCommand {

    private final String name;
    private final String saslServerFactory;
    private final Boolean enabling;
    private final List<Filter> filters;
    private final boolean replaceExisting;

    private AddMechanismProviderFilteringSaslServerFactory(Builder builder) {
        this.name = builder.name;
        this.saslServerFactory = builder.saslServerFactory;
        this.enabling = builder.enabling;
        this.filters = builder.filters;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }
        Operations ops = new Operations(ctx.client);
        Address factoryAddress = Address.subsystem("elytron")
                .and("mechanism-provider-filtering-sasl-server-factory", name);
        if (replaceExisting) {
            ops.removeIfExists(factoryAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        List<ModelNode> filtersNodeList = null;
        if (filters != null && !filters.isEmpty()) {
            filtersNodeList = new ArrayList<ModelNode>();
            for (Filter filter : filters) {
                ModelNode filterModelNode = new ModelNode();
                filterModelNode.add("provider-name", filter.getProviderName());
                if (filter.getMechanismName() != null && !filter.getMechanismName().isEmpty()) {
                    filterModelNode.add("mechanism-name", filter.getMechanismName());
                }
                if (filter.getProviderVersion() != null) {
                    filterModelNode.add("provider-version", filter.getProviderVersion());
                }
                if (filter.getVersionComparison() != null) {
                    filterModelNode.add("version-comparison",
                            filter.getVersionComparison().getVersionComparisonName());
                }
                filterModelNode = filterModelNode.asObject();
                filtersNodeList.add(filterModelNode);
            }
        }

        ops.add(factoryAddress, Values.empty()
                .and("sasl-server-factory", saslServerFactory)
                .andOptional("enabling", enabling)
                .andListOptional(ModelNode.class, "filters", filtersNodeList));
    }

    public static final class Builder {

        private final String name;
        private String saslServerFactory;
        private Boolean enabling;
        private List<Filter> filters = new ArrayList<Filter>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the mechanism-provider-filtering-sasl-server-factory must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the mechanism-provider-filtering-sasl-server-factory must not be empty value");
            }
            this.name = name;
        }

        public Builder saslServerFactory(String saslServerFactory) {
            this.saslServerFactory = saslServerFactory;
            return this;
        }

        public Builder enabling(Boolean enabling) {
            this.enabling = enabling;
            return this;
        }

        public Builder addFilters(Filter... filters) {
            if (filters == null) {
                throw new IllegalArgumentException("Filter added to filters must not be null");
            }
            Collections.addAll(this.filters, filters);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddMechanismProviderFilteringSaslServerFactory build() {
            if (saslServerFactory == null || saslServerFactory.isEmpty()) {
                throw new IllegalArgumentException("sasl-server-factory must not be null and must include at least one entry");
            }
            return new AddMechanismProviderFilteringSaslServerFactory(this);
        }

    }

    public static final class Filter {

        private final String mechanismName;
        private final String providerName;
        private final Double providerVersion;
        private final VersionComparison versionComparison;

        private Filter(FilterBuilder builder) {
            this.mechanismName = builder.mechanismName;
            this.providerName = builder.providerName;
            this.providerVersion = builder.providerVersion;
            this.versionComparison = builder.versionComparison;
        }

        public String getMechanismName() {
            return mechanismName;
        }

        public String getProviderName() {
            return providerName;
        }

        public Double getProviderVersion() {
            return providerVersion;
        }

        public VersionComparison getVersionComparison() {
            return versionComparison;
        }

    }

    public static final class FilterBuilder {

        private String mechanismName;
        private String providerName;
        private Double providerVersion;
        private VersionComparison versionComparison;

        public FilterBuilder mechanismName(String mechanismName) {
            this.mechanismName = mechanismName;
            return this;
        }

        public FilterBuilder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public FilterBuilder providerVersion(Double providerVersion) {
            this.providerVersion = providerVersion;
            return this;
        }

        public FilterBuilder versionComparison(VersionComparison versionComparison) {
            this.versionComparison = versionComparison;
            return this;
        }

        public Filter build() {
            if (providerName == null || providerName.isEmpty()) {
                throw new IllegalArgumentException("provider-name must not be null or empty");
            }
            return new Filter(this);
        }

    }

    public enum VersionComparison {

        GREATER_THAN("greater-than"), LESS_THAN("less-than");

        private final String versionComparisonName;

        VersionComparison(String versionComparisonName) {
            this.versionComparisonName = versionComparisonName;
        }

        public String getVersionComparisonName() {
            return versionComparisonName;
        }
    }

}
