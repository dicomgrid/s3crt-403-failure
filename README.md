# Reproduce S3 CRT Async client 403 across multiple accounts
## Prerequisites
* Java 8
* AWS SDK v2
* 3 AWS Accounts with roles that allow "AssumeRole" and have trusted relationship to main AWS account where EC2 instance resides
* S3 Buckets have bucket policy that allows main AWS account to execute operations against its objects
* Main AWS Account with EC2 instance that has sts:AssumeRole privileges across the 3 target AWS accounts
## Build
* Update AWS_ACCOUNT constants and objects array to point to actual accounts, buckets, and existing objects
* `mvn clean package`
## Run
* Connect to EC2 Instance with instance role
* `java -jar s3crt-403-failure-1.0-SNAPSHOT-jar-with-dependencies.jar`
## Expected Output
Some successful downloads of the objects like:
```
pool-2-thread-3 s3://bucket-1/myobject.zip succeeded
pool-2-thread-5 s3://bucket-2/myobject2.zip
pool-2-thread-1 s3://bucket-3/myobject3.zip succeeded
```
and some exception traces for the 403 access denied:
```
java.util.concurrent.CompletionException: software.amazon.awssdk.services.s3.model.S3Exception: null (Service: S3, Status Code: 403, Request ID: 7XYQ9TD0GP2XB1VS, Extended Request ID: 1CGjM3myJuXuW3IO558rlb8fwvj+cChFXvi54W6r0OlkVlYj1oKXK1yUrxN7hyL4Ua4Lt9zkplHvct76x0SytQ==)
	at software.amazon.awssdk.utils.CompletableFutureUtils.errorAsCompletionException(CompletableFutureUtils.java:65)
	at software.amazon.awssdk.core.internal.http.pipeline.stages.AsyncExecutionFailureExceptionReportingStage.lambda$execute$0(AsyncExecutionFailureExceptionReportingStage.java:51)
	at java.util.concurrent.CompletableFuture.uniHandle(CompletableFuture.java:822)
	at java.util.concurrent.CompletableFuture$UniHandle.tryFire(CompletableFuture.java:797)
	at java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:474)
	at java.util.concurrent.CompletableFuture.completeExceptionally(CompletableFuture.java:1977)
	at software.amazon.awssdk.utils.CompletableFutureUtils.lambda$forwardExceptionTo$0(CompletableFutureUtils.java:79)
	at java.util.concurrent.CompletableFuture.uniWhenComplete(CompletableFuture.java:760)
	at java.util.concurrent.CompletableFuture$UniWhenComplete.tryFire(CompletableFuture.java:736)
	at java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:474)
	at java.util.concurrent.CompletableFuture.completeExceptionally(CompletableFuture.java:1977)
	at software.amazon.awssdk.core.internal.http.pipeline.stages.AsyncRetryableStage$RetryingExecutor.maybeAttemptExecute(AsyncRetryableStage.java:103)
	at software.amazon.awssdk.core.internal.http.pipeline.stages.AsyncRetryableStage$RetryingExecutor.maybeRetryExecute(AsyncRetryableStage.java:184)
	at software.amazon.awssdk.core.internal.http.pipeline.stages.AsyncRetryableStage$RetryingExecutor.lambda$attemptExecute$1(AsyncRetryableStage.java:170)
	at java.util.concurrent.CompletableFuture.uniWhenComplete(CompletableFuture.java:760)
	at java.util.concurrent.CompletableFuture$UniWhenComplete.tryFire(CompletableFuture.java:736)
	at java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:474)
	at java.util.concurrent.CompletableFuture.complete(CompletableFuture.java:1962)
	at software.amazon.awssdk.core.internal.http.pipeline.stages.MakeAsyncHttpRequestStage.lambda$null$0(MakeAsyncHttpRequestStage.java:105)
	at java.util.concurrent.CompletableFuture.uniWhenComplete(CompletableFuture.java:760)
	at java.util.concurrent.CompletableFuture$UniWhenComplete.tryFire(CompletableFuture.java:736)
	at java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:474)
	at java.util.concurrent.CompletableFuture.complete(CompletableFuture.java:1962)
	at software.amazon.awssdk.core.internal.http.pipeline.stages.MakeAsyncHttpRequestStage.lambda$executeHttpRequest$3(MakeAsyncHttpRequestStage.java:163)
	at java.util.concurrent.CompletableFuture.uniWhenComplete(CompletableFuture.java:760)
	at java.util.concurrent.CompletableFuture$UniWhenComplete.tryFire(CompletableFuture.java:736)
	at java.util.concurrent.CompletableFuture$Completion.run(CompletableFuture.java:442)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
Caused by: software.amazon.awssdk.services.s3.model.S3Exception: null (Service: S3, Status Code: 403, Request ID: 7XYQ9TD0GP2XB1VS, Extended Request ID: 1CGjM3myJuXuW3IO558rlb8fwvj+cChFXvi54W6r0OlkVlYj1oKXK1yUrxN7hyL4Ua4Lt9zkplHvct76x0SytQ==)
	at software.amazon.awssdk.services.s3.model.S3Exception$BuilderImpl.build(S3Exception.java:104)
	at software.amazon.awssdk.services.s3.model.S3Exception$BuilderImpl.build(S3Exception.java:58)
	at software.amazon.awssdk.protocols.query.internal.unmarshall.AwsXmlErrorUnmarshaller.unmarshall(AwsXmlErrorUnmarshaller.java:99)
	at software.amazon.awssdk.protocols.query.unmarshall.AwsXmlErrorProtocolUnmarshaller.handle(AwsXmlErrorProtocolUnmarshaller.java:102)
	at software.amazon.awssdk.protocols.query.unmarshall.AwsXmlErrorProtocolUnmarshaller.handle(AwsXmlErrorProtocolUnmarshaller.java:82)
	at software.amazon.awssdk.core.http.MetricCollectingHttpResponseHandler.lambda$handle$0(MetricCollectingHttpResponseHandler.java:52)
	at software.amazon.awssdk.core.internal.util.MetricUtils.measureDurationUnsafe(MetricUtils.java:63)
	at software.amazon.awssdk.core.http.MetricCollectingHttpResponseHandler.handle(MetricCollectingHttpResponseHandler.java:52)
	at software.amazon.awssdk.core.internal.http.async.AsyncResponseHandler.lambda$prepare$0(AsyncResponseHandler.java:89)
	at java.util.concurrent.CompletableFuture.uniCompose(CompletableFuture.java:952)
	at java.util.concurrent.CompletableFuture$UniCompose.tryFire(CompletableFuture.java:926)
	at java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:474)
	at java.util.concurrent.CompletableFuture.complete(CompletableFuture.java:1962)
	at software.amazon.awssdk.core.internal.http.async.AsyncResponseHandler$BaosSubscriber.onComplete(AsyncResponseHandler.java:132)
	at software.amazon.awssdk.utils.async.SimplePublisher.doProcessQueue(SimplePublisher.java:275)
	at software.amazon.awssdk.utils.async.SimplePublisher.processEventQueue(SimplePublisher.java:224)
	at software.amazon.awssdk.utils.async.SimplePublisher.complete(SimplePublisher.java:157)
	at java.util.concurrent.CompletableFuture.uniRun(CompletableFuture.java:705)
	at java.util.concurrent.CompletableFuture.uniRunStage(CompletableFuture.java:717)
	at java.util.concurrent.CompletableFuture.thenRun(CompletableFuture.java:2010)
	at software.amazon.awssdk.services.s3.internal.crt.S3CrtResponseHandlerAdapter.onErrorResponseComplete(S3CrtResponseHandlerAdapter.java:135)
	at software.amazon.awssdk.services.s3.internal.crt.S3CrtResponseHandlerAdapter.handleError(S3CrtResponseHandlerAdapter.java:124)
	at software.amazon.awssdk.services.s3.internal.crt.S3CrtResponseHandlerAdapter.onFinished(S3CrtResponseHandlerAdapter.java:93)
	at software.amazon.awssdk.crt.s3.S3MetaRequestResponseHandlerNativeAdapter.onFinished(S3MetaRequestResponseHandlerNativeAdapter.java:24)
```
## Resolving the issue
* Change the CRT Async client to the Netty Async client

OR

* Instantiate the CRT Async clients before generating any requests to the AWS SDK