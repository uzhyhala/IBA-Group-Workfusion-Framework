package com.ibagroup.workfusion.rpa.core.config;

import com.ibagroup.workfusion.rpa.core.BindingUtils;
import com.ibagroup.workfusion.rpa.core.CommonUtils;
import groovy.lang.Binding;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibagroup.workfusion.rpa.core.datastore.DataStoreQuery;
import com.ibagroup.workfusion.rpa.core.datastore.DataStoreQuery.RowItem;

public class DataStoreConfiguration implements ConfigurationManager {

    private static final String RPA_CONFIG_DS = "rpa_config_ds";

    private static final Logger logger = LoggerFactory.getLogger(DataStoreConfiguration.class);

    private Binding binding;
    private List<ConfigPair> configDs = null;
    private String dsName = null;

    public DataStoreConfiguration(Binding binding) {
        this.binding = binding;
    }

    public DataStoreConfiguration(Binding binding, String dsName) {
        this(binding);
        this.dsName = dsName;
    }

    /**
     * Gets the value from business process configuration data store with "key/value" format.
     *
     * @param keyParam - key of parammetr to retrieve
     * @return value
     */
    @Override
    public String getConfigItem(String keyParam) {
        // SecureEntryDTO
        logger.info("Trying to get config for: " + keyParam);
        if (configDs == null) {
            initConfigDs();
        }

        ConfigPair cfg = configDs.stream().filter(item -> keyParam.equalsIgnoreCase(item.getKey()) ? true : false).findAny().orElse(null);
        String result = cfg != null ? cfg.getValue() : null;
        logger.info("Value found: " + result);
        return result;
    }

    @Override
    public boolean isLocal() {
        return isLocal(binding);
    }

    public static boolean isLocal(Binding bnd) {
        String runname = BindingUtils.getWebHarvestTaskItem(bnd).getRun().getCampaignName();
        if (runname == null || runname.trim().isEmpty()) {
            runname = "local-run";
            return true;
        }
        return false;
    }

    private void initConfigDs() {
        if (StringUtils.isBlank(dsName)) {
            if (!initConfigDs(CommonUtils.getCustomAttribute(binding, RPA_CONFIG_DS))) {
                String dsFromInput = BindingUtils.getPropertyValue(binding, RPA_CONFIG_DS);
                logger.info("Custom attribute 'rpa_config_ds' is not define, using value from input file: " + dsFromInput);
                initConfigDs(dsFromInput);
                if (configDs == null) {
                    throw new IllegalArgumentException(
                            "Can't init config datastore nor from input file (" + RPA_CONFIG_DS + " column), nor from custom attribute (" + RPA_CONFIG_DS + ")");
                }
            }
        } else {
            logger.info("Init ds from " + dsName);
            initConfigDs(dsName);
        }

    }

    private boolean initConfigDs(String configDsName) {
        if (configDsName == null || StringUtils.isEmpty(configDsName)) {
            return false;
        }
        DataStoreQuery dataStoreAccess = new DataStoreQuery(binding);

        configDs = dataStoreAccess.executeQuery(configDsName, "select * from @this").getSelectResultAsListRows().get().stream().map(row -> {
            ConfigPair cfgDs = new ConfigPair();
            for (Iterator<RowItem> iter = row.iterator(); iter.hasNext();) {
                RowItem rowItem = iter.next();
                switch (rowItem.getColumn()) {
                    case "key":
                        cfgDs.setKey(rowItem.getValue());
                        break;
                    case "value":
                        cfgDs.setValue(rowItem.getValue());
                        break;
                    default:
                        break;
                }
            }
            return cfgDs;
        }).collect(Collectors.toList());

        return true;
    }

    private static class ConfigPair {
        private String key;
        private String value;

        private ConfigPair() {}

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}