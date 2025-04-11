package com.cbc_sender.feature.auth.security;

import com.cbc_sender.config.RequiresRole;
import com.cbc_sender.exceptions.error.UnauthorizedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class RoleSecurityAspect {

    @Around("@annotation(com.mailsender.api.security.annotation.RequiresRole) || " +
            "@within(com.mailsender.api.security.annotation.RequiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get method signature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Get annotation from method or class
        RequiresRole annotation = AnnotationUtils.findAnnotation(method, RequiresRole.class);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), RequiresRole.class);
        }

        if (annotation != null) {
            // Check roles
            String[] requiredRoles = annotation.value();
            boolean anyRole = annotation.anyRole();
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new UnauthorizedException("User not authenticated");
            }

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Set<String> userRoles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            
            if (anyRole) {
                // Check if user has any of the required roles
                boolean hasAnyRole = Arrays.stream(requiredRoles)
                        .anyMatch(userRoles::contains);
                if (!hasAnyRole) {
                    throw new UnauthorizedException("User does not have required role");
                }
            } else {
                // Check if user has all of the required roles
                boolean hasAllRoles = Arrays.stream(requiredRoles)
                        .allMatch(userRoles::contains);
                if (!hasAllRoles) {
                    throw new UnauthorizedException("User does not have all required roles");
                }
            }
        }
        
        return joinPoint.proceed();
    }
}