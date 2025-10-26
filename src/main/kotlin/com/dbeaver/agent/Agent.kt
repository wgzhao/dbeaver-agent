/*
 * DBeaver-EE Java Agent
 * 负责注册所有 ByteBuddy 拦截器，实现对目标程序关键方法的劫持与替换。
 * 包括密钥获取、证书校验、License 状态检查等流程。
 */
package com.dbeaver.agent

import com.dbeaver.agent.interceptor.GenericInterceptor
import com.dbeaver.agent.interceptor.LoadKeyInterceptor
import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.utility.JavaModule
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

object Agent {
    @JvmStatic
    fun premain(arg: String?, inst: Instrumentation) {
        install(arg, inst)
    }

    @JvmStatic
    fun agentmain(arg: String?, inst: Instrumentation) {
        install(arg, inst)
    }

    fun install(agentArgs: String?, inst: Instrumentation) {
        println("===============DBeaver-EE Agent===============")
        println("开始进行类替换")
        val listener: AgentBuilder.Listener = object : AgentBuilder.Listener {
            override fun onDiscovery(typeName: String, classLoader: ClassLoader?, module: JavaModule?, loaded: Boolean) {
            }

            override fun onTransformation(typeDescription: TypeDescription, classLoader: ClassLoader?, module: JavaModule?, loaded: Boolean, dynamicType: DynamicType) {
            }

            override fun onIgnored(typeDescription: TypeDescription, classLoader: ClassLoader?, module: JavaModule?, loaded: Boolean) {
            }

            override fun onError(typeName: String, classLoader: ClassLoader?, module: JavaModule?, loaded: Boolean, throwable: Throwable) {
            }

            override fun onComplete(typeName: String, classLoader: ClassLoader?, module: JavaModule?, loaded: Boolean) {
            }
        }
        println("准备劫持解密密钥获取")
        AgentBuilder.Default() // 指定需要拦截的类
            .type(ElementMatchers.nameContains<TypeDescription?>("com.dbeaver.model.license.embedded.LicenseKeyProviderEmbedded"))
            .transform(AgentBuilder.Transformer { builder: DynamicType.Builder<*>?, typeDescription: TypeDescription?, classLoader: ClassLoader?, module: JavaModule?, protectionDomain: ProtectionDomain? ->
                builder!!
                    .method(ElementMatchers.named<MethodDescription?>("getDecryptionKey"))
                    .intercept(MethodDelegation.to(LoadKeyInterceptor::class.java))
            })
            .installOn(inst)
        println("解密密钥获取已经劫持")
        println("劫持证书检查")

        AgentBuilder.Default()
            .type(ElementMatchers.nameContains<TypeDescription?>("com.dbeaver.model.license.validate.PublicLicenseValidator"))
            .transform(AgentBuilder.Transformer { builder: DynamicType.Builder<*>?, typeDescription: TypeDescription?, classLoader: ClassLoader?, module: JavaModule?, protectionDomain: ProtectionDomain? ->
                builder!!
                    .method(ElementMatchers.named<MethodDescription?>("validateLicense"))
                    .intercept(MethodDelegation.to(GenericInterceptor("VALID: Ok")))
            })
            .with(listener)
            .installOn(inst)
        println("准备修改验证结果")
        // 验证结果修改
        AgentBuilder.Default()
            .type(ElementMatchers.nameContains<TypeDescription?>("com.dbeaver.model.license.validate.PublicServiceClient"))
            .transform(AgentBuilder.Transformer { builder: DynamicType.Builder<*>?, typeDescription: TypeDescription?, classLoader: ClassLoader?, module: JavaModule?, protectionDomain: ProtectionDomain? ->
                builder!!
                    .method(ElementMatchers.named<MethodDescription?>("ping"))
                    .intercept(MethodDelegation.to(GenericInterceptor("pong"))) // 拦截 checkCustomerEmail 方法
                    .method(ElementMatchers.named<MethodDescription?>("checkCustomerEmail"))
                    .intercept(MethodDelegation.to(GenericInterceptor(""))) // 拦截 checkLicenseStatus 方法
                    .method(ElementMatchers.named<MethodDescription?>("checkLicenseStatus"))
                    .intercept(MethodDelegation.to(GenericInterceptor("VALID: Ok")))
            })
            .with(listener)
            .installOn(inst)
        println("验证返回结果修改完成,启动程序")
    }
}