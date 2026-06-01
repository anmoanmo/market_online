package com.buka.db;

import com.buka.UserServiceApplication;
import com.buka.config.MinioUtils;
import com.buka.service.MailService;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.regex.Pattern;

/**
 * @className: MailTest
 * @author: LZX
 * @date: 2025/2/10 15:15
 * @Version: 1.0
 * @description:
 */
@SpringBootTest(classes = UserServiceApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class MailTest {
    @Autowired
    private MailService mailService;
    @Test
    public void sendMail() {
        mailService.sendMail("demo@example.com", "404-Shop 验证码测试", "这是一封本地演示测试邮件。");
    }
    public static boolean isValidEmail(String email) {
        if ((email != null) && (!email.isEmpty())) {
            return Pattern.matches("^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$", email);
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(isValidEmail("demo@example.com"));
    }

    @Test
    public void qiu() {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region2());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
//...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传
        String accessKey = requireEnv("QINIU_ACCESS_KEY");
        String secretKey = requireEnv("QINIU_SECRET_KEY");
        String bucket = requireEnv("QINIU_BUCKET");
        String localFilePath = System.getenv().getOrDefault(
                "QINIU_TEST_FILE",
                System.getProperty("java.io.tmpdir") + java.io.File.separator + "demo-image.png"
        );
//默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try {
            Response response = uploadManager.put(localFilePath, key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            ex.printStackTrace();
            if (ex.response != null) {
                System.err.println(ex.response);

                try {
                    String body = ex.response.toString();
                    System.err.println(body);
                } catch (Exception ignored) {
                }
            }
        }

    }
    @Test
    public void delete() {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region2());
//...其他参数参考类注释
        String accessKey = requireEnv("QINIU_ACCESS_KEY");
        String secretKey = requireEnv("QINIU_SECRET_KEY");
        String bucket = requireEnv("QINIU_BUCKET");
        String key = "2025/02/13/c956cd24c3b0439fa5c7bfc47a6ef158.png";
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            bucketManager.delete(bucket, key);
        } catch (QiniuException ex) {
            //如果遇到异常，说明删除失败
            System.err.println(ex.code());
            System.err.println(ex.response.toString());
        }

    }
    @Autowired
    private MinioUtils minioUtils;

    @Test
    public void MiniIO() {
        minioUtils.createBucket("aaaaa");
    }

    private static String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing environment variable: " + key);
        }
        return value;
    }
}


