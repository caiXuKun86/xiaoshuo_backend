package com.kun.common.core.aop;

import com.kun.common.core.context.LoginUser;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Arrays;

@Aspect
public class CheckRoleAspect {
    @Around("@annotation(requireRole)")
    public Object doAround(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        LoginUser user = UserContextHolder.get();
        String[] value = requireRole.value();
        if (user == null || !Arrays.asList(value).contains(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN_ACCESS); // 403: 无管理端操作权限
        }
        return joinPoint.proceed();
    }
}
