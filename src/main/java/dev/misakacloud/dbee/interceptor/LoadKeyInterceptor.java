package dev.misakacloud.dbee.interceptor;


import dev.misakacloud.dbee.utils.LogUtils;
import dev.misakacloud.dbee.utils.MyCryptKey;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class LoadKeyInterceptor {


    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws Exception {
        LogUtils.debug("===============LoadKeyInterceptor.intercept===============");
        return new MyCryptKey().getPublicKey();
//            return callable.call();
    }
}
