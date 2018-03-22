package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.commands.elytron.AbstractAddCustomOfflineTest;

public class AddCustomRoleMapperOfflineTest extends AbstractAddCustomOfflineTest {

    public String convertSubsystem(String subsystemString) {
        return subsystemString.replaceAll(PARENT_TYPE, "mappers")
            .replaceAll(CUSTOM_TYPE, "custom-role-mapper")
            .replaceAll(CUSTOM_NAME, getAddCustomName())
            .replaceAll(CLASS_NAME, getClassName())
            .replaceAll(MODULE_NAME, getModuleName());
    }

    public String getAddCustomName() {
        return "customRoleMapper";
    }

    public Object getBuilderObject(String name) {
        return new AddCustomRoleMapper.Builder(name);
    }

    public String getModuleName() {
        return "org.jboss.customrolemapperimpl";
    }

    public String getClassName() {
        return "SomeCustomRoleMapper";
    }
}
