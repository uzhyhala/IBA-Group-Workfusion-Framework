package com.ibagroup.workfusion.rpa.core.config;

import com.ibagroup.workfusion.rpa.core.BindingUtils;
import groovy.lang.Binding;

public class RuntimeConfiguration implements ConfigurationManager {

    private Binding binding;

    public RuntimeConfiguration(Binding binding) {
        this.binding = binding;
    }

    @Override
    public String getConfigItem(String keyParameter) {
        return BindingUtils.getPropertyValue(binding, keyParameter);
    }

    @Override
    public boolean isLocal() {
        return false;
    }
}
