package com.meishe.sdkdemo.mimodemo.common.permission;

import android.content.Context;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;


public class PermissionsChecker {
    private final Context mContext;

    public PermissionsChecker(Context context) {
        mContext = context.getApplicationContext();
    }

    public List<String> checkPermission(String... permissions) {
        List<String> newList = new ArrayList<>();
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                newList.add(permission);
            }
        }
        return newList;
    }

    /**
     * 判断权限集合
     * Judgment permission set
     *
     * @param permissions
     * @return
     */
    public List<String> checkPermission(List<String> permissions) {
        List<String> newList = new ArrayList<>();
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                newList.add(permission);
            }
        }
        return newList;
    }

    public boolean lacksPermissions(String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean lacksPermissions(List<String> permissions) {

        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断是否缺少权限
     * Determine whether permissions are lacking
     *
     * @param permission
     * @return
     */
    private boolean lacksPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }

}
