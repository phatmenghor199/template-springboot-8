package com.cbc_sender.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RequiresRole {
    String[] value();
    boolean anyRole() default false; // If true, user needs any of the roles. If false, user needs all roles.
}