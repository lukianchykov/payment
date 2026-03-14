package com.iprody.payment.service.app.config.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.annotation.Annotation;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PaymentControllerLoggingAspect {

    private final ObjectMapper objectMapper;

    @Pointcut("within(com.iprody.payment.service.app.controller.PaymentController)")
    public void paymentControllerMethods() { }

    @Before("paymentControllerMethods()")
    public void logMethodEntry(JoinPoint joinPoint) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String methodName = signature.getName();
        final String operation = getOperationType(methodName);

        final UUID id = extractUuidFromArgs(joinPoint);

        if (id != null) {
            log.info("[Payment] Entering {} operation, id={}", operation, id);
        } else {
            log.info("[Payment] Entering {} operation, args={}", operation, serializeArgs(joinPoint.getArgs()));
        }
    }

    @AfterReturning(pointcut = "paymentControllerMethods()", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String methodName = signature.getName();
        final String operation = getOperationType(methodName);

        final UUID id = extractUuidFromArgs(joinPoint);

        if (id != null) {
            log.info("[Payment] Completed {} operation, id={}", operation, id);
        } else {
            log.info("[Payment] Completed {} operation", operation);
        }

        if (result != null) {
            log.debug("[Payment] Operation {} result: {}", operation, serializeResult(result));
        }
    }

    private String getOperationType(String methodName) {
        if (methodName.startsWith("create")) return "CREATE";
        if (methodName.startsWith("update") || methodName.startsWith("updateStatus")
                || methodName.startsWith("updateNote")) return "UPDATE";
        if (methodName.startsWith("delete")) return "DELETE";
        if (methodName.startsWith("get")) return "GET";
        if (methodName.startsWith("find") || methodName.startsWith("search")) return "SEARCH";
        return "UNKNOWN";
    }

    private UUID extractUuidFromArgs(JoinPoint joinPoint) {
        final Object[] args = joinPoint.getArgs();
        final Annotation[][] paramAnnotations = ((MethodSignature) joinPoint.getSignature())
                .getMethod().getParameterAnnotations();

        for (int i = 0; i < args.length; i++) {
            for (Annotation ann : paramAnnotations[i]) {
                if (ann instanceof PathVariable && args[i] instanceof UUID) {
                    return (UUID) args[i];
                }
            }
        }
        return null;
    }

    private String serializeArgs(Object[] args) {
        try {
            return objectMapper.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            return "[Could not serialize arguments]";
        }
    }

    private String serializeResult(Object result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "[Could not serialize result]";
        }
    }
}