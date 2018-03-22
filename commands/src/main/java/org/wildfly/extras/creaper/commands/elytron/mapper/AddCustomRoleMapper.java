package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.commands.elytron.AbstractAddCustom;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;

public final class AddCustomRoleMapper extends AbstractAddCustom {

    private AddCustomRoleMapper(Builder builder) {
        super(builder);
    }

    @Override
    protected String getCustomTypeName() {
        return "custom-role-mapper";
    }

    @Override
    protected GroovyXmlTransform.Builder getGroovyBuilder() {
        return GroovyXmlTransform.of(AddCustomRoleMapper.class);
    }

    public static final class Builder extends AbstractAddCustom.Builder<Builder> {
        public Builder(String name) {
            super(name);
        }

        public AddCustomRoleMapper build() {
            checkClassNameAndModule();
            return new AddCustomRoleMapper(this);
        }
    }
}
