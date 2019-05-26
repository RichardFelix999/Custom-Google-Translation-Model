package com.formentor.google.storage;

import com.google.cloud.storage.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StorageApi {

    /**
     * Creates a bucket
     * @param bucketName The name for the new bucket
     * @return
     */
    public static Bucket createBucket(String bucketName) {
        // Instantiates a client
        // java.io.IOException: The Application Default Credentials are not available. They are available if running in Google Compute Engine.
        // Otherwise, the environment variable GOOGLE_APPLICATION_CREDENTIALS must be defined pointing to a file defining the credentials (service-account.json).
        // See https://developers.google.com/accounts/docs/application-default-credentials for more information.
        Storage storage = StorageOptions.getDefaultInstance().getService();

        // Creates the new bucket
        Bucket bucket = storage.create(BucketInfo.of(bucketName));

        System.out.printf("Bucket %s created.%n", bucket.getName());

        return bucket;
    }

    public static Blob createBlob(String bucketName, String blobName, String content) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(bucketName, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("text/plain")
                // Modify access list to allow all users with link to read file
                // .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER))))
                .build();
        Blob blob = storage.create(blobInfo, content.getBytes(UTF_8));
        return blob;
    }

    public static Bucket getBucketByName(String bucketName) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        for (Bucket bucket: storage.list().iterateAll()) {
            if (bucketName.equals(bucket.getName())) {
                return bucket;
            }
        }

        return null;
    }

}
