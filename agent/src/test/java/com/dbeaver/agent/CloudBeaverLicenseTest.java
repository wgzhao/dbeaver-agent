package com.dbeaver.agent;

import com.dbeaver.lm.api.LMEncryption;
import com.dbeaver.lm.api.LMLicense;
import com.dbeaver.lm.api.LMProduct;
import com.dbeaver.lm.api.LMProductType;
import com.dbeaver.lm.api.LMLicenseType;
import com.dbeaver.lm.api.LMUtils;
import com.dbeaver.agent.utils.MyCryptKey;
import com.dbeaver.agent.utils.OriginalCryptKey;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;

public class CloudBeaverLicenseTest {
    @Test
    public void genCloudBeaverLicense() throws Exception {
        PrivateKey privateKey = (PrivateKey) new MyCryptKey().getPrivateKey();
        // 需要注意的是,这里 id 是不一样的 终极版叫 dbeaver-ue
        LMProduct product = new LMProduct("cloudbeaver-ee",
                                          "DB",
                                          "CloudBeaver Enterprise",
                                          "CloudBeaver Enterprise Edition",
                                          "21",
                                          LMProductType.SERVER,
                                          new Date(),
                                          new String[0]);
        String licenseID = LMUtils.generateLicenseId(product);
        String productID = product.getId();
        String productVersion = product.getVersion();
        String ownerID = "114514";
        String ownerCompany = "无";
        String ownerName = "Null or None";
        String ownerEmail = "example@example.com";
        LMLicense license = new LMLicense(licenseID,
                                          LMLicenseType.ULTIMATE,
                                          new Date(),
                                          new Date(),
                                          // 这样子就是没有到期
                                          null,
                                          0L,
                                          productID,
                                          productVersion,
                                          ownerID,
                                          ownerCompany,
                                          ownerName,
                                          ownerEmail);
        // 反射修改 yearsNumber 用来修改支持年份
        Field yearsNumberField = license.getClass().getDeclaredField("yearsNumber");
        yearsNumberField.setAccessible(true);
        yearsNumberField.set(license, (byte) 127);
        byte[] licenseData = license.getData();
        byte[] licenseEncrypted = LMEncryption.encrypt(licenseData, privateKey);
        System.out.println("--- LICENSE ---");
        System.out.println(Base64.getEncoder().encodeToString(licenseEncrypted));
        System.out.println("--- 请复制上文 (不包括这一行) ---");
    }

    @Test
    public void decryptOriginalLicense() throws Exception {
        String encryptedLicense =
                "cp3qyxPnbVP7WmNZ/dIdhOfi73hYOVo2xtVPF+O03YQIhpmnmPAqloODNSjcQmNRY3x1n09Xckl6\n" +
                        "zSmUYiy9JtE/5SyC+yNHzTZdi7HrzUlzbKq8+DqpkVaXalFfwlrdiTYuV9yp8IyVrOJPYLFWBkKs\n" +
                        "2GfKcW5h8g6RAyF3z5dB4FIOr2pJkrsdOqLitCjnV4UjTIogL5sfWPbqHNaC8MJGsqpdqsYNOoZI\n" +
                        "tLcgTZCDBixQPa0MyVGOqCozC1yzOlFI9pmiNu2fsWyPVlP2fZO1afp43p7uLTZw0PjbTbYBs/c/\n" +
                        "CXssrO6KBxTpR1gIQiyZfwm6paxtXbX1JDQuAA==";
        PublicKey oldPublicKey = (PublicKey) new OriginalCryptKey().getCloudBeaverEePublicKey();
        InputStream is = new ByteArrayInputStream(encryptedLicense.getBytes());
        byte[] encryptedLicenseBytes = LMUtils.readEncryptedString(is);
        LMLicense license = new LMLicense(encryptedLicenseBytes, oldPublicKey);
        System.out.println(license);
    }
}
