package com.isom.dataserver.common.audit;

import com.isom.dataserver.common.audit.entity.AuditLogEntity;
import com.isom.dataserver.common.audit.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogMapper auditLogMapper;

    @Around("@annotation(com.isom.dataserver.common.audit.AuditLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLog = method.getAnnotation(AuditLog.class);

        Long targetId = extractTargetId(point, signature);

        Object result = point.proceed();

        try {
            AuditLogEntity entity = new AuditLogEntity();
            entity.setOperator(1L);
            entity.setAction(auditLog.action());
            entity.setTargetType(auditLog.targetType());
            entity.setTargetId(targetId);
            entity.setIp(getClientIp());
            auditLogMapper.insert(entity);
        } catch (Exception e) {
            log.error("Failed to write audit log", e);
        }

        return result;
    }

    private Long extractTargetId(ProceedingJoinPoint point, MethodSignature signature) {
        Object[] args = point.getArgs();
        Parameter[] params = signature.getMethod().getParameters();
        for (int i = 0; i < params.length; i++) {
            if ("id".equals(params[i].getName()) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }
        if (args.length > 0 && args[0] instanceof Long) {
            return (Long) args[0];
        }
        return null;
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                return ip != null ? ip.split(",")[0].trim() : request.getRemoteAddr();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
