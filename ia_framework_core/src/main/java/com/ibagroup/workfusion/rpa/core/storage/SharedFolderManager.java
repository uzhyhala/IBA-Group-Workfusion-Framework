package com.ibagroup.workfusion.rpa.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

/**
 * Class is responsible for reading and uploading of files to shared folder
 */
public class SharedFolderManager implements StorageManager {

    private NtlmPasswordAuthentication auth;

    public SharedFolderManager(String domain, String user, String password) {
        this.auth = new NtlmPasswordAuthentication(domain, user, password);
    }

    /**
     * Function uploads file on shared folder
     * 
     * @param bytes file content
     * @param path path on shared folder
     */
    @Override
    public boolean uploadFile(String path, InputStream input) {
        try {
            SmbFile sFile = new SmbFile(path, auth);
            SmbFileOutputStream sfos = new SmbFileOutputStream(sFile);
            sfos.write(IOUtils.toByteArray(input));
            sfos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * Function returns Input Stream from a file on shared folder
     *
     * @param path full path to file on shared folder
     */
    @Override
    public InputStream getFile(String path) {
        try {
            return new SmbFile(path, auth).getInputStream();
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listFiles(String path, String filter) {
        try {
            return Arrays.asList(new SmbFile(path, auth).list());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteFile(String path) {
        try {
            new SmbFile(path, auth).delete();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
