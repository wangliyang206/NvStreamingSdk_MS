package com.meishe.sdkdemo.mimodemo.common.base;

import android.content.Intent;
import android.os.Bundle;


import com.meishe.sdkdemo.mimodemo.common.permission.PermissionsActivity;
import com.meishe.sdkdemo.mimodemo.common.permission.PermissionsChecker;

import java.util.List;

/**
 * Created by CaoZhiChao on 2018/12/11 14:04
 */

public abstract class BasePermissionActivity extends BaseActivity {
    static final int REQUEST_CODE = 110;
    List<String> permissionList;
    private PermissionsChecker mPermissionsChecker;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        permissionList = initPermissions();
        super.onCreate(saveInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case PermissionsActivity.PERMISSIONS_DENIED:
                nonePermission();
                break;
            case PermissionsActivity.PERMISSIONS_GRANTED:
                hasPermission();
                break;
            case PermissionsActivity.PERMISSIONS_No_PROMPT:
                noPromptPermission();
                break;
        }
    }

    public boolean hasAllPermission() {
        if (mPermissionsChecker == null) {
            mPermissionsChecker = new PermissionsChecker(this);
        }
        return !mPermissionsChecker.lacksPermissions(permissionList);
    }

    public void checkPermissions() {
        if (mPermissionsChecker == null) {
            mPermissionsChecker = new PermissionsChecker(this);
        }
        permissionList = mPermissionsChecker.checkPermission(permissionList);
        String[] permissions = new String[permissionList.size()];
        permissionList.toArray(permissions);
        if (!permissionList.isEmpty()) {
            startPermissionsActivity(REQUEST_CODE, permissions);
        }
    }

    private void startPermissionsActivity(int code, String... permission) {
        PermissionsActivity.startActivityForResult(this, code, permission);
    }

    /**
     * 获取activity需要的权限列表
     * Gets a list of permissions the activity needs
     *
     * @return 权限列表 Permission list
     */
    protected abstract List<String> initPermissions();

    /**
     * 获取权限后
     * After obtaining permission
     */
    protected abstract void hasPermission();

    /**
     * 没有允许权限
     * No permission
     */
    protected abstract void nonePermission();

    /**
     * 用户选择了不再提示
     * The user chose not to prompt
     */
    protected abstract void noPromptPermission();
}
