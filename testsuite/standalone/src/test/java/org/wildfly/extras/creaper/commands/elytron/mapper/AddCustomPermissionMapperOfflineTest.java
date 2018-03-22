package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractAddCustomOfflineTest;


@RunWith(Arquillian.class)
public class AddCustomPermissionMapperOfflineTest extends AbstractAddCustomOfflineTest {

    public String convertSubsystem(String subsystemString) {
        return subsystemString.replaceAll(PARENT_TYPE, "mappers")
            .replaceAll(CUSTOM_TYPE, "custom-permission-mapper")
            .replaceAll(CUSTOM_NAME, getAddCustomName())
            .replaceAll(CLASS_NAME, getClassName())
            .replaceAll(MODULE_NAME, getModuleName());
    }

    public String getAddCustomName() {
        return "customPermissionMapper";
    }

    public Object getBuilderObject(String name) {
        return new AddCustomPermissionMapper.Builder(name);
    }

    public String getModuleName() {
        return "org.jboss.custompermissionmapperimpl";
    }

    public String getClassName() {
        return "SomeCustomPermissionMapper";
    }
}
