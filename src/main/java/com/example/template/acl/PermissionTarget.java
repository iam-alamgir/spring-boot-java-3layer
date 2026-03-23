package com.example.template.acl;

public record PermissionTarget(String resourceType, String resourceKey, String action) {
    public static PermissionTarget controller(String controllerName) {
        return new PermissionTarget("CONTROLLER", controllerName, "ACCESS");
    }

    public static PermissionTarget endpoint(String endpointKey, String action) {
        return new PermissionTarget("ENDPOINT", endpointKey, action);
    }
}
