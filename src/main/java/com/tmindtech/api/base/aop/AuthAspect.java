package com.tmindtech.api.base.aop;

import com.tmindtech.api.base.Config;
import com.tmindtech.api.base.annotation.Auth;
import com.tmindtech.api.base.annotation.Permission;
import com.tmindtech.api.base.auth.AuthSession;
import com.tmindtech.api.base.exception.AwesomeException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by RexQian on 2017/2/10.
 */
@Aspect
@Component
public class AuthAspect {

    private static Logger LOGGER = Logger.getLogger(AuthAspect.class.getName());

    @Autowired
    AuthSession authSession;

    private PermissionCheckCallback permissionCheckCallback;

    /**
     * 缓存方法，减少反射查找方法时间，提高性能.
     */
    private static ConcurrentHashMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<String, Method>();

    @Pointcut("@annotation(com.tmindtech.api.base.annotation.Auth)"
            + " || @within(com.tmindtech.api.base.annotation.Auth)")
    @Order(OrderDef.ORDER_AUTH)
    public void guard() {
    }

    private boolean hasPermission(Permission[] permissions) throws AwesomeException {
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        boolean result = false;
        if (permissionCheckCallback != null) {
            result = permissionCheckCallback.check(permissions);
        }
        return result;
    }

    private Auth getAuth(JoinPoint joinPoint) {
        Method method = getMethod(joinPoint);
        if (method == null) {
            throw new AwesomeException(Config.ERROR_INTERNAL);
        }
        //先获取方法上的注解
        Auth auth = method.getAnnotation(Auth.class);
        if (auth == null) {
            //得到类上的访问注解
            auth = joinPoint.getTarget().getClass().getAnnotation(Auth.class);
        }
        return auth;
    }

    @Before("guard()")
    private void doGuard(JoinPoint joinPoint)
            throws AwesomeException {
        Auth auth = getAuth(joinPoint);
        if (!authSession.isAuth() && !(auth.preAuth() && authSession.isPreAuth())) {
            authSession.authDeny();
        }

        // 超级管理员豁免所有权限
        if (authSession.getAuthId() == Config.SUPER_MAN_ID) {
            return;
        }

        if (!hasPermission(auth.value())) {
            throw new AwesomeException(Config.ERROR_ACCESS_DENY);
        }
    }

    /**
     * 利用反射得到该方法.
     */
    private Method getMethod(final JoinPoint joinPoint) {
        // 方法
        Object[] args = joinPoint.getArgs();
        Method method = null;
        String methodName = joinPoint.getSignature().getName();
        String key = joinPoint.getSignature().toLongString();//方法名+参数类型
        if (args == null || args.length == 0) {
            method = METHOD_CACHE.get(key);
            if (method == null) {
                try {
                    method = joinPoint.getTarget().getClass().getDeclaredMethod(methodName);
                } catch (NoSuchMethodException ex) {
                    return null;
                }
                method = METHOD_CACHE.putIfAbsent(key, method);
            }
        } else {
            StringBuilder keyBuilder = new StringBuilder(key);
            for (Object arg : args) {
                if (arg != null) {
                    keyBuilder.append("-").append(arg.getClass());
                }
            }
            key = keyBuilder.toString();
            method = METHOD_CACHE.get(key);
            if (method == null) {
                //getMethod(methodName, cla)
                //int Integer, long 与 Long 对应的class都是不一样的，所以要求同一个class下不要重载
                //即方法名不要有一样
                Method[] declaredMethods = joinPoint.getTarget().getClass().getDeclaredMethods();
                for (Method loopMethod : declaredMethods) {
                    if (loopMethod != null && loopMethod.getName().equals(methodName)) {
                        method = METHOD_CACHE.putIfAbsent(key, loopMethod);
                    }
                }
            }
        }

        return method;
    }

    public interface PermissionCheckCallback {
        boolean check(Permission[] permissions) throws AwesomeException;
    }

    public void setPermissionCheckCallback(PermissionCheckCallback callback) {
        this.permissionCheckCallback = callback;
    }
}
