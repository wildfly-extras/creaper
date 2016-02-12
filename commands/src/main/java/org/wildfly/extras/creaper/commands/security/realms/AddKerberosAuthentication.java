package org.wildfly.extras.creaper.commands.security.realms;

import java.util.ArrayList;
import java.util.List;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Add kerberos authentication to security realm.
 *
 * <b>This command is available since WildFly 9 or in EAP 6.4.x!</b>
 */
public final class AddKerberosAuthentication extends AbstractAddSecurityRealmSubElement {

    private final Boolean removeRealm;
    private final List<KerberosKeytab> keytabs;

    private AddKerberosAuthentication(Builder builder) {
        super(builder);
        this.removeRealm = builder.removeRealm;
        this.keytabs = builder.keytabs;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                || ctx.version.inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
            throw new AssertionError("Kerberos authentication in security realm is available since WildFly 9 or in EAP 6.4.x.");
        }

        Operations ops = new Operations(ctx.client);

        Address securityServerIdentitiesAddress = securityRealmAddress.and("server-identity", "kerberos");
        Address securityRealmKerberosAuthnAddress = securityRealmAddress.and("authentication", "kerberos");

        if (replaceExisting) {
            Batch batch = new Batch();
            boolean krbServerIdentityExists = ops.exists(securityServerIdentitiesAddress);
            if (krbServerIdentityExists) {
                batch.remove(securityServerIdentitiesAddress);
            }

            boolean krbAuthenticationExists = ops.exists(securityRealmKerberosAuthnAddress);
            if (krbAuthenticationExists) {
                batch.remove(securityRealmKerberosAuthnAddress);
            }
            ops.batch(batch);
            new Administration(ctx.client).reloadIfRequired();
        }

        Batch batch = new Batch();
        boolean krbServerIdentityExists = ops.exists(securityServerIdentitiesAddress);
        if (!krbServerIdentityExists) {
            batch.add(securityServerIdentitiesAddress);
        }

        for (KerberosKeytab keytab : keytabs) {
            batch.add(securityServerIdentitiesAddress.and("keytab", keytab.getPrincipal()), Values.empty()
                    .andOptional("path", keytab.getPath())
                    .andOptional("relative-to", keytab.getRelativeTo())
                    .andListOptional(String.class, "for-hosts", keytab.getForHosts())
                    .andOptional("debug", keytab.getDebug()));
        }
        batch.add(securityRealmKerberosAuthnAddress, Values.empty()
                .andOptional("remove-realm", removeRealm));

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddKerberosAuthentication.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealmName", securityRealmName)
                .parameter("atrRemoveRealm", removeRealm)
                .parameter("atrKeytabs", keytabs)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder extends AbstractAddSecurityRealmSubElement.Builder<Builder> {

        private Boolean removeRealm;
        private List<KerberosKeytab> keytabs = new ArrayList<KerberosKeytab>();

        public Builder(String securityRealmName) {
            super(securityRealmName);
        }

        public Builder removeRealm(boolean removeRealm) {
            this.removeRealm = removeRealm;
            return this;
        }

        public Builder addKeytab(KerberosKeytab keytab) {
            keytabs.add(keytab);
            return this;
        }

        @Override
        public AddKerberosAuthentication build() {
            if (keytabs.size() < 1) {
                throw new IllegalArgumentException("At least one keytab has to be defined.");
            }
            return new AddKerberosAuthentication(this);
        }
    }
}
