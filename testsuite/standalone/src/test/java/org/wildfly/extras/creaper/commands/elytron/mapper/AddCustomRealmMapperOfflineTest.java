package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.commands.elytron.AbstractAddCustomOfflineTest;

public class AddCustomRealmMapperOfflineTest extends AbstractAddCustomOfflineTest {

    public String convertSubsystem(String subsystemString) {
        return subsystemString.replaceAll(PARENT_TYPE, "mappers")
            .replaceAll(CUSTOM_TYPE, "custom-realm-mapper")
            .replaceAll(CUSTOM_NAME, getAddCustomName())
            .replaceAll(CLASS_NAME, getClassName())
            .replaceAll(MODULE_NAME, getModuleName());
    }

    public String getAddCustomName() {
        return "customRealmMapper";
    }

    public Object getBuilderObject(String name) {
        return new AddCustomRealmMapper.Builder(name);
    }

    public String getModuleName() {
        return "org.jboss.customrealmmapperimpl";
    }

    public String getClassName() {
        return "SomeCustomRealmMapper";
    }
}
