package com.ibagroup.workfusion.rpa.core.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ibagroup.workfusion.rpa.core.BindingUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.utils.CommonUtil;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.workfusion.utils.client.AmazonUtils;

import groovy.lang.Binding;

public class S3Manager implements StorageManager {

    private final String initFldr;
    private final String bucket;
    private final CannedAccessControlList acl;
    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3EndpointUrl;
    private static final Logger logger = LoggerFactory.getLogger(S3Manager.class);

    public S3Manager(Binding binding, String bucket, String folder) {
        this(binding, bucket, folder, CannedAccessControlList.PublicRead);
    }

    public S3Manager(Binding binding, String bucket, String folder, CannedAccessControlList acl) {
        s3EndpointUrl = BindingUtils.getPropertyValue(binding, "s3EndpointUrl");
        s3AccessKey = BindingUtils.getPropertyValue(binding, "s3AccessKey");
        s3SecretKey = BindingUtils.getPropertyValue(binding, "s3SecretKey");

        this.bucket = bucket;

        String _initFldr = !CommonUtil.isEmptyString(folder) ? folder : "";
        if (!_initFldr.endsWith("/") && !_initFldr.isEmpty()) {
            _initFldr += "/";
        }
        this.initFldr = _initFldr;
        this.acl = acl;
    }

    @Override
    public boolean uploadFile(String path_, InputStream input) {
        return uploadFile(path_, input, "");
    }

    public boolean uploadFile(String path_, InputStream input, String contentType) {
        try {
            String path = initFldr + path_;
            logger.info("Uploading file into bucket: " + bucket + " and path: " + path);

            return wrapTransferInvoke((client) -> {
                if (!(client.doesBucketExist(bucket))) {
                    client.createBucket(new CreateBucketRequest(bucket));
                }
                ObjectMetadata objectMetadata = new ObjectMetadata();
                if (StringUtils.isNotBlank(contentType)) {
                    objectMetadata.setContentType(contentType);
                }
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, path, input,
                    objectMetadata).withCannedAcl(acl);

                client.putObject(putObjectRequest);

                return true;
            });

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    
    public boolean uploadFileWithFullAccess(String fullPath, InputStream input) {
    	
    	AccessControlList acl = new AccessControlList();
    	acl.grantPermission(GroupGrantee.AllUsers, Permission.FullControl);

        try {
            String path = initFldr + fullPath;
            logger.info("Uploading file into bucket: " + bucket + " and path: " + path);

            return wrapTransferInvoke(client -> {
                if (!(client.doesBucketExist(bucket))) {
                    client.createBucket(new CreateBucketRequest(bucket));
                }
                
                
                
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, path, input, new ObjectMetadata()).withAccessControlList(acl);

                client.putObject(putObjectRequest);

                return true;
            });

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private <T, R> R wrapTransferInvoke(Function<AmazonS3, R> func) {
        AmazonS3 s3ClientConnection = AmazonUtils.createS3Client(s3AccessKey, s3SecretKey, s3EndpointUrl, null);
        TransferManager manager = new TransferManager(s3ClientConnection);
        try {
            return func.apply(manager.getAmazonS3Client());
        } finally {
            manager.shutdownNow();
        }
    }

    @Override
    public List<String> listFiles(String path, String filter) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.withBucketName(bucket);
        listObjectsRequest.withPrefix(path);
        final Pattern pattern = Pattern.compile(filter);
        ObjectListing listObjects = wrapTransferInvoke((client) -> client.listObjects(listObjectsRequest));

        return listObjects.getObjectSummaries().stream().filter((S3ObjectSummary obj) -> {
            return pattern.matcher(obj.getKey()).matches();
        }).map(obj -> obj.getKey()).collect(Collectors.toList());
    }

    @Override
    public InputStream getFile(String path) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, path);

        return wrapTransferInvoke((client) -> {
            S3Object object = client.getObject(getObjectRequest);
            S3ObjectInputStream objectContent = object.getObjectContent();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                IOUtils.copy(objectContent, byteArrayOutputStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        });
    }

    @Override
    public boolean deleteFile(String path) {
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, path);
        return wrapTransferInvoke((client) -> {
            client.deleteObject(deleteObjectRequest);
            return true;
        });
    }

}
