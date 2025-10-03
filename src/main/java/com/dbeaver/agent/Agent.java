package com.dbeaver.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class Agent {
        // RSA 私钥
        private static final String RSA_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDG+OYD6GRjeTBd\n"
                        +
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

        public static void premain(String arg, Instrumentation inst) {
                install(arg, inst);
        }

        public static void agentmain(String arg, Instrumentation inst) {
                install(arg, inst);
        }

        /**
         * 生成公钥（从私钥导出）
         */
        private static Key generatePublicKey() {
                try {
                        String privateKeyStr = RSA_PRIVATE_KEY.replaceAll("\\n", "").trim();
                        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);

                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);
                        java.security.PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

                        RSAPrivateCrtKey privk = (RSAPrivateCrtKey) privateKey;
                        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(privk.getModulus(),
                                        privk.getPublicExponent());

                        return keyFactory.generatePublic(publicKeySpec);
                } catch (Exception e) {
                        System.err.println("生成公钥失败: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                }
        }

        public static void install(String agentArgs, Instrumentation inst) {
                System.out.println("===============DBeaver-EE Agent===============");
                System.out.println("开始进行类替换");

                // 配置 AgentBuilder 使 interceptor 类对所有模块可见
                AgentBuilder.Listener listener = new AgentBuilder.Listener() {
                        @Override
                        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module,
                                        boolean loaded) {
                        }

                        @Override
                        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader,
                                        JavaModule module, boolean loaded, DynamicType dynamicType) {

                        }

                        @Override
                        public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader,
                                        JavaModule module, boolean loaded) {

                        }

                        @Override
                        public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
                                        Throwable throwable) {

                        }

                        @Override
                        public void onComplete(String typeName, ClassLoader classLoader, JavaModule module,
                                        boolean loaded) {

                        }
                };

                System.out.println("准备劫持解密密钥获取");

                // 预先生成公钥
                final Key publicKey = generatePublicKey();
                if (publicKey == null) {
                        System.err.println("警告: 公钥生成失败，agent 可能无法正常工作");
                }

                new AgentBuilder.Default()
                                // 指定需要拦截的类
                                .type(ElementMatchers.nameContains(
                                                "com.dbeaver.model.license.embedded.LicenseKeyProviderEmbedded"))
                                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                                                .method(ElementMatchers.named("getDecryptionKey"))
                                                // 直接返回公钥，不依赖外部类
                                                .intercept(FixedValue.value(publicKey)))
                                .installOn(inst);
                System.out.println("解密密钥获取已经劫持");
                System.out.println("劫持证书检查");

                new AgentBuilder.Default()
                                // 指定需要拦截的类
                                .type(ElementMatchers.nameContains(
                                                "com.dbeaver.model.license.validate.PublicLicenseValidator"))
                                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                                                .method(ElementMatchers.named("validateLicense"))
                                                // 直接返回 "VALID: Ok"
                                                .intercept(FixedValue.value("VALID: Ok")))
                                .with(listener)
                                .installOn(inst);
                System.out.println("准备修改验证结果");
                // 验证结果修改
                new AgentBuilder.Default()
                                // 指定需要拦截的类
                                .type(ElementMatchers
                                                .nameContains("com.dbeaver.model.license.validate.PublicServiceClient"))
                                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                                                .method(ElementMatchers.named("ping"))
                                                // 直接返回 "pong"
                                                .intercept(FixedValue.value("pong"))
                                                // 拦截 checkCustomerEmail 方法
                                                .method(ElementMatchers.named("checkCustomerEmail"))
                                                // 直接返回空字符串
                                                .intercept(FixedValue.value(""))
                                                // 拦截 checkLicenseStatus 方法
                                                .method(ElementMatchers.named("checkLicenseStatus"))
                                                // 直接返回 "VALID: Ok"
                                                .intercept(FixedValue.value("VALID: Ok")))
                                .with(listener)
                                .installOn(inst);
                System.out.println("验证返回结果修改完成,启动程序");
        }
}
