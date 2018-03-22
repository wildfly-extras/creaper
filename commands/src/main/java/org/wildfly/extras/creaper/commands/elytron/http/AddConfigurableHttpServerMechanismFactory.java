package org.wildfly.extras.creaper.commands.elytron.http;

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

public final class AddConfigurableHttpServerMechanismFactory implements OnlineCommand {

    private final String name;
    private final String httpServerMechanismFactory;
    private final List<Filter> filters;
    private final List<Property> properties;
    private final boolean replaceExisting;

    private AddConfigurableHttpServerMechanismFactory(Builder builder) {
        this.name = builder.name;
        this.httpServerMechanismFactory = builder.httpServerMechanismFactory;
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
                .and("configurable-http-server-mechanism-factory", name);
        if (replaceExisting) {
            ops.removeIfExists(factoryAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        List<ModelNode> filterNodeList = null;
        if (filters != null && !filters.isEmpty()) {
            filterNodeList = new ArrayList<ModelNode>();
            for (Filter filter : filters) {
                ModelNode filterNode = new ModelNode();
                filterNode.add("pattern-filter", filter.getPatternFilter());
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
                .and("http-server-mechanism-factory", httpServerMechanismFactory)
                .andListOptional(ModelNode.class, "filters", filterNodeList)
                .andOptional("properties", propertyNode));

    }

    public static final class Builder {

        private final String name;
        private String httpServerMechanismFactory;
        private List<Filter> filters = new ArrayList<Filter>();
        private List<Property> properties = new ArrayList<Property>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the configurable-http-server-mechanism-factory must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the configurable-http-server-mechanism-factory must not be empty value");
            }
            this.name = name;
        }

        public Builder httpServerMechanismFactory(String httpServerMechanismFactory) {
            this.httpServerMechanismFactory = httpServerMechanismFactory;
            return this;
        }

        public Builder addFilters(Filter... filters) {
            if (filters == null) {
                throw new IllegalArgumentException("Filters added to configurable-http-server-mechanism-factory must not be null");
            }
            Collections.addAll(this.filters, filters);
            return this;
        }

        public Builder addProperties(Property... properties) {
            if (properties == null) {
                throw new IllegalArgumentException("Properties added to configurable-http-server-mechanism-factory must not be null");
            }
            Collections.addAll(this.properties, properties);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddConfigurableHttpServerMechanismFactory build() {
            if (httpServerMechanismFactory == null || httpServerMechanismFactory.isEmpty()) {
                throw new IllegalArgumentException("http-server-mechanism-factory must not be null and must include at least one entry");
            }
            return new AddConfigurableHttpServerMechanismFactory(this);
        }

    }

    public static final class Filter {

        private final String patternFilter;
        private final Boolean enabling;

        private Filter(FilterBuilder builder) {
            this.patternFilter = builder.patternFilter;
            this.enabling = builder.enabling;
        }

        public String getPatternFilter() {
            return patternFilter;
        }

        public Boolean getEnabling() {
            return enabling;
        }

    }

    public static final class FilterBuilder {

        private String patternFilter;
        private Boolean enabling;

        public FilterBuilder patternFilter(String patternFilter) {
            this.patternFilter = patternFilter;
            return this;
        }

        public FilterBuilder enabling(Boolean enabling) {
            this.enabling = enabling;
            return this;
        }

        public Filter build() {
            if (patternFilter == null || patternFilter.isEmpty()) {
                throw new IllegalArgumentException("pattern-filter must not be null or empty");
            }
            return new Filter(this);
        }

    }

}
