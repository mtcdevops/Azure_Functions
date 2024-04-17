package com.function.kms;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import com.azure.storage.blob.*;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;


/**
 * Azure Functions with Azure Blob trigger.
 */
public class BlobTriggerJava {
    /**
     * This function will be invoked when a new or updated blob is detected at the specified path. The blob contents are provided as input to this function.
     */
    @FunctionName("BlobTriggerJava-OUTPUT")
    @StorageAccount("AzureWebJobsStorage")
    public void run(
        @BlobTrigger(name = "content", path = "output/{name}", dataType = "binary") byte[] content,
        @BindingName("name") String name,
        final ExecutionContext context
    ) {
        
        ByteArrayInputStream output = new ByteArrayInputStream(content);
        try {
            BufferedImage originalImage = ImageIO.read(output);
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            if (name.startsWith("resizedImage")) {// 이미지 중복 체크
                context.getLogger().info("이미지가 output 컨테이너 Blob 저장소에 저장되었습니다. 파일명 :"+name); 
                context.getLogger().info("Image Size: " + originalWidth + "x" + originalHeight);
            } else {
                context.getLogger().info("Java Blob trigger function processed a blob. Name: " + name + "\n  Size: " + content.length + " Bytes");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* 저장대상 */
    private final String blobContainer = "output";
    
    @FunctionName("BlobTriggerJava-INOUT")
    @StorageAccount("AzureWebJobsStorage")
    public void run2(
        @BlobTrigger(name = "content", path = "input/{name}", dataType = "binary") byte[] content,
        @BindingName("name") String name,
        final ExecutionContext context
    ) {
        // Load image from input stream
        try {
            
            // 1. Load the input image
            ByteArrayInputStream input = new ByteArrayInputStream(content);
            BufferedImage originalImage = ImageIO.read(input);
            
            int 비율설정 = 2;

            // Get the dimensions of the original image
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int resizedWidth = originalImage.getWidth()/비율설정;
            int resizedHeight = originalImage.getHeight()/비율설정;
            
            // 2. Resize the image
            BufferedImage resizedImage = new BufferedImage(resizedWidth, resizedHeight, originalImage.getType());
            resizedImage.createGraphics().drawImage(originalImage, 0, 0, resizedWidth, resizedHeight, null);
            
            // 3. 처리된 이미지를 Blob 저장소에 업로드
            // 처리된 이미지를 ByteArrayOutputStream에 저장합니다.
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", output);
            
            // Azure Blob Storage 클라이언트 초기화
            // BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(storageConnectionString).buildClient();
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(System.getenv("AzureWebJobsStorage")).buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(blobContainer);
            containerClient.getBlobClient("resizedImage_" + name).upload(new ByteArrayInputStream(output.toByteArray()), output.size());
            context.getLogger().info("이미지가 input 컨테이너 Blob 저장소에 저장되었습니다. 파일명 :"+name);
            context.getLogger().info("Original Image Size: " + originalWidth + "x" + originalHeight);
            context.getLogger().info("Resized Image Size: " + resizedWidth + "x" + resizedHeight);
            
        } catch (Exception e) {
            // TODO: handle exception
            context.getLogger().warning("Error processing image: " + e.getMessage());
            e.printStackTrace(); // 예외 스택 트레이스 출력
        }
    }

    
}
