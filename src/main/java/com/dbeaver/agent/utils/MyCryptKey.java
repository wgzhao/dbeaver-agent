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

public class MyCryptKey
{
    public static String RSA_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDG+OYD6GRjeTBd\n" +
            "HgJLfAfvBgJaFXNud+DvP9/UvIiMtnTKhYJZ634QDx0Vh0O1lWykfq/KbnuxgjmT\n" +
            "f2Wl006sFuGF1eDuwxkPBYBuoBEF4K8rmB+jvq3jSHB+KzrHgYozVCib1osKI+Qp\n" +
            "3KYY0AXiKNREGB9EGSRfFH9ydyVIGlguPtAd4iMg0Bigzb9+SIh6hNh+ZRtthjtd\n" +
            "cE/wTjYZnHdlzygxrNGA6mXaZQKAIQ1MjqvB6w2g3WJriVOWHXnz7TinkwDAPhCL\n" +
            "fLW6j9gELGLkDcdG6xOJprl+XXzbsckE0/UZ+wlOagEB8Fo87te49bYnXN/U0FC7\n" +
            "CzVCkJi9AgMBAAECggEATenjMD6NKQKotJ3uqh5cgPWqcBocRHK+6xDpFkXpdqhv\n" +
            "3WogXBPCHom8itSX6AAmNdfCAJP47c6fuylU5XV0RiEDmMPiu7w8EMzHuQoHAHU2\n" +
            "Quzj3tvo/ao9GrrU3pDUTDs1V6jQc10QmG/lvque0ivIyw1jGOh7fJvrOCh9udie\n" +
            "U5EK4Zd1oWW1jYghmcSVYPmMQQV914Q/nGAJK1PTJFrQw229pJ/dcfkopXeiYX7y\n" +
            "d0nQ3PHJKFv4oZ+7U6sFz2CKp6Kaf2x9tpGdDtrgzh0GexjJOPhcCykaBrpVZ4Ml\n" +
            "eCpplLfoPReBJSJn4MuQGhjoqUjg3U4aMUX+HIZawQKBgQDiI7fKjV3vyBziXodR\n" +
            "Vra0X8fBajRP47qpvSVxU4R8WoZ0WBmr3YPCKKGRaD1unO+q02IWJjWSqM5scJAN\n" +
            "wG/Hm3cVjXNTE0q4Ic4TWi4DGkmeCTgdLV3xsKCAJnwGyaRm5CVxMy19TYaT2MjL\n" +
            "Pqk6wDugKJlFa0Q/STsKXlLLDQKBgQDhPtXDc2yyOcXTnoVxoxLoztOAsXngaCWn\n" +
            "57NEaIbTVOhfgOucmw0ErDjNmLT4o2L7pN+xLspbonfAyQr/6apzKsKrICLovW4c\n" +
            "o6qMTl38DCIbbjghAnZZIGMiLAj2cjXLRpl9UQRDPhcgdo774h3m5KBSXVsb3KTs\n" +
            "CGuw4vrYcQKBgQCklNxBdYuFZL3o3mVbhGGqev71vGbgMdx7hqaGiQMmQfgTGr9s\n" +
            "PuaS22FjY1s6cstXJ0r+1cYtu9+oOnANIh34RyRvMihZsPOzeR7zJLHSHkKv2wPU\n" +
            "8Fyr9yGIwvmHMyAMpkS900JrLI4icicJDMy7boa2tHWAeWYNLS3kCOv2zQKBgAEH\n" +
            "E1yX6zgI8XEcuQkisFRllStyI9c1Qm64rOW0AGF4crL02J5XSsDptDyj5Ld5r1rs\n" +
            "jqS1TPEMFbXIdQdj3oY1/LthIyeirZzt1jpbpSwgNguf/huQck/HyQv/3W6aaMpr\n" +
            "oQmkU/umjd51DAP0LSS/iEe361F+OTZmd29QKcrRAoGAfH5mPi/wEHF5inOxt+vr\n" +
            "6gMm5dypslijJ8dMPAWdw4gOagH0Nd02P2SaOm4qZ+WeEOhpKvugG84xy6OKeIVn\n" +
            "08d6j9Ob5iuwVYs7SYZKaO/+q91AbwldnwgNaYneXCxYAj5+MSp82h/0o2cwZtNE\n" +
            "KL9lIgIOVNhRnvIMDLfn1Y8=\n";

    public byte[] localKeyBytes;

    public MyCryptKey()
            throws Exception
    {
        String privateKeyStr = RSA_PRIVATE_KEY.replaceAll("\\n", "").trim();
        this.localKeyBytes = Base64.getDecoder().decode(privateKeyStr.getBytes());
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
