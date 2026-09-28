package com.kun.common.oss.template;


import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.kun.common.oss.config.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;


@Slf4j
@RequiredArgsConstructor
public class OssTemplate {

    private final OSS ossClient;
    private final OssProperties properties;


    /**
     * 通用流上传 (适用于封面、头像)
     */
    public String uploadFile(String objectName, InputStream inputStream) {
        try {
            ossClient.putObject(properties.getBucketName(), objectName, inputStream);
            // 返回访问路径 (如果配了 domain 则优先使用 domain，否则走默认拼接)
            return properties.getDomain() != null
                    ? properties.getDomain() + "/" + objectName
                    : "https://" + properties.getBucketName() + "." + properties.getEndpoint() + "/" + objectName;
        } catch (Exception e) {
            log.error("上传文件失败: {}", objectName, e);
            throw new RuntimeException("上传文件失败", e);
        }

    }

    /**
     * 【小说专用】将小说正文上传为文本文件保存
     */
    public String uploadChapterContent(String objectName, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            ossClient.putObject(properties.getBucketName(), objectName, inputStream);
            return objectName;
        } catch (IOException e) {
            log.error("上传小说章节到 OSS 失败: {}", objectName, e);
            throw new RuntimeException("上传小说正文失败", e);
        }
    }

    /**
     * 【小说专用】读取 OSS 中的小说章节正文
     */
    public String readChapterContent(String objectName) {
        try (OSSObject ossObject = ossClient.getObject(properties.getBucketName(), objectName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("从 OSS 获取小说正文失败: {}", objectName, e);
            throw new RuntimeException("获取小说正文失败", e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        ossClient.deleteObject(properties.getBucketName(), objectName);
    }
}
