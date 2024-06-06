package dev.misakacloud.dbee.utils;


import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class OriginalCryptKey {
    public static String DBEAVER_UE_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk7ciFU" +
            "/aUCIgH5flBbGD0t7B3KOmfL0l\n" +
            "BMf2ENuLA0w/T8A1RvteUYk2EQo3UrZ7kMZ8rK93nmDjituN7jlv/bsxGyAox87BbKYSs9oH5f9P\n" +
            "hYHAiTE0PxoMODnl4NgR+Bpc+Ath8wDLHMC+BzYkOy4JQo8EX/ff58TT9UYP8eoDeGdSxQmW3FJC\n" +
            "i82UiC5zIk75dx20Al9ql0fdxnzo31q/2MbnNCAfSchsqrKtzBtheex4JvvqZjxn98wk5Te1QgZz\n" +
            "Caz4ay9dkLVjSt79QYm5hKb8Jt3O5SxSUsrjmYVeG+k2bQlidw8dENwLZmvJkIJi8kb94yEwY/dq\n" +
            "lENDkQIDAQAB";

    public static String DBEAVER_EE_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk7ciFU" +
            "/aUCIgH5flBbGD0t7B3KOmfL0l\n" +
            "BMf2ENuLA0w/T8A1RvteUYk2EQo3UrZ7kMZ8rK93nmDjituN7jlv/bsxGyAox87BbKYSs9oH5f9P\n" +
            "hYHAiTE0PxoMODnl4NgR+Bpc+Ath8wDLHMC+BzYkOy4JQo8EX/ff58TT9UYP8eoDeGdSxQmW3FJC\n" +
            "i82UiC5zIk75dx20Al9ql0fdxnzo31q/2MbnNCAfSchsqrKtzBtheex4JvvqZjxn98wk5Te1QgZz\n" +
            "Caz4ay9dkLVjSt79QYm5hKb8Jt3O5SxSUsrjmYVeG+k2bQlidw8dENwLZmvJkIJi8kb94yEwY/dq\n" +
            "lENDkQIDAQAB";

    public static String CLOUDBEAVER_EE_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlAiyH9ghPpqATx" +
            "/FtCV2o5y6UKDFR+4c\n" +
            "LjvztuYyvfb161hrrtkcmLkBJUNfJQbG6y+yE6tJCoFPplobU7ztqIvWQchQbSyDCDyWeeob8SY5\n" +
            "Kj6w3WhuEz1FNDJkylYhbhSXPq80kxCD5EsPe/i8C3JCENFAPpxM2mfjQPrGbQTL3B5scBODh7oX\n" +
            "QgtvIIwYxno+o+wZ6dLayBG3HrSzDe7Qb7QtpZUsHSjEHekfW15rmp063jpL2wJTIZJXThLsTrT7\n" +
            "LzXY7cJHg2nh9JjpmOe7udAnaf6zpIeVZSo+zA4cLzuCuZhyDnCfUMpGpcgwGieNIa4bC2YXPu4T\n" +
            "F7jNpwIDAQAB";

    public byte[] localDBeaverUeKeyBytes;
    public byte[] localDBeaverEeKeyBytes;
    public byte[] localCloudBeaverEeKeyBytes;


    public OriginalCryptKey() throws Exception {
        String dBeaverUePublicKeyStr = DBEAVER_UE_PUBLIC_KEY.replaceAll("\\n", "").trim();
        this.localDBeaverUeKeyBytes = Base64.getDecoder().decode(dBeaverUePublicKeyStr.getBytes());
        String dBeaverEePublicKeyStr = DBEAVER_EE_PUBLIC_KEY.replaceAll("\\n", "").trim();
        this.localDBeaverEeKeyBytes = Base64.getDecoder().decode(dBeaverEePublicKeyStr.getBytes());
        String cloudBeaverEePublicKeyStr = CLOUDBEAVER_EE_PUBLIC_KEY.replaceAll("\\n", "").trim();
        this.localCloudBeaverEeKeyBytes = Base64.getDecoder().decode(cloudBeaverEePublicKeyStr.getBytes());
    }


    public Key getDBeaverUePublicKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(localDBeaverUeKeyBytes);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Key getDBeaverEePublicKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(localDBeaverEeKeyBytes);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Key getCloudBeaverEePublicKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(localCloudBeaverEeKeyBytes);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

}
