package dev.misakacloud.dbee;

import dev.misakacloud.dbee.interceptor.*;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String arg, Instrumentation inst) {
        install(arg, inst);
    }

    public static void agentmain(String arg, Instrumentation inst) {
        install(arg, inst);
    }
    public static void install(String agentArgs, Instrumentation inst) {
        System.out.println("===============DBeaver-EE Agent===============");
        System.out.println("开始进行类替换");
        AgentBuilder.Listener listener = new AgentBuilder.Listener() {
            @Override
            public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {

            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {

            }

            @Override
            public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {

            }

            @Override
            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

            }

        };
        System.out.println("准备劫持解密密钥获取");
        new AgentBuilder
                .Default()
                // 指定需要拦截的类
                //.type(ElementMatchers.nameContains("com.dbeaver.ee.runtime.lm.DBeaverEnterpriseLM"))
//                .type(ElementMatchers.nameContains("com.dbeaver.lm.embedded.LicenseServiceEmbedded"))
                .type(ElementMatchers.nameContains("com.dbeaver.model.license.embedded.LicenseKeyProviderEmbedded"))
                .transform((builder, type, classLoader, module) -> builder
                        .method(ElementMatchers.named("getDecryptionKey"))
                        .intercept(MethodDelegation.to(LoadKeyInterceptor.class)))
//                        // 拦截 getActiveProductLicense
//                        .method(ElementMatchers.named("getActiveProductLicense"))
//                        .intercept(MethodDelegation.to(LicenseInterceptor.class)))
                .installOn(inst);
        System.out.println("解密密钥获取已经劫持");
        System.out.println("劫持证书检查");

        new AgentBuilder
                .Default()
                // 指定需要拦截的类
                .type(ElementMatchers.nameContains("com.dbeaver.model.license.validate.PublicLicenseValidator"))
                .transform((builder, type, classLoader, module) -> builder
                        .method(ElementMatchers.named("validateLicense"))
                        .intercept(MethodDelegation.to(PublicLicenseValidatorInterceptor.class)))
                .with(listener)
                .installOn(inst);
        System.out.println("准备修改验证结果");
        // 验证结果修改
        new AgentBuilder
                .Default()
                // 指定需要拦截的类
                .type(ElementMatchers.nameContains("com.dbeaver.model.license.validate.PublicServiceClient"))
                .transform((builder, type, classLoader, module) -> builder
                        .method(ElementMatchers.named("ping"))
                        .intercept(MethodDelegation.to(PingCheckInterceptor.class))
                        // 拦截 checkCustomerEmail 方法
                        .method(ElementMatchers.named("checkCustomerEmail"))
                        .intercept(MethodDelegation.to(CheckCustomerInterceptor.class))
                        // 拦截 checkLicenseStatus 方法
                        .method(ElementMatchers.named("checkLicenseStatus"))
                        .intercept(MethodDelegation.to(CheckLicenseInterceptor.class)))
                .with(listener)
                .installOn(inst);
        System.out.println("验证返回结果修改完成,启动程序");

    }


}
