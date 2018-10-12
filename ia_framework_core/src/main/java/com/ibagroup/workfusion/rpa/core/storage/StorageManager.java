package com.ibagroup.workfusion.rpa.core.storage;

import java.io.InputStream;
import java.util.List;

public interface StorageManager {

    boolean uploadFile(String path, InputStream input);

    List<String> listFiles(String path, String filter);

    InputStream getFile(String path);

    boolean deleteFile(String path);
}
