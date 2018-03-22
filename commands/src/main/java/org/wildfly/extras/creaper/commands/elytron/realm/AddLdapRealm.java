package org.wildfly.extras.creaper.commands.elytron.realm;

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

public final class AddLdapRealm implements OnlineCommand {

    private final String name;
    private final String dirContext;
    private final Boolean directVerification;
    private final Boolean allowBlankPassword;
    private final IdentityMapping identityMapping;
    private final boolean replaceExisting;

    private AddLdapRealm(Builder builder) {
        this.name = builder.name;
        this.dirContext = builder.dirContext;
        this.directVerification = builder.directVerification;
        this.allowBlankPassword = builder.allowBlankPassword;
        this.identityMapping = builder.identityMapping;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address realmAddress = Address.subsystem("elytron").and("ldap-realm", name);
        if (replaceExisting) {
            ops.removeIfExists(realmAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ModelNode identityMappingModelNode = new ModelNode();
        identityMappingModelNode.add("rdn-identifier", identityMapping.getRdnIdentifier());
        addOptionalToModelNode(identityMappingModelNode, "search-base-dn", identityMapping.getSearchBaseDn());
        addOptionalToModelNode(identityMappingModelNode, "use-recursive-search",
                identityMapping.getUseRecursiveSearch());
        addOptionalToModelNode(identityMappingModelNode, "filter-name", identityMapping.getFilterName());
        addOptionalToModelNode(identityMappingModelNode, "iterator-filter", identityMapping.getIteratorFilter());
        addOptionalToModelNode(identityMappingModelNode, "new-identity-parent-dn",
                identityMapping.getNewIdentityParentDn());
        if (identityMapping.getAttributeMappings() != null && !identityMapping.getAttributeMappings().isEmpty()) {
            List<ModelNode> newAttributeMappingNodeList = new ArrayList<ModelNode>();
            for (AttributeMapping attributeMapping : identityMapping.getAttributeMappings()) {
                ModelNode node = new ModelNode();
                addOptionalToModelNode(node, "from", attributeMapping.getFrom());
                addOptionalToModelNode(node, "to", attributeMapping.getTo());
                addOptionalToModelNode(node, "filter", attributeMapping.getFilter());
                addOptionalToModelNode(node, "filter-base-dn", attributeMapping.getFilterBaseDn());
                addOptionalToModelNode(node, "extract-rdn", attributeMapping.getExtractRdn());
                addOptionalToModelNode(node, "search-recursive", attributeMapping.getSearchRecursive());
                addOptionalToModelNode(node, "role-recursion", attributeMapping.getRoleRecursion());
                addOptionalToModelNode(node, "role-recursion-name", attributeMapping.getRoleRecursionName());
                addOptionalToModelNode(node, "reference", attributeMapping.getReference());
                node = node.asObject();
                newAttributeMappingNodeList.add(node);
            }
            ModelNode attributeMappingNode = new ModelNode();
            attributeMappingNode.set(newAttributeMappingNodeList);
            identityMappingModelNode.add("attribute-mapping", attributeMappingNode);
        }
        if (identityMapping.getUserPasswordMapper() != null) {
            ModelNode node = new ModelNode();
            node.add("from", identityMapping.getUserPasswordMapper().getFrom());
            addOptionalToModelNode(node, "writable", identityMapping.getUserPasswordMapper().getWritable());
            addOptionalToModelNode(node, "verifiable", identityMapping.getUserPasswordMapper().getVerifiable());
            identityMappingModelNode.add("user-password-mapper", node.asObject());
        }
        if (identityMapping.getOtpCredentialMapper() != null) {
            ModelNode node = new ModelNode();
            node.add("algorithm-from", identityMapping.getOtpCredentialMapper().getAlgorithmFrom());
            node.add("hash-from", identityMapping.getOtpCredentialMapper().getHashFrom());
            node.add("seed-from", identityMapping.getOtpCredentialMapper().getSeedFrom());
            node.add("sequence-from", identityMapping.getOtpCredentialMapper().getSequenceFrom());
            identityMappingModelNode.add("otp-credential-mapper", node.asObject());
        }
        if (identityMapping.getX509CredentialMapper() != null) {
            ModelNode node = new ModelNode();
            addOptionalToModelNode(node, "digest-from", identityMapping.getX509CredentialMapper().getDigestFrom());
            addOptionalToModelNode(node, "digest-algorithm",
                    identityMapping.getX509CredentialMapper().getDigestAlgorithm());
            addOptionalToModelNode(node, "certificate-from",
                    identityMapping.getX509CredentialMapper().getCertificateFrom());
            addOptionalToModelNode(node, "serial-number-from",
                    identityMapping.getX509CredentialMapper().getSerialNumberFrom());
            addOptionalToModelNode(node, "subject-dn-from",
                    identityMapping.getX509CredentialMapper().getSubjectDnFrom());
            identityMappingModelNode.add("x509-credential-mapper", node.asObject());
        }
        if (identityMapping.getNewIdentityAttributes() != null
                && !identityMapping.getNewIdentityAttributes().isEmpty()) {
            List<ModelNode> newIdentityAttributesNodeList = new ArrayList<ModelNode>();
            for (NewIdentityAttributes newIdentityAttribute : identityMapping.getNewIdentityAttributes()) {
                ModelNode attributeNode = new ModelNode();
                addOptionalToModelNode(attributeNode, "name", newIdentityAttribute.getName());

                ModelNode valuesList = new ModelNode().setEmptyList();
                for (String value : newIdentityAttribute.getValues()) {
                    valuesList.add(value);
                }
                attributeNode.add("value", valuesList);

                attributeNode = attributeNode.asObject();
                newIdentityAttributesNodeList.add(attributeNode);
            }
            ModelNode newIdentityAttributesNode = new ModelNode();
            newIdentityAttributesNode.set(newIdentityAttributesNodeList);
            identityMappingModelNode.add("new-identity-attributes", newIdentityAttributesNode);
        }

        ops.add(realmAddress, Values.empty()
                .and("dir-context", dirContext)
                .andOptional("direct-verification", directVerification)
                .andOptional("allow-blank-password", allowBlankPassword)
                .and("identity-mapping", identityMappingModelNode.asObject()));

    }

    private void addOptionalToModelNode(ModelNode node, String name, String value) {
        if (value != null && !value.isEmpty()) {
            node.add(name, value);
        }
    }

    private void addOptionalToModelNode(ModelNode node, String name, Boolean value) {
        if (value != null) {
            node.add(name, value);
        }
    }

    private void addOptionalToModelNode(ModelNode node, String name, Integer value) {
        if (value != null) {
            node.add(name, value);
        }
    }

    public static final class Builder {

        private final String name;
        private String dirContext;
        private Boolean directVerification;
        private Boolean allowBlankPassword;
        private IdentityMapping identityMapping;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the ldap-realm must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the ldap-realm must not be empty value");
            }
            this.name = name;
        }

        public Builder dirContext(String dirContext) {
            this.dirContext = dirContext;
            return this;
        }

        public Builder directVerification(Boolean directVerification) {
            this.directVerification = directVerification;
            return this;
        }

        public Builder allowBlankPassword(Boolean allowBlankPassword) {
            this.allowBlankPassword = allowBlankPassword;
            return this;
        }

        public Builder identityMapping(IdentityMapping identityMapping) {
            this.identityMapping = identityMapping;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddLdapRealm build() {
            if (dirContext == null || dirContext.isEmpty()) {
                throw new IllegalArgumentException("dir-context must not be null and must have a minimum length of 1 characters");
            }
            if (identityMapping == null) {
                throw new IllegalArgumentException("identity-mapping must not be null");
            }
            return new AddLdapRealm(this);
        }
    }

    public static final class IdentityMapping {

        private final String rdnIdentifier;
        private final String searchBaseDn;
        private final Boolean useRecursiveSearch;
        private final String filterName;
        private final String iteratorFilter;
        private final String newIdentityParentDn;
        private final List<AttributeMapping> attributeMappings;
        private final UserPasswordMapper userPasswordMapper;
        private final OtpCredentialMapper otpCredentialMapper;
        private final X509CredentialMapper x509CredentialMapper;
        private final List<NewIdentityAttributes> newIdentityAttributes;

        private IdentityMapping(IdentityMappingBuilder builder) {
            this.rdnIdentifier = builder.rdnIdentifier;
            this.searchBaseDn = builder.searchBaseDn;
            this.useRecursiveSearch = builder.useRecursiveSearch;
            this.filterName = builder.filterName;
            this.iteratorFilter = builder.iteratorFilter;
            this.newIdentityParentDn = builder.newIdentityParentDn;
            this.attributeMappings = builder.attributeMappings;
            this.userPasswordMapper = builder.userPasswordMapper;
            this.otpCredentialMapper = builder.otpCredentialMapper;
            this.newIdentityAttributes = builder.newIdentityAttributes;
            this.x509CredentialMapper = builder.x509CredentialMapper;
        }

        public String getRdnIdentifier() {
            return rdnIdentifier;
        }

        public String getSearchBaseDn() {
            return searchBaseDn;
        }

        public Boolean getUseRecursiveSearch() {
            return useRecursiveSearch;
        }

        public String getFilterName() {
            return filterName;
        }

        public String getIteratorFilter() {
            return iteratorFilter;
        }

        public String getNewIdentityParentDn() {
            return newIdentityParentDn;
        }

        public List<AttributeMapping> getAttributeMappings() {
            return attributeMappings;
        }

        public UserPasswordMapper getUserPasswordMapper() {
            return userPasswordMapper;
        }

        public OtpCredentialMapper getOtpCredentialMapper() {
            return otpCredentialMapper;
        }

        public X509CredentialMapper getX509CredentialMapper() {
            return x509CredentialMapper;
        }

        public List<NewIdentityAttributes> getNewIdentityAttributes() {
            return newIdentityAttributes;
        }

    }

    public static final class IdentityMappingBuilder {

        private String rdnIdentifier;
        private String searchBaseDn;
        private Boolean useRecursiveSearch;
        private String filterName;
        private String iteratorFilter;
        private String newIdentityParentDn;
        private List<AttributeMapping> attributeMappings = new ArrayList<AttributeMapping>();
        private UserPasswordMapper userPasswordMapper;
        private OtpCredentialMapper otpCredentialMapper;
        private X509CredentialMapper x509CredentialMapper;
        private List<NewIdentityAttributes> newIdentityAttributes = new ArrayList<NewIdentityAttributes>();

        public IdentityMappingBuilder rdnIdentifier(String rdnIdentifier) {
            this.rdnIdentifier = rdnIdentifier;
            return this;
        }

        public IdentityMappingBuilder searchBaseDn(String searchBaseDn) {
            this.searchBaseDn = searchBaseDn;
            return this;
        }

        public IdentityMappingBuilder useRecursiveSearch(Boolean useRecursiveSearch) {
            this.useRecursiveSearch = useRecursiveSearch;
            return this;
        }

        public IdentityMappingBuilder filterName(String filterName) {
            this.filterName = filterName;
            return this;
        }

        public IdentityMappingBuilder iteratorFilter(String iteratorFilter) {
            this.iteratorFilter = iteratorFilter;
            return this;
        }

        public IdentityMappingBuilder newIdentityParentDn(String newIdentityParentDn) {
            this.newIdentityParentDn = newIdentityParentDn;
            return this;
        }

        public IdentityMappingBuilder addAttributeMappings(AttributeMapping... attributeMappings) {
            if (attributeMappings == null) {
                throw new IllegalArgumentException("AttributeMappings added to ldap-realm must not be null");
            }
            Collections.addAll(this.attributeMappings, attributeMappings);
            return this;
        }

        public IdentityMappingBuilder userPasswordMapper(UserPasswordMapper userPasswordMapper) {
            this.userPasswordMapper = userPasswordMapper;
            return this;
        }

        public IdentityMappingBuilder otpCredentialMapper(OtpCredentialMapper otpCredentialMapper) {
            this.otpCredentialMapper = otpCredentialMapper;
            return this;
        }

        public IdentityMappingBuilder x509CredentialMapper(
                X509CredentialMapper x509CredentialMapper) {
            this.x509CredentialMapper = x509CredentialMapper;
            return this;
        }

        public IdentityMappingBuilder addNewIdentityAttributes(NewIdentityAttributes... newIdentityAttributes) {
            if (newIdentityAttributes == null) {
                throw new IllegalArgumentException("NewIdentityAttributes added to ldap-realm must not be null");
            }
            Collections.addAll(this.newIdentityAttributes, newIdentityAttributes);
            return this;
        }

        public IdentityMapping build() {
            if (rdnIdentifier == null || rdnIdentifier.isEmpty()) {
                throw new IllegalArgumentException("rdn-identifier must not be null and must have a minimum length of 1 characters");
            }
            return new IdentityMapping(this);
        }
    }

    public static final class AttributeMapping {

        private final String from;
        private final String to;
        private final String filter;
        private final String filterBaseDn;
        private final String extractRdn;
        private final Boolean searchRecursive;
        private final Integer roleRecursion;
        private final String roleRecursionName;
        private final String reference;

        private AttributeMapping(AttributeMappingBuilder builder) {
            this.from = builder.from;
            this.to = builder.to;
            this.filter = builder.filter;
            this.filterBaseDn = builder.filterBaseDn;
            this.extractRdn = builder.extractRdn;
            this.searchRecursive = builder.searchRecursive;
            this.roleRecursion = builder.roleRecursion;
            this.roleRecursionName = builder.roleRecursionName;
            this.reference = builder.reference;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getFilter() {
            return filter;
        }

        public String getFilterBaseDn() {
            return filterBaseDn;
        }

        public String getExtractRdn() {
            return extractRdn;
        }

        public Boolean getSearchRecursive() {
            return searchRecursive;
        }

        public Integer getRoleRecursion() {
            return roleRecursion;
        }

        public String getRoleRecursionName() {
            return roleRecursionName;
        }

        public String getReference() {
            return reference;
        }

    }

    public static final class AttributeMappingBuilder {

        private String from;
        private String to;
        private String filter;
        private String filterBaseDn;
        private String extractRdn;
        private Boolean searchRecursive;
        private Integer roleRecursion;
        private String roleRecursionName;
        private String reference;

        public AttributeMappingBuilder from(String from) {
            this.from = from;
            return this;
        }

        public AttributeMappingBuilder to(String to) {
            this.to = to;
            return this;
        }

        public AttributeMappingBuilder filter(String filter) {
            this.filter = filter;
            return this;
        }

        public AttributeMappingBuilder filterBaseDn(String filterBaseDn) {
            this.filterBaseDn = filterBaseDn;
            return this;
        }

        public AttributeMappingBuilder extractRdn(String extractRdn) {
            this.extractRdn = extractRdn;
            return this;
        }

        public AttributeMappingBuilder searchRecursive(Boolean searchRecursive) {
            this.searchRecursive = searchRecursive;
            return this;
        }

        public AttributeMappingBuilder roleRecursion(Integer roleRecursion) {
            this.roleRecursion = roleRecursion;
            return this;
        }

        public AttributeMappingBuilder roleRecursionName(String roleRecursionName) {
            this.roleRecursionName = roleRecursionName;
            return this;
        }

        public AttributeMappingBuilder reference(String reference) {
            this.reference = reference;
            return this;
        }

        public AttributeMapping build() {
            return new AttributeMapping(this);
        }

    }

    public static final class UserPasswordMapper {

        private final String from;
        private final Boolean writable;
        private final Boolean verifiable;

        private UserPasswordMapper(UserPasswordMapperBuilder builder) {
            this.from = builder.from;
            this.writable = builder.writable;
            this.verifiable = builder.verifiable;
        }

        public String getFrom() {
            return from;
        }

        public Boolean getWritable() {
            return writable;
        }

        public Boolean getVerifiable() {
            return verifiable;
        }

    }

    public static final class UserPasswordMapperBuilder {

        private String from;
        private Boolean writable;
        private Boolean verifiable;

        public UserPasswordMapperBuilder from(String from) {
            this.from = from;
            return this;
        }

        public UserPasswordMapperBuilder writable(Boolean writable) {
            this.writable = writable;
            return this;
        }

        public UserPasswordMapperBuilder verifiable(Boolean verifiable) {
            this.verifiable = verifiable;
            return this;
        }

        public UserPasswordMapper build() {
            if (from == null || from.isEmpty()) {
                throw new IllegalArgumentException("identity-mapping.user-password-mapper.from must not be null and must have a minimum length of 1 characters");
            }
            return new UserPasswordMapper(this);
        }
    }

    public static final class OtpCredentialMapper {

        private final String algorithmFrom;
        private final String hashFrom;
        private final String seedFrom;
        private final String sequenceFrom;

        private OtpCredentialMapper(OtpCredentialMapperBuilder builder) {
            this.algorithmFrom = builder.algorithmFrom;
            this.hashFrom = builder.hashFrom;
            this.seedFrom = builder.seedFrom;
            this.sequenceFrom = builder.sequenceFrom;
        }

        public String getAlgorithmFrom() {
            return algorithmFrom;
        }

        public String getHashFrom() {
            return hashFrom;
        }

        public String getSeedFrom() {
            return seedFrom;
        }

        public String getSequenceFrom() {
            return sequenceFrom;
        }

    }

    public static final class OtpCredentialMapperBuilder {

        private String algorithmFrom;
        private String hashFrom;
        private String seedFrom;
        private String sequenceFrom;

        public OtpCredentialMapperBuilder algorithmFrom(String algorithmFrom) {
            this.algorithmFrom = algorithmFrom;
            return this;
        }

        public OtpCredentialMapperBuilder hashFrom(String hashFrom) {
            this.hashFrom = hashFrom;
            return this;
        }

        public OtpCredentialMapperBuilder seedFrom(String seedFrom) {
            this.seedFrom = seedFrom;
            return this;
        }

        public OtpCredentialMapperBuilder sequenceFrom(String sequenceFrom) {
            this.sequenceFrom = sequenceFrom;
            return this;
        }

        public OtpCredentialMapper build() {
            if (algorithmFrom == null || algorithmFrom.isEmpty()) {
                throw new IllegalArgumentException("identity-mapping.otp-credential-mapper.algorithm-from must not be null and must have a minimum length of 1 characters");
            }
            if (hashFrom == null || hashFrom.isEmpty()) {
                throw new IllegalArgumentException("identity-mapping.otp-credential-mapper.hash-from must not be null and must have a minimum length of 1 characters");
            }
            if (seedFrom == null || seedFrom.isEmpty()) {
                throw new IllegalArgumentException("identity-mapping.otp-credential-mapper.seed-from must not be null and must have a minimum length of 1 characters");
            }
            if (sequenceFrom == null || sequenceFrom.isEmpty()) {
                throw new IllegalArgumentException("identity-mapping.otp-credential-mapper.sequence-from must not be null and must have a minimum length of 1 characters");
            }
            return new OtpCredentialMapper(this);
        }
    }

    public static final class X509CredentialMapper {

        private final String digestFrom;
        private final String digestAlgorithm;
        private final String certificateFrom;
        private final String serialNumberFrom;
        private final String subjectDnFrom;

        private X509CredentialMapper(X509CredentialMapperBuilder builder) {
            this.digestFrom = builder.digestFrom;
            this.digestAlgorithm = builder.digestAlgorithm;
            this.certificateFrom = builder.certificateFrom;
            this.serialNumberFrom = builder.serialNumberFrom;
            this.subjectDnFrom = builder.subjectDnFrom;
        }

        public String getDigestFrom() {
            return digestFrom;
        }

        public String getDigestAlgorithm() {
            return digestAlgorithm;
        }

        public String getCertificateFrom() {
            return certificateFrom;
        }

        public String getSerialNumberFrom() {
            return serialNumberFrom;
        }

        public String getSubjectDnFrom() {
            return subjectDnFrom;
        }

    }

    public static final class X509CredentialMapperBuilder {

        private String digestFrom;
        private String digestAlgorithm;
        private String certificateFrom;
        private String serialNumberFrom;
        private String subjectDnFrom;

        public X509CredentialMapperBuilder digestFrom(String digestFrom) {
            this.digestFrom = digestFrom;
            return this;
        }

        public X509CredentialMapperBuilder digestAlgorithm(String digestAlgorithm) {
            this.digestAlgorithm = digestAlgorithm;
            return this;
        }

        public X509CredentialMapperBuilder certificateFrom(String certificateFrom) {
            this.certificateFrom = certificateFrom;
            return this;
        }

        public X509CredentialMapperBuilder serialNumberFrom(String serialNumberFrom) {
            this.serialNumberFrom = serialNumberFrom;
            return this;
        }

        public X509CredentialMapperBuilder subjectDnFrom(String subjectDnFrom) {
            this.subjectDnFrom = subjectDnFrom;
            return this;
        }

        public X509CredentialMapper build() {
            return new X509CredentialMapper(this);
        }
    }

    public static final class NewIdentityAttributes {

        private final String name;
        private List<String> values;

        private NewIdentityAttributes(NewIdentityAttributesBuilder builder) {
            this.name = builder.name;
            this.values = builder.values;
        }

        public String getName() {
            return name;
        }

        public List<String> getValues() {
            return values;
        }

    }

    public static final class NewIdentityAttributesBuilder {

        private String name;
        private List<String> values = new ArrayList<String>();

        public NewIdentityAttributesBuilder name(String name) {
            this.name = name;
            return this;
        }

        public NewIdentityAttributesBuilder addValues(String... values) {
            if (values == null) {
                throw new IllegalArgumentException("Values added to NewIdentityAttributesBuilder for ldap-realm must not be null");
            }
            Collections.addAll(this.values, values);
            return this;
        }

        public NewIdentityAttributes build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("name must not be null and must have a minimum length of 1 characters");
            }
            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("values must not be null and must include at least one entry");
            }
            return new NewIdentityAttributes(this);
        }
    }

}
