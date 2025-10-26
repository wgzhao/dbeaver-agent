package com.dbeaver.agent

import com.dbeaver.agent.utils.MyCryptKey.privateKey
import com.dbeaver.agent.utils.OriginalCryptKey
import com.dbeaver.lm.api.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.PrivateKey
import java.util.*

class CloudBeaverLicenseTest {
    @Test
    @Throws(Exception::class)
    fun genCloudBeaverLicense() {
        val privateKey = privateKey as PrivateKey?
        // 需要注意的是,这里 id 是不一样的 终极版叫 dbeaver-ue
        val product = LMProduct(
            "cloudbeaver-ee",
            "DB",
            "CloudBeaver Enterprise",
            "CloudBeaver Enterprise Edition",
            "24",
            LMProductType.SERVER,
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
        val encryptedLicense =
            "cp3qyxPnbVP7WmNZ/dIdhOfi73hYOVo2xtVPF+O03YQIhpmnmPAqloODNSjcQmNRY3x1n09Xckl6\n" +
                    "zSmUYiy9JtE/5SyC+yNHzTZdi7HrzUlzbKq8+DqpkVaXalFfwlrdiTYuV9yp8IyVrOJPYLFWBkKs\n" +
                    "2GfKcW5h8g6RAyF3z5dB4FIOr2pJkrsdOqLitCjnV4UjTIogL5sfWPbqHNaC8MJGsqpdqsYNOoZI\n" +
                    "tLcgTZCDBixQPa0MyVGOqCozC1yzOlFI9pmiNu2fsWyPVlP2fZO1afp43p7uLTZw0PjbTbYBs/c/\n" +
                    "CXssrO6KBxTpR1gIQiyZfwm6paxtXbX1JDQuAA=="
        val oldPublicKey = OriginalCryptKey().cloudBeaverEePublicKey
        val `is`: InputStream = ByteArrayInputStream(encryptedLicense.toByteArray())
        val encryptedLicenseBytes = LMUtils.readEncryptedString(`is`)
        val license = LMLicense(encryptedLicenseBytes, oldPublicKey)
        println(license)
    }
}
