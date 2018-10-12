package com.ibagroup.workfusion.rpa.core.metadata.storage;

public interface MetadataPermanentStorage {

    boolean storeAllMetadata();

    boolean storeAllMetadata(String uploadUid);

}
