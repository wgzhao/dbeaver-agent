/*
 * 官方公钥工具类
 * 保存 DBeaver/CloudBeaver 官方公钥字符串，供 license 校验等场景使用。
 */
package com.dbeaver.agent.utils

import java.security.KeyFactory
import java.security.PublicKey
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.*

class OriginalCryptKey {
    val localDBeaverUeKeyBytes: ByteArray = Base64.getDecoder().decode(DBEAVER_UE_PUBLIC_KEY)
    var localDBeaverEeKeyBytes: ByteArray = Base64.getDecoder().decode(DBEAVER_EE_PUBLIC_KEY)
    var localCloudBeaverEeKeyBytes: ByteArray = Base64.getDecoder().decode(CLOUDBEAVER_EE_PUBLIC_KEY)

    val dBeaverUePublicKey: PublicKey?
        get() {
            try {
                val keyFactory = KeyFactory.getInstance("RSA")
                val publicKeySpec = X509EncodedKeySpec(localDBeaverUeKeyBytes)
                return keyFactory.generatePublic(publicKeySpec)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            }
            return null
        }

    val dBeaverEePublicKey: PublicKey?
        get() {
            try {
                val keyFactory = KeyFactory.getInstance("RSA")
                val publicKeySpec = X509EncodedKeySpec(localDBeaverEeKeyBytes)
                return keyFactory.generatePublic(publicKeySpec)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            }
            return null
        }

    val cloudBeaverEePublicKey: PublicKey?
        get() {
            try {
                val keyFactory = KeyFactory.getInstance("RSA")
                val publicKeySpec = X509EncodedKeySpec(localCloudBeaverEeKeyBytes)
                return keyFactory.generatePublic(publicKeySpec)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            }
            return null
        }

    companion object {
        var DBEAVER_UE_PUBLIC_KEY: String = """
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk7ciFU/aUCIgH5flBbGD0t7B3KOmfL0l
            BMf2ENuLA0w/T8A1RvteUYk2EQo3UrZ7kMZ8rK93nmDjituN7jlv/bsxGyAox87BbKYSs9oH5f9P
            hYHAiTE0PxoMODnl4NgR+Bpc+Ath8wDLHMC+BzYkOy4JQo8EX/ff58TT9UYP8eoDeGdSxQmW3FJC
            i82UiC5zIk75dx20Al9ql0fdxnzo31q/2MbnNCAfSchsqrKtzBtheex4JvvqZjxn98wk5Te1QgZz
            Caz4ay9dkLVjSt79QYm5hKb8Jt3O5SxSUsrjmYVeG+k2bQlidw8dENwLZmvJkIJi8kb94yEwY/dq
            lENDkQIDAQAB
            
            """.trimIndent().replace("[^A-Za-z0-9+/=]".toRegex(), "")

        var DBEAVER_EE_PUBLIC_KEY: String = """
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk7ciFU/aUCIgH5flBbGD0t7B3KOmfL0l
            BMf2ENuLA0w/T8A1RvteUYk2EQo3UrZ7kMZ8rK93nmDjituN7jlv/bsxGyAox87BbKYSs9oH5f9P
            hYHAiTE0PxoMODnl4NgR+Bpc+Ath8wDLHMC+BzYkOy4JQo8EX/ff58TT9UYP8eoDeGdSxQmW3FJC
            i82UiC5zIk75dx20Al9ql0fdxnzo31q/2MbnNCAfSchsqrKtzBtheex4JvvqZjxn98wk5Te1QgZz
            Caz4ay9dkLVjSt79QYm5hKb8Jt3O5SxSUsrjmYVeG+k2bQlidw8dENwLZmvJkIJi8kb94yEwY/dq
            lENDkQIDAQAB
            
            """.trimIndent().replace("[^A-Za-z0-9+/=]".toRegex(), "")

        var CLOUDBEAVER_EE_PUBLIC_KEY: String = """
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlAiyH9ghPpqATx/FtCV2o5y6UKDFR+4c
            LjvztuYyvfb161hrrtkcmLkBJUNfJQbG6y+yE6tJCoFPplobU7ztqIvWQchQbSyDCDyWeeob8SY5
            Kj6w3WhuEz1FNDJkylYhbhSXPq80kxCD5EsPe/i8C3JCENFAPpxM2mfjQPrGbQTL3B5scBODh7oX
            QgtvIIwYxno+o+wZ6dLayBG3HrSzDe7Qb7QtpZUsHSjEHekfW15rmp063jpL2wJTIZJXThLsTrT7
            LzXY7cJHg2nh9JjpmOe7udAnaf6zpIeVZSo+zA4cLzuCuZhyDnCfUMpGpcgwGieNIa4bC2YXPu4T
            F7jNpwIDAQAB
            
            """.trimIndent().replace("[^A-Za-z0-9+/=]".toRegex(), "")
    }
}
