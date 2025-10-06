/*
 * LicenseKeyProviderEmbedded#getDecryptionKey 方法拦截器
 * 用于劫持密钥获取流程，返回自定义公钥。
 */

package com.dbeaver.agent.interceptor;

import com.dbeaver.agent.utils.MyCryptKey;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class LoadKeyInterceptor
{

    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable)
            throws Exception
    {
        return new MyCryptKey().getPublicKey();
    }
}
