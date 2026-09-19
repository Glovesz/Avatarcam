package com.glovesz.avatarcam;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.reflect.Method;

final class VirtualCameraProbe {
    static final String CREATE_VIRTUAL_DEVICE = "android.permission.CREATE_VIRTUAL_DEVICE";

    enum State { SUPPORTED, UNSUPPORTED, API_NOT_PRESENT, ERROR }

    static final class Result {
        final State state;
        final boolean hasPrivilegedPermission;
        final boolean configClassPresent;
        final String detail;

        Result(State state, boolean hasPrivilegedPermission, boolean configClassPresent, String detail) {
            this.state = state;
            this.hasPrivilegedPermission = hasPrivilegedPermission;
            this.configClassPresent = configClassPresent;
            this.detail = detail;
        }
    }

    private VirtualCameraProbe() {}

    static Result run(Context context) {
        boolean permission = context.checkSelfPermission(CREATE_VIRTUAL_DEVICE) == PackageManager.PERMISSION_GRANTED;
        boolean configPresent;
        try {
            Class.forName("android.companion.virtual.camera.VirtualCameraConfig");
            configPresent = true;
        } catch (Throwable ignored) {
            configPresent = false;
        }

        try {
            Class<?> manager = Class.forName("android.companion.virtual.VirtualDeviceManager");
            Method m = manager.getMethod("isVirtualCameraSupported");
            Object raw = m.invoke(null);
            boolean supported = raw instanceof Boolean && (Boolean) raw;
            return new Result(
                    supported ? State.SUPPORTED : State.UNSUPPORTED,
                    permission,
                    configPresent,
                    "Android " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")"
            );
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return new Result(State.API_NOT_PRESENT, permission, configPresent,
                    "Virtual-camera capability API is not present on this firmware build.");
        } catch (Throwable t) {
            return new Result(State.ERROR, permission, configPresent,
                    t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage()));
        }
    }
}
