package org.wildfly.extras.creaper.commands.orb;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Creaper command which allows settings of orb subsystem
 *
 * <ul>
 *   <li>JacORB for AS7 and WildFly &lt;= 8</li>
 *   <li>OpenJDK IIOP ORB for WildFly &gt;= 9</li>
 * </ul>
 */
public final class ChangeOrb implements OfflineCommand, OnlineCommand {

    private static final String JACORB_SUBSYSTEM_NAME = "jacorb";
    private static final String OPENJDK_IIOP_SUBSYSTEM_NAME = "iiop-openjdk";

    private final Attribute<TransactionValues> transactions;
    private final Attribute<OnOff> supportSsl;
    private final Attribute<String> rootContext;
    private final Attribute<String> socketBinding;
    private final Attribute<String> sslSocketBinding;
    private final Attribute<AuthValues> clientRequires;
    private final Attribute<OnOff> addComponentViaInterceptor;
    private final Attribute<AuthValues> serverSupports;
    private final Attribute<String> giopVersion;
    private final Attribute<SecurityValues> security;
    private final Attribute<String> securityDomain;
    private final Attribute<AuthValues> serverRequires;
    private final Attribute<AuthValues> clientSupports;
    private final Attribute<OnOff> exportCorbaloc;
    private final Map<String, Attribute<String>> properties;
    private final Attribute<SupportedValues> integrity;
    private final Attribute<SupportedValues> confidentiality;
    private final Attribute<SupportedValues> trustInTarget;
    private final Attribute<SupportedValues> trustInClient;
    private final Attribute<SupportedValues> detectReplay;
    private final Attribute<SupportedValues> detectMisordering;
    private final Attribute<String> authMethodPassword;
    private final Attribute<String> realm;
    private final Attribute<Boolean> authRequired;
    private final Attribute<SupportedValues> callerPropagation;
    private final Attribute<String> persistentServerId;

    private ChangeOrb(Builder builder) {
        this.transactions = builder.transactions;
        this.supportSsl = builder.supportSsl;
        this.rootContext = builder.rootContext;
        this.socketBinding = builder.socketBinding;
        this.sslSocketBinding = builder.sslSocketBinding;
        this.clientRequires = builder.clientRequires;
        this.addComponentViaInterceptor = builder.addComponentViaInterceptor;
        this.serverSupports = builder.serverSupports;
        this.giopVersion = builder.giopVersion;
        this.security = builder.security;
        this.securityDomain = builder.securityDomain;
        this.serverRequires = builder.serverRequires;
        this.clientSupports = builder.clientSupports;
        this.exportCorbaloc = builder.exportCorbaloc;
        this.properties = builder.properties;
        this.integrity = builder.integrity;
        this.confidentiality = builder.confidentiality;
        this.trustInTarget = builder.trustInTarget;
        this.trustInClient = builder.trustInClient;
        this.detectReplay = builder.detectReplay;
        this.detectMisordering = builder.detectMisordering;
        this.authMethodPassword = builder.authMethodPassword;
        this.realm = builder.realm;
        this.authRequired = builder.authRequired;
        this.callerPropagation = builder.callerPropagation;
        this.persistentServerId = builder.persistentServerId;
    }

    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        OrbType orbType = OrbType.get(ctx.version);
        Address address = Address.subsystem(orbType.subsystemName());

        if (!ops.exists(address)) {
            throw new IllegalStateException("Configuration does not support "
                    + orbType.subsystemName()  + " subsystem");
        }

