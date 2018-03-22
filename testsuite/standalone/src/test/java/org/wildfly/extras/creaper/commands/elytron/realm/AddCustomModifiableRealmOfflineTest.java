package org.wildfly.extras.creaper.commands.elytron.realm;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractAddCustomOfflineTest;


@RunWith(Arquillian.class)
public class AddCustomModifiableRealmOfflineTest extends AbstractAddCustomOfflineTest {

    public String convertSubsystem(String subsystemString) {
        return subsystemString.replaceAll(PARENT_TYPE, "security-realms")
            .replaceAll(CUSTOM_TYPE, "custom-realm")
            .replaceAll(CUSTOM_NAME, getAddCustomName())
            .replaceAll(CLASS_NAME, getClassName())
            .replaceAll(MODULE_NAME, getModuleName());
    }

    public String getAddCustomName() {
        return "customRealm";
    }

    public Object getBuilderObject(String name) {
        return new AddCustomRealm.Builder(name);
    }

    public String getModuleName() {
        return "org.jboss.customrealmimpl";
    }

    public String getClassName() {
        return "SomeCustomRealm";
    }
}
