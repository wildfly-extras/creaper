package org.wildfly.extras.creaper.commands.elytron.realm;

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

public final class AddJdbcRealm implements OnlineCommand {

    private final String name;
    private final List<PrincipalQuery> principalQueries;
    private final boolean replaceExisting;

    private AddJdbcRealm(Builder builder) {
        this.name = builder.name;
        this.replaceExisting = builder.replaceExisting;
        this.principalQueries = builder.principalQueries;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address jdbcRealmAddress = Address.subsystem("elytron").and("jdbc-realm", name);
        if (replaceExisting) {
            ops.removeIfExists(jdbcRealmAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        List<ModelNode> principalQueryNodeList = new ArrayList<ModelNode>();
        for (PrincipalQuery principalQuery : principalQueries) {
            ModelNode principalQueryNode = new ModelNode();
            principalQueryNode.add("sql", principalQuery.getSql());
            principalQueryNode.add("data-source", principalQuery.getDataSource());

            if (principalQuery.getClearPasswordMapper() != null) {
                ModelNode mapperNode = new ModelNode();
                mapperNode.add("password-index", principalQuery.getClearPasswordMapper().getPasswordIndex());
                mapperNode = mapperNode.asObject();
                principalQueryNode.add("clear-password-mapper", mapperNode);
            }
            if (principalQuery.getBcryptMapper() != null) {
                ModelNode mapperNode = new ModelNode();
                mapperNode.add("password-index", principalQuery.getBcryptMapper().getPasswordIndex());
                mapperNode.add("salt-index", principalQuery.getBcryptMapper().getSaltIndex());
                mapperNode.add("iteration-count-index", principalQuery.getBcryptMapper().getIterationCountIndex());
                mapperNode = mapperNode.asObject();
                principalQueryNode.add("bcrypt-mapper", mapperNode);
            }
            if (principalQuery.getSimpleDigestMapper() != null) {
                ModelNode mapperNode = new ModelNode();
                mapperNode.add("password-index", principalQuery.getSimpleDigestMapper().getPasswordIndex());
                mapperNode.add("algorithm", principalQuery.getSimpleDigestMapper().getAlgorithm());
                mapperNode = mapperNode.asObject();
                principalQueryNode.add("simple-digest-mapper", mapperNode);
            }
            if (principalQuery.getSaltedSimpleDigestMapper() != null) {
                ModelNode mapperNode = new ModelNode();
                mapperNode.add("password-index", principalQuery.getSaltedSimpleDigestMapper().getPasswordIndex());
                mapperNode.add("salt-index", principalQuery.getSaltedSimpleDigestMapper().getSaltIndex());
                mapperNode.add("algorithm", principalQuery.getSaltedSimpleDigestMapper().getAlgorithm());
                mapperNode = mapperNode.asObject();
                principalQueryNode.add("salted-simple-digest-mapper", mapperNode);
            }
            if (principalQuery.getScramMapper() != null) {
                ModelNode mapperNode = new ModelNode();
                mapperNode.add("password-index", principalQuery.getScramMapper().getPasswordIndex());
                mapperNode.add("salt-index", principalQuery.getScramMapper().getSaltIndex());
                mapperNode.add("iteration-count-index", principalQuery.getScramMapper().getIterationCountIndex());
                mapperNode.add("algorithm", principalQuery.getScramMapper().getAlgorithm());
                mapperNode = mapperNode.asObject();
                principalQueryNode.add("scram-mapper", mapperNode);
            }

            if (principalQuery.getAttributeMapping() != null && !principalQuery.getAttributeMapping().isEmpty()) {
                ModelNode attributeMappingNodeList = new ModelNode().setEmptyList();
                for (AttributeMapping attributeMapping : principalQuery.getAttributeMapping()) {
                    ModelNode attributeMappingNode = new ModelNode();
                    attributeMappingNode.add("index", attributeMapping.getIndex());
                    attributeMappingNode.add("to", attributeMapping.getTo());
                    attributeMappingNode = attributeMappingNode.asObject();
                    attributeMappingNodeList.add(attributeMappingNode);
                }
                principalQueryNode.add("attribute-mapping", attributeMappingNodeList);
            }

            principalQueryNode = principalQueryNode.asObject();
            principalQueryNodeList.add(principalQueryNode);
        }

        ops.add(jdbcRealmAddress, Values.empty()
                .andList(ModelNode.class, "principal-query", principalQueryNodeList));
    }

    public static final class Builder {

        private final String name;
        private List<PrincipalQuery> principalQueries;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the jdbc-realm must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the jdbc-realm must not be empty value");
            }
            this.name = name;
        }

        public Builder principalQueries(PrincipalQuery... principalQueries) {
            if (principalQueries == null) {
                throw new IllegalArgumentException("Principal queries added to jdbc-realm must not be null");
            }
            if (this.principalQueries == null) {
                this.principalQueries = new ArrayList<PrincipalQuery>();
            }
            Collections.addAll(this.principalQueries, principalQueries);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddJdbcRealm build() {
            if (principalQueries == null || principalQueries.isEmpty()) {
                throw new IllegalArgumentException("Principal queries must not be null and must include at least one entry");
            }
            return new AddJdbcRealm(this);
        }

    }

    public static final class PrincipalQuery {

        private final String sql;
        private final String dataSource;
        private final List<AttributeMapping> attributeMapping;
        private final ClearPasswordMapper clearPasswordMapper;
        private final BcryptMapper bcryptMapper;
        private final SimpleDigestMapper simpleDigestMapper;
        private final SaltedSimpleDigestMapper saltedSimpleDigestMapper;
        private final ScramMapper scramMapper;

        private PrincipalQuery(PrincipalQueryBuilder builder) {
            this.sql = builder.sql;
            this.dataSource = builder.dataSource;
            this.attributeMapping = builder.attributeMapping;
            this.clearPasswordMapper = builder.clearPasswordMapper;
            this.bcryptMapper = builder.bcryptMapper;
            this.simpleDigestMapper = builder.simpleDigestMapper;
            this.saltedSimpleDigestMapper = builder.saltedSimpleDigestMapper;
            this.scramMapper = builder.scramMapper;
        }

        public String getSql() {
            return sql;
        }

        public String getDataSource() {
            return dataSource;
        }

        public List<AttributeMapping> getAttributeMapping() {
            return attributeMapping;
        }

        public ClearPasswordMapper getClearPasswordMapper() {
            return clearPasswordMapper;
        }

        public BcryptMapper getBcryptMapper() {
            return bcryptMapper;
        }

        public SimpleDigestMapper getSimpleDigestMapper() {
            return simpleDigestMapper;
        }

        public SaltedSimpleDigestMapper getSaltedSimpleDigestMapper() {
            return saltedSimpleDigestMapper;
        }

        public ScramMapper getScramMapper() {
            return scramMapper;
        }

    }

    public static final class PrincipalQueryBuilder {

        private String sql;
        private String dataSource;
        private List<AttributeMapping> attributeMapping;
        private ClearPasswordMapper clearPasswordMapper;
        private BcryptMapper bcryptMapper;
        private SimpleDigestMapper simpleDigestMapper;
        private SaltedSimpleDigestMapper saltedSimpleDigestMapper;
        private ScramMapper scramMapper;

        public PrincipalQueryBuilder sql(String sql) {
            this.sql = sql;
            return this;
        }

        public PrincipalQueryBuilder dataSource(String dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public PrincipalQueryBuilder attributeMapping(AttributeMapping... attributeMapping) {
            if (attributeMapping == null) {
                throw new IllegalArgumentException("Attribute mappings added to principal-query must not be null");
            }
            if (this.attributeMapping == null) {
                this.attributeMapping = new ArrayList<AttributeMapping>();
            }
            Collections.addAll(this.attributeMapping, attributeMapping);
            return this;
        }

        public PrincipalQueryBuilder clearPasswordMapper(ClearPasswordMapper clearPasswordMapper) {
            this.clearPasswordMapper = clearPasswordMapper;
            return this;
        }

        public PrincipalQueryBuilder bcryptMapper(BcryptMapper bcryptMapper) {
            this.bcryptMapper = bcryptMapper;
            return this;
        }

        public PrincipalQueryBuilder simpleDigestMapper(SimpleDigestMapper simpleDigestMapper) {
            this.simpleDigestMapper = simpleDigestMapper;
            return this;
        }

        public PrincipalQueryBuilder saltedSimpleDigestMapper(SaltedSimpleDigestMapper saltedSimpleDigestMapper) {
            this.saltedSimpleDigestMapper = saltedSimpleDigestMapper;
            return this;
        }

        public PrincipalQueryBuilder scramMapper(ScramMapper scramMapper) {
            this.scramMapper = scramMapper;
            return this;
        }

        public PrincipalQuery build() {

            if (sql == null || sql.isEmpty()) {
                throw new IllegalArgumentException("sql must not be null or empty");
            }

            if (dataSource == null || dataSource.isEmpty()) {
                throw new IllegalArgumentException("Data source must not be null or empty");
            }

            return new PrincipalQuery(this);
        }

    }

    public static final class AttributeMapping {

        private final Integer index;
        private final String to;

        private AttributeMapping(AttributeMappingBuilder builder) {
            this.index = builder.index;
            this.to = builder.to;
        }

        public Integer getIndex() {
            return index;
        }

        public String getTo() {
            return to;
        }
    }

    public static final class AttributeMappingBuilder {

        private Integer index;
        private String to;

        public AttributeMappingBuilder index(Integer index) {
            this.index = index;
            return this;
        }

        public AttributeMappingBuilder to(String to) {
            this.to = to;
            return this;
        }

        public AttributeMapping build() {
            if (index == null) {
                throw new IllegalArgumentException("Index of the attribute-mapping must be specified as non null value");
            }
            if (to == null || to.isEmpty()) {
                throw new IllegalArgumentException("Attribute to of the attribute-mapping must be specified as non empty value");
            }
            return new AttributeMapping(this);
        }
    }

    public static final class ClearPasswordMapper {

        private final Integer passwordIndex;

        private ClearPasswordMapper(ClearPasswordMapperBuilder builder) {
            this.passwordIndex = builder.passwordIndex;
        }

        public Integer getPasswordIndex() {
            return passwordIndex;
        }
    }

    public static final class ClearPasswordMapperBuilder {

        private Integer passwordIndex;

        public ClearPasswordMapperBuilder passwordIndex(Integer passwordIndex) {
            this.passwordIndex = passwordIndex;
            return this;
        }

        public ClearPasswordMapper build() {
            if (passwordIndex == null) {
                throw new IllegalArgumentException("Password index of the clear password mapper must be specified as non null value");
            }
            return new ClearPasswordMapper(this);
        }
    }

    public static final class SimpleDigestMapper {

        private final Integer passwordIndex;
        private final String algorithm;

        private SimpleDigestMapper(SimpleDigestMapperBuilder builder) {
            this.passwordIndex = builder.passwordIndex;
            this.algorithm = builder.algorithm;
        }

        public Integer getPasswordIndex() {
            return passwordIndex;
        }

        public String getAlgorithm() {
            return algorithm;
        }

    }

    public static final class SimpleDigestMapperBuilder {

        private Integer passwordIndex;
        private String algorithm;

        public SimpleDigestMapperBuilder passwordIndex(Integer passwordIndex) {
            this.passwordIndex = passwordIndex;
            return this;
        }

        public SimpleDigestMapperBuilder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public SimpleDigestMapper build() {
            if (passwordIndex == null) {
                throw new IllegalArgumentException("Password index of the simple digest password mapper must be specified as non null value");
            }
            return new SimpleDigestMapper(this);
        }
    }

    public static final class SaltedSimpleDigestMapper {

        private final Integer passwordIndex;
        private final Integer saltIndex;
        private final String algorithm;

        private SaltedSimpleDigestMapper(SaltedSimpleDigestMapperBuilder builder) {
            this.passwordIndex = builder.passwordIndex;
            this.saltIndex = builder.saltIndex;
            this.algorithm = builder.algorithm;
        }

        public Integer getPasswordIndex() {
            return passwordIndex;
        }

        public Integer getSaltIndex() {
            return saltIndex;
        }

        public String getAlgorithm() {
            return algorithm;
        }

    }

    public static final class SaltedSimpleDigestMapperBuilder {

        private Integer passwordIndex;
        private Integer saltIndex;
        private String algorithm;

        public SaltedSimpleDigestMapperBuilder passwordIndex(Integer passwordIndex) {
            this.passwordIndex = passwordIndex;
            return this;
        }

        public SaltedSimpleDigestMapperBuilder saltIndex(Integer saltIndex) {
            this.saltIndex = saltIndex;
            return this;
        }

        public SaltedSimpleDigestMapperBuilder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public SaltedSimpleDigestMapper build() {
            if (passwordIndex == null) {
                throw new IllegalArgumentException("Password index of the salted simple digest password mapper must be specified as non null value");
            }
            if (saltIndex == null) {
                throw new IllegalArgumentException("Salt index of the salted simple digest password mapper must be specified as non null value");
            }
            return new SaltedSimpleDigestMapper(this);
        }
    }

    public static final class BcryptMapper {

        private final Integer passwordIndex;
        private final Integer saltIndex;
        private final Integer iterationCountIndex;

        private BcryptMapper(BcryptMapperBuilder builder) {
            this.passwordIndex = builder.passwordIndex;
            this.saltIndex = builder.saltIndex;
            this.iterationCountIndex = builder.iterationCountIndex;
        }

        public Integer getPasswordIndex() {
            return passwordIndex;
        }

        public Integer getSaltIndex() {
            return saltIndex;
        }

        public Integer getIterationCountIndex() {
            return iterationCountIndex;
        }

    }

    public static final class BcryptMapperBuilder {

        private Integer passwordIndex;
        private Integer saltIndex;
        private Integer iterationCountIndex;

        public BcryptMapperBuilder passwordIndex(Integer passwordIndex) {
            this.passwordIndex = passwordIndex;
            return this;
        }

        public BcryptMapperBuilder saltIndex(Integer saltIndex) {
            this.saltIndex = saltIndex;
            return this;
        }

        public BcryptMapperBuilder iterationCountIndex(Integer iterationCountIndex) {
            this.iterationCountIndex = iterationCountIndex;
            return this;
        }

        public BcryptMapper build() {
            if (passwordIndex == null) {
                throw new IllegalArgumentException("Password index of the bcrypt password mapper must be specified as non null value");
            }
            if (saltIndex == null) {
                throw new IllegalArgumentException("Salt index of the bcrypt password mapper must be specified as non null value");
            }
            if (iterationCountIndex == null) {
                throw new IllegalArgumentException("Algorithm of the bcrypt password mapper must be specified as non null value");
            }
            return new BcryptMapper(this);
        }
    }

    public static final class ScramMapper {

        private final Integer passwordIndex;
        private final Integer saltIndex;
        private final Integer iterationCountIndex;
        private final String algorithm;

        private ScramMapper(ScramMapperBuilder builder) {
            this.passwordIndex = builder.passwordIndex;
            this.saltIndex = builder.saltIndex;
            this.iterationCountIndex = builder.iterationCountIndex;
            this.algorithm = builder.algorithm;
        }

        public Integer getPasswordIndex() {
            return passwordIndex;
        }

        public Integer getSaltIndex() {
            return saltIndex;
        }

        public Integer getIterationCountIndex() {
            return iterationCountIndex;
        }

        public String getAlgorithm() {
            return algorithm;
        }

    }

    public static final class ScramMapperBuilder {

        private Integer passwordIndex;
        private Integer saltIndex;
        private Integer iterationCountIndex;
        private String algorithm;

        public ScramMapperBuilder passwordIndex(Integer passwordIndex) {
            this.passwordIndex = passwordIndex;
            return this;
        }

        public ScramMapperBuilder saltIndex(Integer saltIndex) {
            this.saltIndex = saltIndex;
            return this;
        }

        public ScramMapperBuilder iterationCountIndex(Integer iterationCountIndex) {
            this.iterationCountIndex = iterationCountIndex;
            return this;
        }

        public ScramMapperBuilder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public ScramMapper build() {
            if (passwordIndex == null) {
                throw new IllegalArgumentException("Password index of the scram mapper must be specified as non null value");
            }
            if (saltIndex == null) {
                throw new IllegalArgumentException("Salt index of the scram mapper must be specified as non null value");
            }
            if (iterationCountIndex == null) {
                throw new IllegalArgumentException("Algorithm of the scram mapper must be specified as non null value");
            }
            return new ScramMapper(this);
        }
    }

}
