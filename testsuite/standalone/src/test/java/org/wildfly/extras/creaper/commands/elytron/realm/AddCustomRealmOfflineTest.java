package org.wildfly.extras.creaper.commands.elytron.realm;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractAddCustomOfflineTest;


@RunWith(Arquillian.class)
public class AddCustomRealmOfflineTest extends AbstractAddCustomOfflineTest  {

    public String convertSubsystem(String subsystemString) {
        return subsystemString.replaceAll(PARENT_TYPE, "security-realms")
            .replaceAll(CUSTOM_TYPE, "custom-modifiable-realm")
            .replaceAll(CUSTOM_NAME, getAddCustomName())
            .replaceAll(CLASS_NAME, getClassName())
            .replaceAll(MODULE_NAME, getModuleName());
    }

    public String getAddCustomName() {
        return "customModifiableRealm";
    }

    public Object getBuilderObject(String name) {
        return new AddCustomModifiableRealm.Builder(name);
    }

    public String getModuleName() {
        return "org.jboss.custommodifiablerealmimpl";
    }

    public String getClassName() {
        return "SomeCustomModifiableRealm";
    }
}
