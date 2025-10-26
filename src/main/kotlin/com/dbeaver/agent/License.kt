/*
 * DBeaver License 生成工具
 * 命令行工具类，用于生成 DBeaver 或 CloudBeaver 的 license 文件。
 * 与 Java Agent 拦截流程无直接关联。
 */
package com.dbeaver.agent

import com.dbeaver.agent.utils.MyCryptKey
import com.dbeaver.lm.api.*
import java.security.PrivateKey
import java.util.*
import kotlin.system.exitProcess

class License {
    // Backwards-compatible fields used by the Java UI via reflection
    var productName: String? = "dbeaver"
    var licenseType: String? = "ue"
    var productVersion: Int = 25

    // Keep a public call() method so Java code that expects Callable-like API still works
    @Throws(Exception::class)
    fun call(): Int {
        genLicense(productName ?: "dbeaver", licenseType ?: "ue", productVersion)
        return 0
    }

    // generate a license using provided options
    @Throws(Exception::class)
    fun genLicense(productName: String, licenseType: String, productVersionOpt: Int) {
        val privateKey = MyCryptKey.privateKey as PrivateKey?
        // 需要注意的是,这里 id 是不一样的 终极版叫 dbeaver-ue
        val productId = "${productName}-${licenseType}"
        val product = LMProduct(
            productId,
            "DB",
            "DBeaver Enterprise",
            "DBeaver Enterprise Edition",
            productVersionOpt.toString(),
            LMProductType.DESKTOP,
            Date(),
            arrayOfNulls<String>(0)
        )
        val licenseID = LMUtils.generateLicenseId(product)
        val productID = product.getId()
        val productVersion = product.getVersion()
        val licenseData: ByteArray = getLicenseData(licenseID, productID, productVersion)
        val licenseEncrypted = LMEncryption.encrypt(licenseData, privateKey)
        println("---${productId}(v${productVersion}) LICENSE ---")
        println(Base64.getEncoder().encodeToString(licenseEncrypted))
        println("--- 请复制上一行 ---")
    }
    companion object {
        private fun getLicenseData(licenseID: String?, productID: String?, productVersion: String?): ByteArray {
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
            license.usersNumber = 999.toShort()
            license.yearsNumber = 127.toByte()
            return license.data
        }

        @JvmStatic
        fun main(args: Array<String>) {
            // Simple dependency-free argument parsing
            var product = "dbeaver"
            var licenseType = "ue"
            var productVersion = 25

            val it = args.iterator()
            fun usage() {
                println("Usage: gen-license [-p|--product <dbeaver|cloudbeaver>] [-t|--type <le|ee|ue>] [-v|--version <number>] [-h|--help]")
            }

            var i = 0
            while (i < args.size) {
                when (args[i]) {
                    "-h", "--help" -> {
                        usage()
                        exitProcess(0)
                    }
                    "-p", "--product" -> {
                        if (i + 1 < args.size) {
                            product = args[i + 1]
                            i += 1
                        } else {
                            System.err.println("Missing value for ${args[i]}")
                            usage()
                            exitProcess(1)
                        }
                    }
                    "-t", "--type" -> {
                        if (i + 1 < args.size) {
                            licenseType = args[i + 1]
                            i += 1
                        } else {
                            System.err.println("Missing value for ${args[i]}")
                            usage()
                            exitProcess(1)
                        }
                    }
                    "-v", "--version" -> {
                        if (i + 1 < args.size) {
                            try {
                                productVersion = args[i + 1].toInt()
                            } catch (e: NumberFormatException) {
                                System.err.println("Invalid number for version: ${args[i + 1]}")
                                usage()
                                exitProcess(1)
                            }
                            i += 1
                        } else {
                            System.err.println("Missing value for ${args[i]}")
                            usage()
                            exitProcess(1)
                        }
                    }
                    else -> {
                        System.err.println("Unknown argument: ${args[i]}")
                        usage()
                        exitProcess(1)
                    }
                }
                i += 1
            }

            try {
                val license = License()
                license.genLicense(product, licenseType, productVersion)
            } catch (e: Exception) {
                e.printStackTrace()
                exitProcess(2)
            }
        }
    }
}
