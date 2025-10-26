package com.dbeaver.agent

import com.dbeaver.agent.utils.MyCryptKey
import com.dbeaver.agent.utils.MyCryptKey.privateKey
import com.dbeaver.agent.utils.OriginalCryptKey
import com.dbeaver.lm.api.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.PrivateKey
import java.util.*

class DBeaverLicenseTest {
    @Test
    @Throws(Exception::class)
    fun genEnterpriseLicense() {
        val myCryptKey = MyCryptKey
        val privateKey = myCryptKey.privateKey as PrivateKey?
        // 需要注意的是,这里 id 是不一样的 终极版叫 dbeaver-ue
        val product = LMProduct(
            "dbeaver-ee",
            "DB",
            "DBeaver Enterprise 24.0",
            "DBeaver Enterprise Edition",
            "24",
            LMProductType.DESKTOP,
            Date(),
            arrayOfNulls<String>(0)
        )
        val licenseID = LMUtils.generateLicenseId(product)
        val productID = product.getId()
        val productVersion = product.getVersion()
        val ownerID = "080601"
        val ownerCompany = "example.com"
        val ownerName = "owner"
        val ownerEmail = "owner@example.com"
        val license = LMLicense(
            licenseID,
            LMLicenseType.ULTIMATE,
            Date(),
            Date(),  // 这样子就是没有到期
            null,
            0L,
            productID,
            productVersion,
            ownerID,
            ownerCompany,
            ownerName,
            ownerEmail
        )
        // 反射修改 yearsNumber 用来修改支持年份
        val yearsNumberField = license.javaClass.getDeclaredField("yearsNumber")
        yearsNumberField.setAccessible(true)
        yearsNumberField.set(license, 127.toByte())
        val licenseData = license.getData()
        val licenseEncrypted = LMEncryption.encrypt(licenseData, privateKey)
        println("--- LICENSE ---")
        println(Base64.getEncoder().encodeToString(licenseEncrypted))
        println("--- 请复制上文 (不包括这一行) ---")
        LMEncryption.decrypt(licenseEncrypted, myCryptKey.publicKey)
    }

    @Test
    @Throws(Exception::class)
    fun genLiteLicense() {
        val privateKey = privateKey as PrivateKey?
        // 需要注意的是,这里 id 是不一样的 终极版叫 dbeaver-ue
        val product = LMProduct(
            "dbeaver-le",
            "DB",
            "DBeaver Lite",
            "DBeaver Lite Edition",
            "24",
            LMProductType.DESKTOP,
            Date(),
            arrayOfNulls<String>(0)
        )
        val licenseID = LMUtils.generateLicenseId(product)
        val productID = product.getId()
        val productVersion = product.getVersion()
        val ownerID = "114514"
        val ownerCompany = "owner"
        val ownerName = "owner"
        val ownerEmail = "owner@example.com"
        val license = LMLicense(
            licenseID,
            LMLicenseType.ULTIMATE,
            Date(),
            Date(),  // 这样子就是没有到期
            null,
            0L,
            productID,
            productVersion,
            ownerID,
            ownerCompany,
            ownerName,
            ownerEmail
        )
        // 反射修改 yearsNumber 用来修改支持年份
        val yearsNumberField = license.javaClass.getDeclaredField("yearsNumber")
        yearsNumberField.setAccessible(true)
        yearsNumberField.set(license, 127.toByte())
        val licenseData = license.getData()
        val licenseEncrypted = LMEncryption.encrypt(licenseData, privateKey)
        println("--- LICENSE ---")
        println(Base64.getEncoder().encodeToString(licenseEncrypted))
        println("--- 请复制上文 (不包括这一行) ---")
    }

    @Test
    @Throws(Exception::class)
    fun genUltimateLicense() {
        val privateKey = privateKey as PrivateKey?
        // 需要注意的是,这里 id 是不一样的 终极版叫 dbeaver-ue
        val product = LMProduct(
            "dbeaver-ue",
            "DB",
            "DBeaver Enterprise",
            "DBeaver Ultimate Edition",
            "24",
            LMProductType.DESKTOP,
            Date(),
            arrayOfNulls<String>(0)
        )
        val licenseID = LMUtils.generateLicenseId(product)
        val productID = product.getId()
        val productVersion = product.getVersion()
        val ownerID = "114514"
        val ownerCompany = "owner"
        val ownerName = "owner"
        val ownerEmail = "owner@example.com"
        val license = LMLicense(
            licenseID,
            LMLicenseType.ULTIMATE,
            Date(),
            Date(),  // 这样子就是没有到期
            null,
            0L,
            productID,
            productVersion,
            ownerID,
            ownerCompany,
            ownerName,
            ownerEmail
        )
        // 反射修改 yearsNumber 用来修改支持年份
        val yearsNumberField = license.javaClass.getDeclaredField("yearsNumber")
        yearsNumberField.setAccessible(true)
        yearsNumberField.set(license, 127.toByte())
        val licenseData = license.getData()
        val licenseEncrypted = LMEncryption.encrypt(licenseData, privateKey)
        println("--- LICENSE ---")
        println(Base64.getEncoder().encodeToString(licenseEncrypted))
        println("--- 请复制上文 (不包括这一行) ---")
    }

    @Test
    @Throws(Exception::class)
    fun decryptOriginalLicense() {
        val encryptedLicense = "GcEVPtVH+fzyCX3Jw/b2iDGHIYe20IwwGGzvCaSvgN+SOLyeOfmhTgIXkhhuJsCi7Ov/7Sy2Hpk3\n" +
                "VdHjehLS727GlKOKKKkZ6s9C8bt+Aw4WEhDsivOJpQt5eLUjvDLhZC0ols4R9kIXHRo1KcS5AaHy\n" +
                "8EWhdiuxPOJdHTR01waJUvb4RdH8Ldi2m2CNB93sv1OTMvzoDX1oWUnWGN8mL7K0UU+3ksy06a0O\n" +
                "/AU8wueD1yaXHQp9OML5WmBDZapiuSKoQUH/dPhu6C7XRj1EAiTueNibb9rSfbhlUYKgA/1is4nW\n" +
                "42xwiN3+jzQrBYO1NQIYAlGHxlsJ0+IxqVLHCw=="
        val oldPublicKey = OriginalCryptKey().dBeaverEePublicKey
        val `is`: InputStream = ByteArrayInputStream(encryptedLicense.toByteArray())
        val encryptedLicenseBytes = LMUtils.readEncryptedString(`is`)
        val license = LMLicense(encryptedLicenseBytes, oldPublicKey)
        println(license)
    }
}
