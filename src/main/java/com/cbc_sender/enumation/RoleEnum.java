package com.cbc_sender.enumation;

public enum RoleEnum {
    ADMIN,
    DEVELOPER,
    USER;

    public String getValue() {
        return this.name();
    }
}