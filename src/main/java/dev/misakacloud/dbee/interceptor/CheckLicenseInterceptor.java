package dev.misakacloud.dbee.interceptor;

import dev.misakacloud.dbee.utils.LogUtils;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class CheckLicenseInterceptor {
    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws Exception {
        LogUtils.debug("===============CheckLicenseInterceptor.intercept===============");
        String response = "VALID: Ok";
//        String response = "INVALID: License 'DB-ZS1MPZK4--FVC' not found";
        return response;
    }
}
