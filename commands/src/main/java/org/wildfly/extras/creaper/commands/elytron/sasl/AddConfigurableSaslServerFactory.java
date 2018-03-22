package org.wildfly.extras.creaper.commands.elytron.sasl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.commands.elytron.Property;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddConfigurableSaslServerFactory implements OnlineCommand {

    private final String name;
    private final String saslServerFactory;
    private final String protocol;
    private final String serverName;
    private final List<Filter> filters;
    private final List<Property> properties;
    private final boolean replaceExisting;

    private AddConfigurableSaslServerFactory(Builder builder) {
        this.name = builder.name;
        this.saslServerFactory = builder.saslServerFactory;
        this.protocol = builder.protocol;
        this.serverName = builder.serverName;
        this.filters = builder.filters;
        this.properties = builder.properties;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address factoryAddress = Address.subsystem("elytron")
                .and("configurable-sasl-server-factory", name);
        if (replaceExisting) {
            ops.removeIfExists(factoryAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        List<ModelNode> filterNodeList = null;
        if (filters != null && !filters.isEmpty()) {
            filterNodeList = new ArrayList<ModelNode>();
            for (Filter filter : filters) {
                ModelNode filterNode = new ModelNode();
                if (filter.getPatternFilter() != null) {
                    filterNode.add("pattern-filter", filter.getPatternFilter());
                }
                if (filter.getPredefinedFilter() != null) {
                    filterNode.add("predefined-filter", filter.getPredefinedFilter());
                }
                if (filter.getEnabling() != null) {
                    filterNode.add("enabling", filter.getEnabling());
                }
                filterNode = filterNode.asObject();
                filterNodeList.add(filterNode);
            }
        }

        ModelNode propertyNode = null;
        if (properties != null && !properties.isEmpty()) {
            propertyNode = new ModelNode();
            for (Property property : properties) {
                propertyNode.add(property.getKey(), property.getValue());
            }
            propertyNode = propertyNode.asObject();
        }

        ops.add(factoryAddress, Values.empty()
                .and("sasl-server-factory", saslServerFactory)
                .andOptional("protocol", protocol)
                .andOptional("server-name", serverName)
                .andListOptional(ModelNode.class, "filters", filterNodeList)
                .andOptional("properties", propertyNode));

    }

    public static final class Builder {

        private final String name;
        private String saslServerFactory;
        private String protocol;
        private String serverName;
        private List<Filter> filters = new ArrayList<Filter>();
        private List<Property> properties = new ArrayList<Property>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the configurable-sasl-server-factory must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the configurable-sasl-server-factory must not be empty value");
            }
            this.name = name;
        }

        public Builder saslServerFactory(String saslServerFactory) {
            this.saslServerFactory = saslServerFactory;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder addFilters(Filter... filters) {
            if (filters == null) {
                throw new IllegalArgumentException("Filters added to configurable-sasl-server-factory must not be null");
            }
            Collections.addAll(this.filters, filters);
            return this;
        }

        public Builder addProperties(Property... properties) {
            if (properties == null) {
                throw new IllegalArgumentException("Properties added to configurable-sasl-server-factory must not be null");
            }
            Collections.addAll(this.properties, properties);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddConfigurableSaslServerFactory build() {
            if (saslServerFactory == null || saslServerFactory.isEmpty()) {
                throw new IllegalArgumentException("sasl-server-factory must not be null and must include at least one entry");
            }
            return new AddConfigurableSaslServerFactory(this);
        }

    }

    public static final class Filter {

        private final String patternFilter;
        private final String predefinedFilter;
        private final Boolean enabling;

        private Filter(FilterBuilder builder) {
            this.patternFilter = builder.patternFilter;
            this.predefinedFilter = builder.predefinedFilter;
            this.enabling = builder.enabling;
        }

        public String getPatternFilter() {
            return patternFilter;
        }

        public String getPredefinedFilter() {
            return predefinedFilter;
        }

        public Boolean getEnabling() {
            return enabling;
        }

    }

    public static final class FilterBuilder {

        private String patternFilter;
        private String predefinedFilter;
        private Boolean enabling;

        public FilterBuilder patternFilter(String patternFilter) {
            this.patternFilter = patternFilter;
            return this;
        }

        public FilterBuilder predefinedFilter(String predefinedFilter) {
            this.predefinedFilter = predefinedFilter;
            return this;
        }

        public FilterBuilder enabling(Boolean enabling) {
            this.enabling = enabling;
            return this;
        }

        public Filter build() {
            if ((patternFilter == null || patternFilter.isEmpty())
                    && (predefinedFilter == null || predefinedFilter.isEmpty())) {
                throw new IllegalArgumentException("pattern-filter or predefined-filter must not be null or empty");
            }
            if (patternFilter != null && predefinedFilter != null) {
                throw new IllegalArgumentException("both pattern-filter and predefined-filter cannot be used");
            }
            return new Filter(this);
        }

    }

}
