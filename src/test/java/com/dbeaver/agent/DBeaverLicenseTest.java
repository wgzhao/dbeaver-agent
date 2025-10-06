package com.dbeaver.agent;

import com.dbeaver.agent.utils.MyCryptKey;
import com.dbeaver.agent.utils.OriginalCryptKey;
import com.dbeaver.lm.api.LMEncryption;
import com.dbeaver.lm.api.LMLicense;
import com.dbeaver.lm.api.LMLicenseType;
import com.dbeaver.lm.api.LMProduct;
import com.dbeaver.lm.api.LMProductType;
import com.dbeaver.lm.api.LMUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;

public class DBeaverLicenseTest {
    @Test
    public void genEnterpriseLicense() throws Exception {
        MyCryptKey myCryptKey = new MyCryptKey();
        PrivateKey privateKey = (PrivateKey) myCryptKey.getPrivateKey();
        // 需要注意的是,这里 id 是不一样的 终极版叫 dbeaver-ue
        LMProduct product = new LMProduct("dbeaver-ee",
                "DB",
                "DBeaver Enterprise 24.0",
                "DBeaver Enterprise Edition",
                "24",
                LMProductType.DESKTOP,
                new Date(),
                new String[0]);
        String licenseID = LMUtils.generateLicenseId(product);
        String productID = product.getId();
        String productVersion = product.getVersion();
        String ownerID = "080601";
        String ownerCompany = "example.com";
        String ownerName = "owner";
        String ownerEmail = "owner@example.com";
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
        LMEncryption.decrypt(licenseEncrypted, myCryptKey.getPublicKey());
    }

    @Test
    public void genLiteLicense() throws Exception {
        PrivateKey privateKey = (PrivateKey) new MyCryptKey().getPrivateKey();
        // 需要注意的是,这里 id 是不一样的 终极版叫 dbeaver-ue
        LMProduct product = new LMProduct("dbeaver-le",
                "DB",
                "DBeaver Lite",
                "DBeaver Lite Edition",
                "24",
                LMProductType.DESKTOP,
                new Date(),
                new String[0]);
        String licenseID = LMUtils.generateLicenseId(product);
        String productID = product.getId();
        String productVersion = product.getVersion();
        String ownerID = "114514";
        String ownerCompany = "owner";
        String ownerName = "owner";
        String ownerEmail = "owner@example.com";
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
    public void genUltimateLicense() throws Exception {
        PrivateKey privateKey = (PrivateKey) new MyCryptKey().getPrivateKey();
        // 需要注意的是,这里 id 是不一样的 终极版叫 dbeaver-ue
        LMProduct product = new LMProduct("dbeaver-ue",
                                          "DB",
                                          "DBeaver Enterprise",
                                          "DBeaver Ultimate Edition",
                                          "24",
                                          LMProductType.DESKTOP,
                                          new Date(),
                                          new String[0]);
        String licenseID = LMUtils.generateLicenseId(product);
        String productID = product.getId();
        String productVersion = product.getVersion();
        String ownerID = "114514";
        String ownerCompany = "owner";
        String ownerName = "owner";
        String ownerEmail = "owner@example.com";
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
        String encryptedLicense = "GcEVPtVH+fzyCX3Jw/b2iDGHIYe20IwwGGzvCaSvgN+SOLyeOfmhTgIXkhhuJsCi7Ov/7Sy2Hpk3\n" +
                "VdHjehLS727GlKOKKKkZ6s9C8bt+Aw4WEhDsivOJpQt5eLUjvDLhZC0ols4R9kIXHRo1KcS5AaHy\n" +
                "8EWhdiuxPOJdHTR01waJUvb4RdH8Ldi2m2CNB93sv1OTMvzoDX1oWUnWGN8mL7K0UU+3ksy06a0O\n" +
                "/AU8wueD1yaXHQp9OML5WmBDZapiuSKoQUH/dPhu6C7XRj1EAiTueNibb9rSfbhlUYKgA/1is4nW\n" +
                "42xwiN3+jzQrBYO1NQIYAlGHxlsJ0+IxqVLHCw==";
        PublicKey oldPublicKey = (PublicKey) new OriginalCryptKey().getDBeaverEePublicKey();
        InputStream is = new ByteArrayInputStream(encryptedLicense.getBytes());
        byte[] encryptedLicenseBytes = LMUtils.readEncryptedString(is);
        LMLicense license = new LMLicense(encryptedLicenseBytes, oldPublicKey);
        System.out.println(license);
    }
}
