package org.wildfly.extras.creaper.commands.elytron.securityproperty;

import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;

@RunWith(Arquillian.class)
public class AddSecurityPropertyOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SECURITY_PROPERTY_NAME = "CreaperTestSecurityProperty";
    private static final String TEST_SECURITY_PROPERTY_FULL_NAME = "security-properties."
            + TEST_SECURITY_PROPERTY_NAME;
    private static final String TEST_SECURITY_PROPERTY_NAME2 = "CreaperTestSecurityProperty2";
    private static final String TEST_SECURITY_PROPERTY_FULL_NAME2 = "security-properties."
            + TEST_SECURITY_PROPERTY_NAME2;

    @After
    public void cleanup() throws Exception {
        ops.undefineAttribute(SUBSYSTEM_ADDRESS, TEST_SECURITY_PROPERTY_FULL_NAME);
        ops.undefineAttribute(SUBSYSTEM_ADDRESS, TEST_SECURITY_PROPERTY_FULL_NAME2);
    }

    @Test
    public void addSecurityProperty() throws Exception {
        AddSecurityProperty addSecurityProperty = new AddSecurityProperty.Builder(TEST_SECURITY_PROPERTY_NAME)
                .value("someSecretValue")
                .build();
        client.apply(addSecurityProperty);

        checkAttribute(SUBSYSTEM_ADDRESS, TEST_SECURITY_PROPERTY_FULL_NAME, "someSecretValue");
    }

    @Test
    public void addSecurityProperties() throws Exception {
        AddSecurityProperty addSecurityProperty = new AddSecurityProperty.Builder(TEST_SECURITY_PROPERTY_NAME)
                .value("someSecretValue")
                .build();

        AddSecurityProperty addSecurityProperty2 = new AddSecurityProperty.Builder(TEST_SECURITY_PROPERTY_NAME2)
                .value("someSecretValue2")
                .build();

        client.apply(addSecurityProperty);
        client.apply(addSecurityProperty2);

        checkAttribute(SUBSYSTEM_ADDRESS, TEST_SECURITY_PROPERTY_FULL_NAME, "someSecretValue");
        checkAttribute(SUBSYSTEM_ADDRESS, TEST_SECURITY_PROPERTY_FULL_NAME2, "someSecretValue2");
    }

    @Test
    public void addSecurityPropertyAllowed() throws Exception {
        AddSecurityProperty addSecurityProperty = new AddSecurityProperty.Builder(TEST_SECURITY_PROPERTY_NAME)
                .value("someSecretValue")
                .build();

        AddSecurityProperty addSecurityProperty2 = new AddSecurityProperty.Builder(TEST_SECURITY_PROPERTY_NAME)
                .value("differentSecretValue")
                .build();

        client.apply(addSecurityProperty);
        checkAttribute(SUBSYSTEM_ADDRESS, TEST_SECURITY_PROPERTY_FULL_NAME, "someSecretValue");
        client.apply(addSecurityProperty2);
        // check whether it was really rewritten
        checkAttribute(SUBSYSTEM_ADDRESS, TEST_SECURITY_PROPERTY_FULL_NAME, "differentSecretValue");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityProperty_nullKey() throws Exception {
        new AddSecurityProperty.Builder(null)
                .value("someSecretValue")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityProperty_emptyKey() throws Exception {
        new AddSecurityProperty.Builder("")
                .value("someSecretValue")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityProperty_nullValue() throws Exception {
        new AddSecurityProperty.Builder(TEST_SECURITY_PROPERTY_NAME)
                .value(null)
                .build();
        fail("Creating command with null value should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityProperty_emptyValue() throws Exception {
        new AddSecurityProperty.Builder(TEST_SECURITY_PROPERTY_NAME)
                .value("")
                .build();
        fail("Creating command with empty value should throw exception");
    }

}
