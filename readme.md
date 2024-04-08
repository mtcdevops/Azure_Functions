# pom.xml 추가
```xml
<!-- 이미지 업로드 종속성 추가-->
        <dependency>
            <groupId>com.microsoft.azure.functions</groupId>
            <artifactId>azure-functions-java-library</artifactId>
            <version>3.1.0</version>
        </dependency>

        <dependency>
            <groupId>com.microsoft.azure</groupId>
            <artifactId>azure-storage</artifactId>
            <version>8.6.5</version>
        </dependency>

        <!-- 썸네일 사용을 위한 의존성 추가 -->
        <dependency>
            <groupId>net.coobird</groupId>
            <artifactId>thumbnailator</artifactId>
            <version>0.4.8</version>
        </dependency>

        <!--BlobInputStream, BlobOutputStream 사용을 위한 의존성 추가-->
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-storage-blob</artifactId>
            <version>12.25.3</version>
        </dependency>

        <!-- SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder". -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
            <version>2.13.5</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.13.5</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-xml</artifactId>
            <version>2.13.5</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.13.5</version>
        </dependency>
```

# local.settings.json 파일 생성
```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "<스토리지 계정 연결 문자열>",
    "FUNCTIONS_WORKER_RUNTIME": "java",
  }
}
```

# mvn 명령어
- build
```cmd
mvn clean package
mvn package
```

- function start
```cmd
mvn azure-functions:run
```
