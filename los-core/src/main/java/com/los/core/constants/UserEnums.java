package com.los.core.constants;

/*
 * @author paul 2024/2/4
 */

public enum UserEnums {
    /*
     * 角色
     */
    MERCHANT("MCH-商户中心"),
    MANAGER("MGR-运营平台");
    private final String role;

    UserEnums(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
