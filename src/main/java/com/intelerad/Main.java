package com.intelerad;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.internal.crt.S3CrtAsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedDownload;
import software.amazon.awssdk.transfer.s3.model.DownloadRequest;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class Main {
    private static final String REGION = "us-east-2";
    private static final String AWS_ACCOUNT_1 = "AWSACCOUNT1";
    private static final String AWS_ACCOUNT_2 = "AWSACCOUNT2";
    private static final String AWS_ACCOUNT_3 = "AWSACCOUNT3";
    private static final String[][] objects = {
            { AWS_ACCOUNT_1, "bucket1-in-account1", "object-in-bucket1" },
            { AWS_ACCOUNT_2, "bucket1-in-account2", "object-in-bucket2" },
            { AWS_ACCOUNT_3, "bucket1-in-account3", "object-in-bucket3" },
    };

    private static StsClient stsClient;
    private static Map<String, S3AsyncClient> asyncClients;

    private static boolean anyFailed = false;

    public static void main(String[] argv) throws Exception {
        init();
        test();

        if(anyFailed) {
            System.err.println("At least one failure");
            System.exit(1);
        }
        System.out.println("All succeeded");
    }

    private static void init() {
        stsClient = StsClient.builder()
                .region(Region.of(REGION))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        asyncClients = new ConcurrentHashMap<>();
    }

    private static S3AsyncClient getAsyncClient(String awsAccountId) {
        return asyncClients.computeIfAbsent(awsAccountId, s -> {
            String roleArn = "arn:aws:iam::" + awsAccountId + ":role/S3ObjectManager";
            AssumeRoleRequest roleRequest = AssumeRoleRequest.builder()
                    .durationSeconds(900)
                    .roleArn(roleArn)
                    .roleSessionName("storage-service")
                    .build();
            AwsCredentialsProvider credentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                    .stsClient(stsClient)
                    .refreshRequest(roleRequest)
                    .build();
            return S3CrtAsyncClient.builder()
                    .region(Region.of(REGION))
                    .credentialsProvider(credentialsProvider)
                    .maxConcurrency(8)
                    .build();
        });
    }

    private static S3TransferManager newTransferManager(String awsAccountId) {
        return S3TransferManager.builder()
                .s3Client(getAsyncClient(awsAccountId))
                .build();
    }

    private static void test() throws Exception {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 1000, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        // Download some files randomly
        for(int i = 0; i < 6; i++) {
            pool.submit(new PerformRandomDownload());
        }

        // Download each file once (random download may not get all files)
        for(int i = 0; i < objects.length; i++) {
            pool.submit(new PerformDownload().setIndex(i));
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.MINUTES);
    }

    private static void download(String acctid, String bucket, String key) {
        try {
            File target = new File(UUID.randomUUID() + ".tmp");
            target.deleteOnExit();

            try(S3TransferManager transferManager = newTransferManager(acctid)) {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();
                DownloadRequest<GetObjectResponse> downloadRequest = DownloadRequest.builder()
                        .getObjectRequest(getObjectRequest)
                        .responseTransformer(AsyncResponseTransformer.toFile(target))
                        .build();
                CompletedDownload<GetObjectResponse> downloadResponse =
                        transferManager.download(downloadRequest).completionFuture().join();
                boolean success = downloadResponse.result().sdkHttpResponse().isSuccessful();
                System.out.println(Thread.currentThread().getName() + " s3://" + bucket + "/" + key + " " + (success ? "succeeded" : "FAILED"));
                if (!success) {
                    anyFailed = true;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static class PerformRandomDownload implements Runnable {
        public void run() {
            int i = ThreadLocalRandom.current().nextInt(0, objects.length);
            String[] info = objects[i];
            download(info[0], info[1], info[2]);
        }
    }

    private static class PerformDownload implements Runnable {
        private int index;

        public PerformDownload setIndex(int index) {
            this.index = index;
            return this;
        }
        public void run() {
            String[] info = objects[index];
            download(info[0], info[1], info[2]);
        }
    }
}