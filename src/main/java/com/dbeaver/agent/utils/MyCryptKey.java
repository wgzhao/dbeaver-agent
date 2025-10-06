package com.dbeaver.agent.utils;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/*
 * 自定义密钥工具类
 * 用于生成和获取自定义密钥（私钥、公钥），供 Agent 拦截器使用。
 */
public class MyCryptKey
{
    public static String RSA_PRIVATE_KEY = """
            MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDG+OYD6GRjeTBd
            HgJLfAfvBgJaFXNud+DvP9/UvIiMtnTKhYJZ634QDx0Vh0O1lWykfq/KbnuxgjmT
            f2Wl006sFuGF1eDuwxkPBYBuoBEF4K8rmB+jvq3jSHB+KzrHgYozVCib1osKI+Qp
            3KYY0AXiKNREGB9EGSRfFH9ydyVIGlguPtAd4iMg0Bigzb9+SIh6hNh+ZRtthjtd
            cE/wTjYZnHdlzygxrNGA6mXaZQKAIQ1MjqvB6w2g3WJriVOWHXnz7TinkwDAPhCL
            fLW6j9gELGLkDcdG6xOJprl+XXzbsckE0/UZ+wlOagEB8Fo87te49bYnXN/U0FC7
            CzVCkJi9AgMBAAECggEATenjMD6NKQKotJ3uqh5cgPWqcBocRHK+6xDpFkXpdqhv
            3WogXBPCHom8itSX6AAmNdfCAJP47c6fuylU5XV0RiEDmMPiu7w8EMzHuQoHAHU2
            Quzj3tvo/ao9GrrU3pDUTDs1V6jQc10QmG/lvque0ivIyw1jGOh7fJvrOCh9udie
            U5EK4Zd1oWW1jYghmcSVYPmMQQV914Q/nGAJK1PTJFrQw229pJ/dcfkopXeiYX7y
            d0nQ3PHJKFv4oZ+7U6sFz2CKp6Kaf2x9tpGdDtrgzh0GexjJOPhcCykaBrpVZ4Ml
            eCpplLfoPReBJSJn4MuQGhjoqUjg3U4aMUX+HIZawQKBgQDiI7fKjV3vyBziXodR
            Vra0X8fBajRP47qpvSVxU4R8WoZ0WBmr3YPCKKGRaD1unO+q02IWJjWSqM5scJAN
            wG/Hm3cVjXNTE0q4Ic4TWi4DGkmeCTgdLV3xsKCAJnwGyaRm5CVxMy19TYaT2MjL
            Pqk6wDugKJlFa0Q/STsKXlLLDQKBgQDhPtXDc2yyOcXTnoVxoxLoztOAsXngaCWn
            57NEaIbTVOhfgOucmw0ErDjNmLT4o2L7pN+xLspbonfAyQr/6apzKsKrICLovW4c
            o6qMTl38DCIbbjghAnZZIGMiLAj2cjXLRpl9UQRDPhcgdo774h3m5KBSXVsb3KTs
            CGuw4vrYcQKBgQCklNxBdYuFZL3o3mVbhGGqev71vGbgMdx7hqaGiQMmQfgTGr9s
            PuaS22FjY1s6cstXJ0r+1cYtu9+oOnANIh34RyRvMihZsPOzeR7zJLHSHkKv2wPU
            8Fyr9yGIwvmHMyAMpkS900JrLI4icicJDMy7boa2tHWAeWYNLS3kCOv2zQKBgAEH
            E1yX6zgI8XEcuQkisFRllStyI9c1Qm64rOW0AGF4crL02J5XSsDptDyj5Ld5r1rs
            jqS1TPEMFbXIdQdj3oY1/LthIyeirZzt1jpbpSwgNguf/huQck/HyQv/3W6aaMpr
            oQmkU/umjd51DAP0LSS/iEe361F+OTZmd29QKcrRAoGAfH5mPi/wEHF5inOxt+vr
            6gMm5dypslijJ8dMPAWdw4gOagH0Nd02P2SaOm4qZ+WeEOhpKvugG84xy6OKeIVn
            08d6j9Ob5iuwVYs7SYZKaO/+q91AbwldnwgNaYneXCxYAj5+MSp82h/0o2cwZtNE
            "KL9lIgIOVNhRnvIMDLfn1Y8=
            """.replaceAll("[^A-Za-z0-9+/=]", "");

    public byte[] localKeyBytes;

    public MyCryptKey()
            throws Exception
    {
        this.localKeyBytes = Base64.getDecoder().decode(RSA_PRIVATE_KEY);
    }

    public Key getPublicKey()
    {
        try {
            PrivateKey myPrivateKey = (PrivateKey) getPrivateKey();
            RSAPrivateCrtKey privk = (RSAPrivateCrtKey) myPrivateKey;
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(publicKeySpec);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Key getPrivateKey()
    {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(localKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
