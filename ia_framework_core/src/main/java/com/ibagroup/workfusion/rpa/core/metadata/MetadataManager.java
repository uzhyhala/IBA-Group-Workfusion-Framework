package com.ibagroup.workfusion.rpa.core.metadata;

import java.util.List;
import com.ibagroup.workfusion.rpa.core.metadata.types.Metadata;

public interface MetadataManager {

    void addMetadata(Metadata... metadata);

    List<Metadata> getMetadataList();

    void clearMetadata();

}