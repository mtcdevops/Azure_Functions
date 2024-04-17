package com.function.kms;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import java.io.ByteArrayInputStream;


/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        final String query = request.getQueryParameters().get("name");
        final String name = request.getBody().orElse(query);

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
    }

    @FunctionName("GetCurrentTime")
    public HttpResponseMessage run2(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, 
                        authLevel = AuthorizationLevel.ANONYMOUS) 
                        HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // 현재 시간을 가져오기 위해 Calendar 객체 사용
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(cal.getTime());

        return request.createResponseBuilder(HttpStatus.OK).body("Current time is: " + currentTime).build();
    }

    @FunctionName("FileReceiver")
    public void run3(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().severe("Java HTTP trigger 업로드 실행.");
        // 요청에서 이미지 바이트 배열을 읽기
        
        String filename = "";
        context.getLogger().severe("요청 본문에서 파일명 추출");
       
        String input = request.getBody().orElse("");
        int startIndex = input.indexOf("filename=\"");
        // context.getLogger().severe("data : "+request.getBody());
        if (startIndex != -1) {
            // 시작 위치 이후의 문자열에서 다음 " 문자의 위치 찾기
            int endIndex = input.indexOf("\"", startIndex + 10); // "filename=\"" 이후부터 시작
            
            // 시작 위치와 끝 위치 사이의 문자열을 추출하여 파일명으로 사용
            if (endIndex != -1) {
                filename = input.substring(startIndex + 10, endIndex); // "filename=\"" 길이만큼 이동
                context.getLogger().severe("추출된 파일명: " + filename);
            } else {
                context.getLogger().severe("No closing quote found.");
            }
        } else {
            context.getLogger().severe("No filename found.");
        }

        context.getLogger().severe("Data 정제");
        String data = request.getBody().get();
        String[] lines = data.split("\n");
        StringBuilder newData = new StringBuilder();
        for (int i = 4; i < lines.length - 1; i++) {
            newData.append(lines[i]);
            if (i < lines.length - 2) {
                newData.append("\n");
            }
        }

        data = newData.toString();
        context.getLogger().severe(newData.toString());
        // 요청이 이미지를 포함하는지 확인
        if (request.getBody().isPresent() && !filename.equals("")) {
            context.getLogger().severe("요청 본문에 이미지 데이터가 있습니다.");
            try {

                /* 이미지 byte배열 전환 */
                byte[] imageDataBytes = data.getBytes();
                // 바이트 배열을 사용하여 이미지 스트림 생성
                ByteArrayInputStream imageStream = new ByteArrayInputStream(imageDataBytes);
                // InputStream imageStream2 = new ByteArrayInputStream(imageDataBytes);

                /* File upload 작업 */
                // 1. 저장소 계정을 위한 연결 문자열 구문 분석
                String storageConnectionString = System.getenv("AzureWebJobsStorage");
                context.getLogger().severe("1. 저장소 계정을 위한 연결 문자열 구문 분석 : "+storageConnectionString);
                
                // 2. 연결 문자열에서 저장소 계정 가져오기
                CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
                context.getLogger().severe("2. 연결 문자열에서 저장소 계정 가져오기 : "+storageAccount);

                // 3. Blob 클라이언트 생성
                CloudBlobContainer container = storageAccount.createCloudBlobClient().getContainerReference("output");

                // 4. Blob을 위한 고유한 이름 생성
                String blobName = filename;

                // 5. Blob 참조 가져오기
                CloudBlockBlob blob = container.getBlockBlobReference(blobName);
                
                // 6. 이미지 데이터 Blob으로 업로드
                blob.upload(imageStream, imageStream.available());

                // 이미지가 Azure Storage에 성공적으로 업로드되었음을 기록
                context.getLogger().info("이미지가 Azure Storage에 성공적으로 업로드되었습니다.");

                
            } catch (Exception e) {
                // TODO: handle exception
                context.getLogger().severe("오류 발생: " + e.getMessage());
            }
        } else {
            context.getLogger().severe("요청 본문에 이미지 데이터가 없습니다.");
        }
    }

}
