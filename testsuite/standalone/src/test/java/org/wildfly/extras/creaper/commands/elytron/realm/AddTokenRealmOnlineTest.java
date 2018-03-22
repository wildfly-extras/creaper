package org.wildfly.extras.creaper.commands.elytron.realm;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import org.bouncycastle.util.encoders.Base64;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.tls.AddClientSSLContext;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddTokenRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_TOKEN_REALM_NAME = "CreaperTestTokenRealm";
    private static final Address TEST_TOKEN_REALM_ADDRESS = SUBSYSTEM_ADDRESS
            .and("token-realm", TEST_TOKEN_REALM_NAME);
    private static final String TEST_TOKEN_REALM_NAME2 = "CreaperTestTokenRealm2";
    private static final Address TEST_TOKEN_REALM_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("token-realm", TEST_TOKEN_REALM_NAME2);

    private static final String TEST_CLIENT_SSL_CONTEXT = "CreaperTestClientSSLContext";
    private static final Address TEST_CLIENT_SSL_CONTEXT_ADDRESS = SUBSYSTEM_ADDRESS.and("client-ssl-context",
            TEST_CLIENT_SSL_CONTEXT);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_TOKEN_REALM_ADDRESS);
        ops.removeIfExists(TEST_TOKEN_REALM_ADDRESS2);
        ops.removeIfExists(TEST_CLIENT_SSL_CONTEXT_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleTokenRealmWithJwt() throws Exception {
        AddTokenRealm addTokenRealm = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .jwt(new AddTokenRealm.JwtBuilder().build())
                .build();

        client.apply(addTokenRealm);
        assertTrue("Token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS));
    }

    @Test
    public void addSimpleTokenRealmWithOauth() throws Exception {
        AddTokenRealm addTokenRealm = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .oauth2Introspection(new AddTokenRealm.Oauth2IntrospectionBuilder()
                        .clientId("someClient")
                        .clientSecret("someClientSecret")
                        .introspectionUrl("http://www.example.com")
                        .build())
                .build();

        client.apply(addTokenRealm);
        assertTrue("Token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS));
    }

    @Test
    public void addTwoSimpleTokenRealmsWithJwt() throws Exception {
        AddTokenRealm addTokenRealm = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .jwt(new AddTokenRealm.JwtBuilder()
                        .build())
                .build();

        AddTokenRealm addTokenRealm2 = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME2)
                .jwt(new AddTokenRealm.JwtBuilder().build())
                .build();

        client.apply(addTokenRealm);
        client.apply(addTokenRealm2);

        assertTrue("Token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS));
        assertTrue("Second token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS2));
    }

    @Test
    public void addTwoSimpleTokenRealmsWithOauth() throws Exception {
        AddTokenRealm addTokenRealm = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .oauth2Introspection(new AddTokenRealm.Oauth2IntrospectionBuilder()
                        .clientId("someClient")
                        .clientSecret("someClientSecret")
                        .introspectionUrl("http://www.example.com")
                        .build())
                .build();

        AddTokenRealm addTokenRealm2 = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME2)
                .oauth2Introspection(new AddTokenRealm.Oauth2IntrospectionBuilder()
                        .clientId("someClient")
                        .clientSecret("someClientSecret")
                        .introspectionUrl("http://www.example.com")
                        .build())
                .build();

        client.apply(addTokenRealm);
        client.apply(addTokenRealm2);

        assertTrue("Token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS));
        assertTrue("Second token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS2));
    }

    @Test
    public void addTwoSimpleTokenRealmsCombined() throws Exception {
        AddTokenRealm addTokenRealm = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .jwt(new AddTokenRealm.JwtBuilder().build())
                .build();

        AddTokenRealm addTokenRealm2 = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME2)
                .oauth2Introspection(new AddTokenRealm.Oauth2IntrospectionBuilder()
                        .clientId("someClient")
                        .clientSecret("someClientSecret")
                        .introspectionUrl("http://www.example.com")
                        .build())
                .build();

        client.apply(addTokenRealm);
        client.apply(addTokenRealm2);

        assertTrue("Token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS));
        assertTrue("Second token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS2));
    }

    @Test
    public void addFullTokenRealmWithJwt() throws Exception {
        String pemPublicKey = getPublicKeyInPmeFormat();
        AddTokenRealm addTokenRealm = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .jwt(new AddTokenRealm.JwtBuilder()
                        .addIssuer("someIssuer")
                        .addAudience("someAudience")
                        .publicKey(pemPublicKey)
                        .build())
                .principalClaim("somePrincipalClaim")
                .build();

        client.apply(addTokenRealm);
        assertTrue("Token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS));

        checkAttribute(TEST_TOKEN_REALM_ADDRESS, "jwt.issuer[0]", "someIssuer");
        checkAttribute(TEST_TOKEN_REALM_ADDRESS, "jwt.audience[0]", "someAudience");
        checkAttribute(TEST_TOKEN_REALM_ADDRESS, "jwt.public-key", pemPublicKey);
        checkAttribute(TEST_TOKEN_REALM_ADDRESS, "principal-claim", "somePrincipalClaim");
    }

    @Test
    public void addFullTokenRealmWithOauth() throws Exception {
        AddClientSSLContext addClientSSLContext = new AddClientSSLContext.Builder(TEST_CLIENT_SSL_CONTEXT)
                .build();
        client.apply(addClientSSLContext);
        assertTrue("SSL context should be created", ops.exists(TEST_CLIENT_SSL_CONTEXT_ADDRESS));

        AddTokenRealm addTokenRealm = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .oauth2Introspection(new AddTokenRealm.Oauth2IntrospectionBuilder()
                        .clientId("someClient")
                        .clientSecret("someClientSecret")
                        .introspectionUrl("http://www.example.com")
                        .clientSslContext(TEST_CLIENT_SSL_CONTEXT)
                        .hostNameVerificationPolicy("ANY")
                        .build())
                .principalClaim("somePrincipalClaim")
                .build();

        client.apply(addTokenRealm);
        assertTrue("Token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS));

        checkAttribute(TEST_TOKEN_REALM_ADDRESS, "oauth2-introspection.client-id", "someClient");
        checkAttribute(TEST_TOKEN_REALM_ADDRESS, "oauth2-introspection.client-secret", "someClientSecret");
        checkAttribute(TEST_TOKEN_REALM_ADDRESS, "oauth2-introspection.introspection-url", "http://www.example.com");
        checkAttribute(TEST_TOKEN_REALM_ADDRESS, "oauth2-introspection.client-ssl-context", TEST_CLIENT_SSL_CONTEXT);
        checkAttribute(TEST_TOKEN_REALM_ADDRESS, "oauth2-introspection.host-name-verification-policy", "ANY");
        checkAttribute(TEST_TOKEN_REALM_ADDRESS, "principal-claim", "somePrincipalClaim");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistTokenRealmNotAllowed() throws Exception {
        AddTokenRealm addTokenRealm = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .jwt(new AddTokenRealm.JwtBuilder()
                        .addIssuer("someIssuer")
                        .addAudience("someAudience")
                        .build())
                .build();

        AddTokenRealm addTokenRealm2 = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .jwt(new AddTokenRealm.JwtBuilder()
                        .addIssuer("someOtherIssuer")
                        .addAudience("someAudience")
                        .build())
                .build();

        client.apply(addTokenRealm);
        assertTrue("Token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS));
        client.apply(addTokenRealm2);
        fail("Token realm CreaperTestTokenRealm already exists in configuration, exception should be thrown");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistTokenRealmAllowed() throws Exception {
        AddTokenRealm addTokenRealm = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .jwt(new AddTokenRealm.JwtBuilder()
                        .addIssuer("someIssuer")
                        .addAudience("someAudience")
                        .build())
                .build();

        AddTokenRealm addTokenRealm2 = new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .jwt(new AddTokenRealm.JwtBuilder()
                        .addIssuer("someOtherIssuer")
                        .addAudience("someAudience")
                        .build())
                .build();

        client.apply(addTokenRealm);
        assertTrue("Token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS));
        client.apply(addTokenRealm2);
        assertTrue("Token realm should be created", ops.exists(TEST_TOKEN_REALM_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_TOKEN_REALM_ADDRESS, "jwt.issuer[0]", "someOtherIssuer");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTokenRealm_nullName() throws Exception {
        new AddTokenRealm.Builder(null)
                .jwt(new AddTokenRealm.JwtBuilder()
                        .addIssuer("someIssuer")
                        .addAudience("someAudience")
                        .build())
                .build();

        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTokenRealm_emptyName() throws Exception {
        new AddTokenRealm.Builder("")
                .jwt(new AddTokenRealm.JwtBuilder()
                        .addIssuer("someIssuer")
                        .addAudience("someAudience")
                        .build())
                .build();

        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTokenRealmWithJwt_nullIssuer() throws Exception {
        new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .jwt(new AddTokenRealm.JwtBuilder()
                        .addIssuer(null)
                        .addAudience("someAudience")
                        .build())
                .build();

        fail("Creating command with null issuer should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTokenRealmWithJwt_nullAudience() throws Exception {
        new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .jwt(new AddTokenRealm.JwtBuilder()
                        .addIssuer("someIssuer")
                        .addAudience(null)
                        .build())
                .build();

        fail("Creating command with null audience should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTokenRealmWithOauth_nullClientId() throws Exception {
        new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .oauth2Introspection(new AddTokenRealm.Oauth2IntrospectionBuilder()
                        .clientId(null)
                        .clientSecret("someClientSecret")
                        .introspectionUrl("http://www.example.com")
                        .build())
                .build();

        fail("Creating command with null client-id should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTokenRealmWithOauth_emptyClientId() throws Exception {
        new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .oauth2Introspection(new AddTokenRealm.Oauth2IntrospectionBuilder()
                        .clientId("")
                        .clientSecret("someClientSecret")
                        .introspectionUrl("http://www.example.com")
                        .build())
                .build();

        fail("Creating command with empty client-id should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTokenRealmWithOauth_nullClientSecret() throws Exception {
        new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .oauth2Introspection(new AddTokenRealm.Oauth2IntrospectionBuilder()
                        .clientId("someClient")
                        .clientSecret(null)
                        .introspectionUrl("http://www.example.com")
                        .build())
                .build();

        fail("Creating command with null client-secret should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTokenRealmWithOauth_emptyClientSecret() throws Exception {
        new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .oauth2Introspection(new AddTokenRealm.Oauth2IntrospectionBuilder()
                        .clientId("someClient")
                        .clientSecret("")
                        .introspectionUrl("http://www.example.com")
                        .build())
                .build();

        fail("Creating command with empty client-secret should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTokenRealmWithOauth_nullIntrospectionUrl() throws Exception {
        new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .oauth2Introspection(new AddTokenRealm.Oauth2IntrospectionBuilder()
                        .clientId("someClient")
                        .clientSecret("someClientSecret")
                        .introspectionUrl(null)
                        .build())
                .build();

        fail("Creating command with null introspection-url should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTokenRealmWithOauth_emptyIntrospectionUrl() throws Exception {
        new AddTokenRealm.Builder(TEST_TOKEN_REALM_NAME)
                .oauth2Introspection(new AddTokenRealm.Oauth2IntrospectionBuilder()
                        .clientId("someClient")
                        .clientSecret("someClientSecret")
                        .introspectionUrl("")
                        .build())
                .build();

        fail("Creating command with empty introspection-url should throw exception");
    }

    private static String getPublicKeyInPmeFormat() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair pair = keyGen.generateKeyPair();
        PublicKey pub = pair.getPublic();

        String pemPublicKey = "-----BEGIN PUBLIC KEY-----\n";
        pemPublicKey += Base64.toBase64String(pub.getEncoded());
        pemPublicKey += "\n-----END PUBLIC KEY-----";

        return pemPublicKey;
    }
}