        Batch batch = new Batch();
        AttributeWriter attributeWriter = new AttributeWriter(address, batch)
            .write("root-context", rootContext)
            .write("socket-binding", socketBinding)
            .write("ssl-socket-binding", sslSocketBinding)
            .write("security-domain", securityDomain)
            .write("auth-method", authMethodPassword)
            .write("realm", realm)
            .write("required", authRequired)
            .write("client-requires",
                    clientRequires.hasValue() ? Attribute.of(clientRequires.get().value) : clientRequires)
            .write("server-supports",
                    serverSupports.hasValue() ? Attribute.of(serverSupports.get().value) : serverSupports)
            .write("server-requires",
                    serverRequires.hasValue() ? Attribute.of(serverRequires.get().value) : serverRequires)
            .write("client-supports",
                    clientSupports.hasValue() ? Attribute.of(clientSupports.get().value) : clientSupports)
            .write("integrity", integrity.hasValue() ? Attribute.of(integrity.get().value) : integrity)
            .write("confidentiality",
                    confidentiality.hasValue() ? Attribute.of(confidentiality.get().value) : confidentiality)
            .write("trust-in-target",
                    trustInTarget.hasValue() ? Attribute.of(trustInTarget.get().value) : trustInTarget)
            .write("trust-in-client", trustInClient.hasValue()
                    ? Attribute.of(trustInClient.get().value) : trustInClient)
            .write("detect-replay", detectReplay.hasValue()
                    ? Attribute.of(detectReplay.get().value) : detectReplay)
            .write("detect-misordering", detectMisordering.hasValue()
                    ? Attribute.of(detectMisordering.get().value) : detectMisordering)
            .write("caller-propagation", callerPropagation.hasValue()
                    ? Attribute.of(callerPropagation.get().value) : callerPropagation);

        if (!properties.isEmpty()) {
            ModelNodeResult readAttributeResult = ops.readAttribute(address, "properties");
            if (readAttributeResult.value().isDefined()) {
                for (ModelNode declaredPoperty : readAttributeResult.value().asList()) {
                    String key = declaredPoperty.asProperty().getName();
                    if (!properties.containsKey(key)) {
                        properties.put(key, Attribute.of(declaredPoperty.asProperty().getValue().asString()));
                    }
                }
            }
            ModelNode propertiesToSet = new ModelNode();
            for (Entry<String, Attribute<String>> property : properties.entrySet()) {
                Attribute<String> attrValue = property.getValue();
                if (attrValue.hasValue() && !attrValue.isUndefine()) {
                    propertiesToSet.get(property.getKey()).set(attrValue.get());
                }
            }
            batch.writeAttribute(address, "properties", propertiesToSet);
        }

        if (orbType == OrbType.IIOP) {
            // IIOP OPENJDK
            attributeWriter
                    .write("support-ssl",
                            supportSsl.hasValue() ? Attribute.of(supportSsl.get().openjdk) : supportSsl)
                    .write("transactions", transactions.hasValue()
                            ? Attribute.of(transactions.get().openjdk) : transactions)
                    .write("security", security.hasValue() ? Attribute.of(security.get().openjdk) : security)
                    .write("export-corbaloc", exportCorbaloc.hasValue()
                            ? Attribute.of(exportCorbaloc.get().openjdk) : exportCorbaloc)
                    .write("add-component-via-interceptor", addComponentViaInterceptor.hasValue()
                            ? Attribute.of(addComponentViaInterceptor.get().openjdk) : addComponentViaInterceptor)
                    .write("giop-version", giopVersion)
                    .write("persistent-server-id", persistentServerId);
        } else {
            // JACORB
            attributeWriter
                    .write("support-ssl",
                            supportSsl.hasValue() ? Attribute.of(supportSsl.get().jacorb) : supportSsl)
                    .write("transactions", transactions.hasValue()
                            ? Attribute.of(transactions.get().jacorb) : transactions)
                    .write("security", security.hasValue() ? Attribute.of(security.get().jacorb) : security)
                    .write("export-corbaloc", exportCorbaloc.hasValue()
                            ? Attribute.of(exportCorbaloc.get().jacorb) : exportCorbaloc)
                    .write("add-component-via-interceptor", addComponentViaInterceptor.hasValue()
                            ? Attribute.of(addComponentViaInterceptor.get().jacorb) : addComponentViaInterceptor)
                    .write("giop-minor-version", giopVersion);
        }

