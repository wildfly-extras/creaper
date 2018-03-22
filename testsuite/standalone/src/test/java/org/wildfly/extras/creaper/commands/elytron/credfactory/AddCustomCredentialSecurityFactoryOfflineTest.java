package org.wildfly.extras.creaper.commands.elytron.credfactory;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractAddCustomOfflineTest;


@RunWith(Arquillian.class)
public class AddCustomCredentialSecurityFactoryOfflineTest extends AbstractAddCustomOfflineTest {

    public String convertSubsystem(String subsystemString) {
        return subsystemString.replaceAll(PARENT_TYPE, "credential-security-factories")
            .replaceAll(CUSTOM_TYPE, "custom-credential-security-factory")
            .replaceAll(CUSTOM_NAME, getAddCustomName())
            .replaceAll(CLASS_NAME, getClassName())
            .replaceAll(MODULE_NAME, getModuleName());
    }

    public String getAddCustomName() {
        return "customCredSecFac";
    }

    public Object getBuilderObject(String name) {
        return new AddCustomCredentialSecurityFactory.Builder(name);
    }

    public String getModuleName() {
        return "org.jboss.customcredsecfacimpl";
    }

    public String getClassName() {
        return "SomeCustomCredentialSecurityFactory";
    }
}
