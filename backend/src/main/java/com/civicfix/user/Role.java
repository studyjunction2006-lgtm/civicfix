package com.civicfix.user;

public enum Role {
    CITIZEN,
    AUTHORITY,
    WORKER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