        ops.batch(batch);
    }

    public void apply(OfflineCommandContext ctx) throws Exception {
        OrbType orbType = OrbType.get(ctx.version);

        GroovyXmlTransform.Builder transform = GroovyXmlTransform.of(ChangeOrb.class)
            // generic settings
            .subtree("iiop", Subtree.subsystem(orbType.subsystemName()))
            .parameter("orbType", orbType)
            .parameter("subsystem", orbType.subsystemName())
            .parameter("isOpenjdkIiop", orbType == OrbType.IIOP)
            .parameter("isJacorb", orbType != OrbType.IIOP)
            // attributes
            .parameter("rootContext", rootContext)
            .parameter("socketBinding", socketBinding)
            .parameter("sslSocketBinding", sslSocketBinding)
            .parameter("securityDomain", securityDomain)
            .parameter("authMethod", authMethodPassword)
            .parameter("realm", realm)
            .parameter("required", authRequired)
            .parameter("properties", properties)
            .parameter("giopVersion", giopVersion)
            .parameter("clientRequires",
                    clientRequires.hasValue() ? Attribute.of(clientRequires.get().value) : clientRequires)
            .parameter("serverSupports",
                    serverSupports.hasValue() ? Attribute.of(serverSupports.get().value) : serverSupports)
            .parameter("serverRequires",
                    serverRequires.hasValue() ? Attribute.of(serverRequires.get().value) : serverRequires)
            .parameter("clientSupports",
                    clientSupports.hasValue() ? Attribute.of(clientSupports.get().value) : clientSupports)
            .parameter("integrity", integrity.hasValue() ? Attribute.of(integrity.get().value) : integrity)
            .parameter("confidentiality",
                    confidentiality.hasValue() ? Attribute.of(confidentiality.get().value) : confidentiality)
            .parameter("trustInTarget",
                    trustInTarget.hasValue() ? Attribute.of(trustInTarget.get().value) : trustInTarget)
            .parameter("trustInClient",
                    trustInClient.hasValue() ? Attribute.of(trustInClient.get().value) : trustInClient)
            .parameter("detectReplay",
                    detectReplay.hasValue() ? Attribute.of(detectReplay.get().value) : detectReplay)
            .parameter("detectMisordering", detectMisordering.hasValue()
                    ? Attribute.of(detectMisordering.get().value) : detectMisordering)
            .parameter("callerPropagation", callerPropagation.hasValue()
                    ? Attribute.of(callerPropagation.get().value) : callerPropagation);

        if (orbType == OrbType.IIOP) {
            transform
                .parameter("supportSsl",
                        supportSsl.hasValue() ? Attribute.of(supportSsl.get().openjdk) : supportSsl)
                .parameter("transactions",
                        transactions.hasValue() ? Attribute.of(transactions.get().openjdk) : transactions)
                .parameter("security", security.hasValue() ? Attribute.of(security.get().openjdk) : security)
                .parameter("exportCorbaloc",
                        exportCorbaloc.hasValue() ? Attribute.of(exportCorbaloc.get().openjdk) : exportCorbaloc)
                .parameter("addComponentViaInterceptor", addComponentViaInterceptor.hasValue()
                        ? Attribute.of(addComponentViaInterceptor.get().openjdk) : addComponentViaInterceptor)
                .parameter("persistentServerId", persistentServerId);
        } else {
            transform
                .parameter("supportSsl",
                        supportSsl.hasValue() ? Attribute.of(supportSsl.get().jacorb) : supportSsl)
                .parameter("transactions",
                        transactions.hasValue() ? Attribute.of(transactions.get().jacorb) : transactions)
                .parameter("security", security.hasValue() ? Attribute.of(security.get().jacorb) : security)
                .parameter("exportCorbaloc",
                        exportCorbaloc.hasValue() ? Attribute.of(exportCorbaloc.get().jacorb) : exportCorbaloc)
                .parameter("addComponentViaInterceptor", addComponentViaInterceptor.hasValue()
                        ? Attribute.of(addComponentViaInterceptor.get().jacorb) : addComponentViaInterceptor)
                .parameter("persistentServerId", Attribute.noValue());
        }

        ctx.client.apply(transform.build());
    }

    enum OrbType {
        // jacorb in AS7 and WFLY 8
        JACORB,
        // openjdk iiop in WFLY 9 and higher
        IIOP;

        static OrbType get(ServerVersion serverVersion) {
            if (serverVersion.lessThan(ServerVersion.VERSION_3_0_0)) {
                return JACORB;
            } else {
                return IIOP;
            }
        }

        String subsystemName() {
            return this == OrbType.IIOP ? OPENJDK_IIOP_SUBSYSTEM_NAME : JACORB_SUBSYSTEM_NAME;
        }
    }


    private static final class AttributeWriter {
        private final Address address;
        private final Batch batch;

        AttributeWriter(Address address, Batch batch) {
            this.address = address;
            this.batch = batch;
        }

        AttributeWriter write(String name, Attribute<?> value) {
            if (value == null) return this;
            if (value.isAbsent()) return this;

            if (value.isUndefine()) {
                batch.undefineAttribute(address, name);
            } else {
                batch.writeAttribute(address, name, value.get().toString());
            }
            return this;
        }
    }

    public static final class Builder {

        private Attribute<OnOff> supportSsl = Attribute.noValue();
        private Attribute<TransactionValues> transactions = Attribute.noValue();
        private Attribute<String> rootContext = Attribute.noValue();
        private Attribute<String> socketBinding = Attribute.noValue();
        private Attribute<String> sslSocketBinding = Attribute.noValue();
        private Attribute<AuthValues> clientRequires = Attribute.noValue();
        private Attribute<OnOff> addComponentViaInterceptor = Attribute.noValue();
        private Attribute<AuthValues> serverSupports = Attribute.noValue();
        private Attribute<String> giopVersion = Attribute.noValue();
        private Attribute<SecurityValues> security = Attribute.noValue();
        private Attribute<String> securityDomain = Attribute.noValue();
        private Attribute<AuthValues> serverRequires = Attribute.noValue();
        private Attribute<AuthValues> clientSupports = Attribute.noValue();
        private Attribute<OnOff> exportCorbaloc = Attribute.noValue();
        private Attribute<SupportedValues> integrity = Attribute.noValue();
        private Attribute<SupportedValues> confidentiality = Attribute.noValue();
        private Attribute<SupportedValues> trustInTarget = Attribute.noValue();
        private Attribute<SupportedValues> trustInClient = Attribute.noValue();
        private Attribute<SupportedValues> detectReplay = Attribute.noValue();
        private Attribute<SupportedValues> detectMisordering = Attribute.noValue();
        private Attribute<String> authMethodPassword = Attribute.noValue();
        private Attribute<String> realm = Attribute.noValue();
        private Attribute<Boolean> authRequired = Attribute.noValue();
        private Attribute<SupportedValues> callerPropagation = Attribute.noValue();
        private Attribute<String> persistentServerId = Attribute.noValue();
        private Map<String, Attribute<String>> properties = new HashMap<String, Attribute<String>>();

        public Builder transactions(TransactionValues transactions) {
            this.transactions = Attribute.of(transactions);
            return this;
        }

        public Builder supportSsl(Boolean supportSsl) {
            this.supportSsl = Attribute.of(OnOff.get(supportSsl));
            return this;
        }

        public Builder rootContext(String rootContext) {
            this.rootContext = Attribute.of(rootContext);
            return this;
        }

        public Builder socketBinding(String socketBinding) {
            this.socketBinding = Attribute.of(socketBinding);
            return this;
        }

        public Builder sslSocketBinding(String sslSocketBinding) {
            this.sslSocketBinding = Attribute.of(sslSocketBinding);
            return this;
        }

        public Builder clientRequires(AuthValues clientRequires) {
            this.clientRequires = Attribute.of(clientRequires);
            return this;
        }

        public Builder addComponentViaInterceptor(Boolean addComponentViaInterceptor) {
            this.addComponentViaInterceptor = Attribute.of(OnOff.get(addComponentViaInterceptor));
            return this;
        }

        public Builder serverSupports(AuthValues serverSupports) {
            this.serverSupports = Attribute.of(serverSupports);
            return this;
        }

        public Builder giopVersion(String giopVersion) {
            this.giopVersion = Attribute.of(giopVersion);
            return this;
        }

        public Builder security(SecurityValues security) {
            this.security = Attribute.of(security);
            return this;
        }

        public Builder securityDomain(String securityDomain) {
            this.securityDomain = Attribute.of(securityDomain);
            return this;
        }

        public Builder serverRequires(AuthValues serverRequires) {
            this.serverRequires = Attribute.of(serverRequires);
            return this;
        }

        public Builder clientSupports(AuthValues clientSupports) {
            this.clientSupports = Attribute.of(clientSupports);
            return this;
        }

        public Builder exportCorbaloc(Boolean exportCorbaloc) {
            this.exportCorbaloc = Attribute.of(OnOff.get(exportCorbaloc));
            return this;
        }

        public Builder property(String name, String value) {
            this.properties.put(name, Attribute.of(value));
            return this;
        }

        public Builder integrity(SupportedValues integrity) {
            this.integrity = Attribute.of(integrity);
            return this;
        }

        public Builder confidentiality(SupportedValues confidentiality) {
            this.confidentiality = Attribute.of(confidentiality);
            return this;
        }

        public Builder trustInTarget(SupportedValues trustInTarget) {
            this.trustInTarget = Attribute.of(trustInTarget);
            return this;
        }

        public Builder trustInClient(SupportedValues trustInClient) {
            this.trustInClient = Attribute.of(trustInClient);
            return this;
        }

        public Builder detectReplay(SupportedValues detectReplay) {
            this.detectReplay = Attribute.of(detectReplay);
            return this;
        }

        public Builder detectMisordering(SupportedValues detectMisordering) {
            this.detectMisordering = Attribute.of(detectMisordering);
            return this;
        }

        public Builder authMethodPassword(String authMethodPassword) {
            this.authMethodPassword = Attribute.of(authMethodPassword);
            return this;
        }

        public Builder authMethodNone() {
            this.authMethodPassword = Attribute.of("none");
            return this;
        }

        public Builder realm(String realm) {
            this.realm = Attribute.of(realm);
            return this;
        }

        public Builder authRequired(Boolean authRequired) {
            this.authRequired = Attribute.of(authRequired);
            return this;
        }

        public Builder callerPropagation(SupportedValues callerPropagation) {
            this.callerPropagation = Attribute.of(callerPropagation);
            return this;
        }

        public Builder persistentServerId(String persistentServerId) {
            this.persistentServerId = Attribute.of(persistentServerId);
            return this;
        }

        public Builder undefineRootContext() {
            this.rootContext = Attribute.undefine();
            return this;
        }

        public Builder undefineSecurity() {
            this.security = Attribute.undefine();
            return this;
        }

        public Builder undefineSslSocketBinding() {
            this.sslSocketBinding = Attribute.undefine();
            return this;
        }

        public Builder undefineTransactions() {
            this.transactions = Attribute.undefine();
            return this;
        }

        public Builder undefineSupportSsl() {
            this.supportSsl = Attribute.undefine();
            return this;
        }

        public Builder undefineAddComponentViaInterceptor() {
            this.addComponentViaInterceptor = Attribute.undefine();
            return this;
        }

        public Builder undefineSocketBinding() {
            this.socketBinding = Attribute.undefine();
            return this;
        }

        public Builder undefineGiopVersion() {
            this.giopVersion = Attribute.undefine();
            return this;
        }

        public Builder undefineClientRequires() {
            this.clientRequires = Attribute.undefine();
            return this;
        }

        public Builder undefineServerSupports() {
            this.serverSupports = Attribute.undefine();
            return this;
        }

        public Builder undefineSecurityDomain() {
            this.securityDomain = Attribute.undefine();
            return this;
        }

        public Builder undefineServerRequires() {
            this.serverRequires = Attribute.undefine();
            return this;
        }

        public Builder undefineProperty(String name) {
            this.properties.put(name, Attribute.undefineString());
            return this;
        }

        public Builder undefineClientSupports() {
            this.clientSupports = Attribute.undefine();
            return this;
        }

        public Builder undefineExportCorbaloc() {
            this.exportCorbaloc = Attribute.undefine();
            return this;
        }

        public Builder undefineIntegrity() {
            this.integrity = Attribute.undefine();
            return this;
        }

        public Builder undefineConfidentiality() {
            this.confidentiality = Attribute.undefine();
            return this;
        }

        public Builder undefineTrustInTarget() {
            this.trustInTarget = Attribute.undefine();
            return this;
        }

        public Builder undefineTrustInClient() {
            this.trustInClient = Attribute.undefine();
            return this;
        }

        public Builder undefineDetectReplay() {
            this.detectReplay = Attribute.undefine();
            return this;
        }

        public Builder undefineDetectMisordering() {
            this.detectMisordering = Attribute.undefine();
            return this;
        }

        public Builder undefineAuthMethodPassword() {
            this.authMethodPassword = Attribute.undefine();
            return this;
        }

        public Builder undefineAuthMethodNone() {
            this.authMethodPassword = Attribute.undefine();
            return this;
        }

        public Builder undefineRealm() {
            this.realm = Attribute.undefine();
            return this;
        }

        public Builder undefineAuthRequired() {
            this.authRequired = Attribute.undefine();
            return this;
        }

        public Builder undefineCallerPropagation() {
            this.callerPropagation = Attribute.undefine();
            return this;
        }

        public Builder undefinePersistentServerId() {
            this.persistentServerId = Attribute.undefine();
            return this;
        }

        public ChangeOrb build() {
            return new ChangeOrb(this);
        }
    }
}
