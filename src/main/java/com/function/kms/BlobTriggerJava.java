package com.function.kms;

import com.microsoft.azure.functions.annotation.*;

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

    /* 저장대상 */
    private final String blobContainer = "output";
    
    @FunctionName("BlobTriggerJava")
    @StorageAccount("AzureWebJobsStorage")
    public void run(
        @BlobTrigger(name = "content", path = "input/{name}", dataType = "binary") byte[] content,
        @BindingName("name") String name,
        final ExecutionContext context
    ) {
        // Load image from input stream
        context.getLogger().info("업로드된 이미지 정보: " + name + "\n  Size: " + content.length + " Bytes");
        try {
            
            // 1. Load the input image
            ByteArrayInputStream input = new ByteArrayInputStream(content);
            BufferedImage originalImage = ImageIO.read(input);
            
            int 비율설정 = 2;

            // Get the dimensions of the original image
            int originalWidth = originalImage.getWidth()/비율설정;
            int originalHeight = originalImage.getHeight()/비율설정;
            
            // 2. Resize the image
            BufferedImage resizedImage = new BufferedImage(originalWidth, originalHeight, originalImage.getType());
            resizedImage.createGraphics().drawImage(originalImage, 0, 0, originalWidth, originalHeight, null);
            
            // 배경색 black으로 설정
            // for (int y = 0; y < resizedImage.getHeight(); y++) {
                //     for (int x = 0; x < resizedImage.getWidth(); x++) {
            //         resizedImage.setRGB(x, y, 0xFF000000);
            //     }
            // }

            
            // 3. 처리된 이미지를 Blob 저장소에 업로드
            if (name.startsWith("resizedImage")) {// 이미지 중복 체크
                context.getLogger().info("저장된 이미지 파일명 : "+name);        
                context.getLogger().info("Image Size: " + originalWidth + "x" + originalHeight);
            } else {
                // 처리된 이미지를 ByteArrayOutputStream에 저장합니다.
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "png", output);
                
                // Azure Blob Storage 클라이언트 초기화
                // BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(storageConnectionString).buildClient();
                BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(System.getenv("AzureWebJobsStorage")).buildClient();
                BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(blobContainer);
                containerClient.getBlobClient("resizedImage_" + name).upload(new ByteArrayInputStream(output.toByteArray()), output.size());
                context.getLogger().info("처리된 이미지가 Blob 저장소에 저장되었습니다. 파일명 :"+name);
                context.getLogger().info("Original Image Size: " + originalWidth + "x" + originalHeight);
            }
            
        } catch (Exception e) {
            // TODO: handle exception
            context.getLogger().warning("Error processing image: " + e.getMessage());
            e.printStackTrace(); // 예외 스택 트레이스 출력
        }
    }
}
