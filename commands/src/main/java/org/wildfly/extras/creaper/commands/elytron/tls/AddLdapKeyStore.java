package org.wildfly.extras.creaper.commands.elytron.tls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddLdapKeyStore implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String dirContext;
    private final String searchPath;
    private final Boolean searchRecursive;
    private final Integer searchTimeLimit;
    private final String filterAlias;
    private final String filterCertificate;
    private final String filterIterate;
    private final String aliasAttribute;
    private final String certificateAttribute;
    private final String certificateType;
    private final String certificateChainAttribute;
    private final String certificateChainEncoding;
    private final String keyAttribute;
    private final String keyType;
    private final NewItemTemplate newItemTemplate;
    private final boolean replaceExisting;

    private AddLdapKeyStore(Builder builder) {
        this.name = builder.name;
        this.dirContext = builder.dirContext;
        this.searchPath = builder.searchPath;
        this.searchRecursive = builder.searchRecursive;
        this.searchTimeLimit = builder.searchTimeLimit;
        this.filterAlias = builder.filterAlias;
        this.filterCertificate = builder.filterCertificate;
        this.filterIterate = builder.filterIterate;
        this.aliasAttribute = builder.aliasAttribute;
        this.certificateAttribute = builder.certificateAttribute;
        this.certificateType = builder.certificateType;
        this.certificateChainAttribute = builder.certificateChainAttribute;
        this.certificateChainEncoding = builder.certificateChainEncoding;
        this.keyAttribute = builder.keyAttribute;
        this.keyType = builder.keyType;
        this.newItemTemplate = builder.newItemTemplate;
        // Replace existing
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address keyStoreAddress = Address.subsystem("elytron").and("ldap-key-store", name);
        if (replaceExisting) {
            ops.removeIfExists(keyStoreAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        Values keyStoreValues = Values.empty()
                .and("name", name)
                .and("dir-context", dirContext)
                .and("search-path", searchPath)
                .andOptional("search-recursive", searchRecursive)
                .andOptional("search-time-limit", searchTimeLimit)
                .andOptional("filter-alias", filterAlias)
                .andOptional("filter-certificate", filterCertificate)
                .andOptional("filter-iterate", filterIterate)
                .andOptional("alias-attribute", aliasAttribute)
                .andOptional("certificate-attribute", certificateAttribute)
                .andOptional("certificate-type", certificateType)
                .andOptional("certificate-chain-attribute", certificateChainAttribute)
                .andOptional("certificate-chain-encoding", certificateChainEncoding)
                .andOptional("key-attribute", keyAttribute)
                .andOptional("key-type", keyType);

        if (newItemTemplate != null) {
            ModelNode newItemTemplateNode = new ModelNode();
            newItemTemplateNode.add("new-item-path", newItemTemplate.getNewItemPath());
            newItemTemplateNode.add("new-item-rdn", newItemTemplate.getNewItemRdn());
            if (newItemTemplate.getNewItemAttributes() != null && !newItemTemplate.getNewItemAttributes().isEmpty()) {
                List<ModelNode> newItemAttributesNodeList = new ArrayList<ModelNode>();
                for (NewItemAttribute newItemAttribute : newItemTemplate.getNewItemAttributes()) {
                    ModelNode attributeNode = new ModelNode();

                    if (newItemAttribute.getName() != null && !newItemAttribute.getName().isEmpty()) {
                        attributeNode.add("name", newItemAttribute.getName());
                    }

                    ModelNode valuesList = new ModelNode().setEmptyList();
                    for (String value : newItemAttribute.getValues()) {
                        valuesList.add(value);
                    }
                    attributeNode.add("value", valuesList);

                    attributeNode = attributeNode.asObject();
                    newItemAttributesNodeList.add(attributeNode);
                }
                ModelNode newItemAttributesNode = new ModelNode();
                newItemAttributesNode.set(newItemAttributesNodeList);
                newItemTemplateNode.add("new-item-attributes", newItemAttributesNode);
            }
            newItemTemplateNode = newItemTemplateNode.asObject();
            keyStoreValues = keyStoreValues.and("new-item-template", newItemTemplateNode);
        }

        ops.add(keyStoreAddress, keyStoreValues);

    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        ctx.client.apply(GroovyXmlTransform.of(AddLdapKeyStore.class)
                .subtree("elytronSubsystem", Subtree.subsystem("elytron"))
                .parameter("atrName", name)
                .parameter("atrDirContext", dirContext)
                .parameter("atrSearchPath", searchPath)
                .parameter("atrSearchRecursive", searchRecursive)
                .parameter("atrSearchTimeLimit", searchTimeLimit)
                .parameter("atrFilterAlias", filterAlias)
                .parameter("atrFilterCertificate", filterCertificate)
                .parameter("atrFilterIterate", filterIterate)
                .parameter("atrAliasAttribute", aliasAttribute)
                .parameter("atrCertificateAttribute", certificateAttribute)
                .parameter("atrCertificateType", certificateType)
                .parameter("atrCertificateChainAttribute", certificateChainAttribute)
                .parameter("atrCertificateChainEncoding", certificateChainEncoding)
                .parameter("atrKeyAttribute", keyAttribute)
                .parameter("atrKeyType", keyType)
                .parameters(newItemTemplate != null ? newItemTemplate.toParameters() : NewItemTemplate.EMPTY_PARAMETERS)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private final String name;
        private String dirContext;
        private String searchPath;
        private Boolean searchRecursive;
        private Integer searchTimeLimit;
        private String filterAlias;
        private String filterCertificate;
        private String filterIterate;
        private String aliasAttribute;
        private String certificateAttribute;
        private String certificateType;
        private String certificateChainAttribute;
        private String certificateChainEncoding;
        private String keyAttribute;
        private String keyType;
        private NewItemTemplate newItemTemplate;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name of the ldap-key-store must be specified as non empty value");
            }
            this.name = name;
        }

        public Builder dirContext(String dirContext) {
            this.dirContext = dirContext;
            return this;
        }

        public Builder searchPath(String searchPath) {
            this.searchPath = searchPath;
            return this;
        }

        public Builder searchRecursive(Boolean searchRecursive) {
            this.searchRecursive = searchRecursive;
            return this;
        }

        public Builder searchTimeLimit(Integer searchTimeLimit) {
            this.searchTimeLimit = searchTimeLimit;
            return this;
        }

        public Builder filterAlias(String filterAlias) {
            this.filterAlias = filterAlias;
            return this;
        }

        public Builder filterCertificate(String filterCertificate) {
            this.filterCertificate = filterCertificate;
            return this;
        }

        public Builder filterIterate(String filterIterate) {
            this.filterIterate = filterIterate;
            return this;
        }

        public Builder aliasAttribute(String aliasAttribute) {
            this.aliasAttribute = aliasAttribute;
            return this;
        }

        public Builder certificateAttribute(String certificateAttribute) {
            this.certificateAttribute = certificateAttribute;
            return this;
        }

        public Builder certificateType(String certificateType) {
            this.certificateType = certificateType;
            return this;
        }

        public Builder certificateChainAttribute(String certificateChainAttribute) {
            this.certificateChainAttribute = certificateChainAttribute;
            return this;
        }

        public Builder certificateChainEncoding(String certificateChainEncoding) {
            this.certificateChainEncoding = certificateChainEncoding;
            return this;
        }

        public Builder keyAttribute(String keyAttribute) {
            this.keyAttribute = keyAttribute;
            return this;
        }

        public Builder keyType(String keyType) {
            this.keyType = keyType;
            return this;
        }

        public Builder newItemTemplate(NewItemTemplate newItemTemplate) {
            this.newItemTemplate = newItemTemplate;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }


        public AddLdapKeyStore build() {

            if (dirContext == null || dirContext.isEmpty()) {
                throw new IllegalArgumentException("Dir context of the ldap-key-store must be specified as non empty value");
            }
            if (searchPath == null || searchPath.isEmpty()) {
                throw new IllegalArgumentException("Search path of the ldap-key-store must be specified as non empty value");
            }

            return new AddLdapKeyStore(this);
        }
    }

    public static final class NewItemTemplate {

        static final Map<String, Object> EMPTY_PARAMETERS = new HashMap<String, Object>();

        static {
            EMPTY_PARAMETERS.put("atrNewItemAttributes", null);
            EMPTY_PARAMETERS.put("atrNewItemPath", null);
            EMPTY_PARAMETERS.put("atrNewItemRdn", null);
        }

        private final List<NewItemAttribute> newItemAttributes;
        private final String newItemPath;
        private final String newItemRdn;


        private NewItemTemplate(NewItemTemplateBuilder builder) {
            this.newItemPath = builder.newItemPath;
            this.newItemRdn = builder.newItemRdn;
            this.newItemAttributes = builder.newItemAttributes;
        }

        public String getNewItemPath() {
            return newItemPath;
        }

        public String getNewItemRdn() {
            return newItemRdn;
        }

        public List<NewItemAttribute> getNewItemAttributes() {
            return newItemAttributes;
        }

        public Map<String, Object> toParameters() {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("atrNewItemAttributes", newItemAttributes);
            parameters.put("atrNewItemPath", newItemPath);
            parameters.put("atrNewItemRdn", newItemRdn);
            return parameters;
        }
    }

    public static final class NewItemTemplateBuilder {

        private String newItemPath;
        private String newItemRdn;
        private List<NewItemAttribute> newItemAttributes  = new ArrayList<NewItemAttribute>();

        public NewItemTemplateBuilder newItemPath(String newItemPath) {
            this.newItemPath = newItemPath;
            return this;
        }

        public NewItemTemplateBuilder newItemRdn(String newItemRdn) {
            this.newItemRdn = newItemRdn;
            return this;
        }

        public NewItemTemplateBuilder addNewItemAttributes(NewItemAttribute... newItemAttributes) {
            if (newItemAttributes == null) {
                throw new IllegalArgumentException("NewItemAttributes added to ldap-key-store must not be null");
            }
            Collections.addAll(this.newItemAttributes, newItemAttributes);
            return this;
        }

        public NewItemTemplate build() {
            if (newItemPath == null || newItemPath.isEmpty()) {
                throw new IllegalArgumentException("new-item-template.new-item-path of the ldap-key-store must be specified as non empty value");
            }
            if (newItemRdn == null || newItemRdn.isEmpty()) {
                throw new IllegalArgumentException("new-item-template.new-item-rdn of the ldap-key-store must be specified as non empty value");
            }
            return new NewItemTemplate(this);
        }
    }

    public static final class NewItemAttribute {

        private final String name;
        private final List<String> values;

        private NewItemAttribute(NewItemAttributeBuilder builder) {
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

    public static final class NewItemAttributeBuilder {

        private String name;
        private List<String> values = new ArrayList<String>();

        public NewItemAttributeBuilder name(String name) {
            this.name = name;
            return this;
        }

        public NewItemAttributeBuilder addValues(String... values) {
            if (values == null) {
                throw new IllegalArgumentException("Values added to NewIdentityAttributesBuilder for ldap-key-store must not be null");
            }
            Collections.addAll(this.values, values);
            return this;
        }

        public NewItemAttribute build() {
            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("values must not be null and must include at least one entry");
            }
            return new NewItemAttribute(this);
        }
    }

}
