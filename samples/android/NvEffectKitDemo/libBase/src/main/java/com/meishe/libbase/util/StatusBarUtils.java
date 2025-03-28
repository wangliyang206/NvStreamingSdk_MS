package com.meishe.libbase.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.meishe.libbase.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.ColorUtils;


/**
 * @author LiFei
 * @version 1.0
 * @title
 * @description 该类可支持修改状态栏文字颜色
 * @company
 * @created 2019/9/17 17:36
 * @changeRecord [修改记录] <br/>
 */
public class StatusBarUtils {
    private static final int FAKE_STATUS_BAR_VIEW_ID = R.id.fake_status_bar_view;

    /**
     * 获得状态栏的高度
     * Gets the height of status bar
     *
     * @param context 上下文 Context
     * @return 状态栏高度 the height of status bar
     */
    public static int getHeight(Context context) {
        int statusBarHeight = 0;
        try {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                    "android");
            if (resourceId > 0) {
                statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 设置状态栏颜色
     * Set the status bar color
     *
     * @param context 上下文，尽量使用Activity  Context, try to use Activity
     * @param color   状态栏颜色 color
     */
    public static void setColor(Context context, @ColorInt int color) {
        if (context instanceof Activity) {
            setColor(((Activity) context).getWindow(), color);
        }
    }

    /**
     * 设置状态栏颜色
     * Set the status bar color
     *
     * @param window 窗口，可用于Activity和Dialog等 Window, which can be used for activities, Dialog, etc
     * @param color  状态栏颜色 color
     */
    public static void setColor(@NonNull Window window, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.setStatusBarColor(color);
            setTextDark(window, !isDarkColor(color));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setColor(window, ColorUtils.blendARGB(Color.TRANSPARENT, color, 0.5f), false);
        }
    }

    /**
     * Android 5.0 以下版本设置状态栏颜色
     * Android version 5.0 or later sets the status bar color
     *
     * @param window        窗口 window
     * @param color         状态栏颜色值 Status bar color value
     * @param isTransparent 是否透明 Transparent or not
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setColor(@NonNull Window window, @ColorInt int color,
                                boolean isTransparent) {
        Context context = window.getContext();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        View contentView = decorView.findViewById(android.R.id.content);
        if (contentView != null) {
            contentView.setPadding(0, isTransparent ? 0 : getHeight(context), 0, 0);
        }
        View fakeStatusBarView = decorView.findViewById(FAKE_STATUS_BAR_VIEW_ID);
        if (fakeStatusBarView != null) {
            fakeStatusBarView.setBackgroundColor(color);
            if (fakeStatusBarView.getVisibility() == View.GONE) {
                fakeStatusBarView.setVisibility(View.VISIBLE);
            }
        } else {
            // 绘制一个和状态栏一样高的矩形  Draw a rectangle the same height as the status bar
            View statusBarView = new View(context);
            FrameLayout.LayoutParams layoutParams =
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            getHeight(context));
            statusBarView.setLayoutParams(layoutParams);
            statusBarView.setBackgroundColor(color);
            statusBarView.setId(FAKE_STATUS_BAR_VIEW_ID);
            decorView.addView(statusBarView);
        }
    }

    /**
     * 设置状态栏透明
     * Set the status bar transparent
     *
     * @param context 上下文，尽量使用Activity Context, try to use Activity
     */
    public static void setTransparent(Context context) {
        if (context instanceof Activity) {
            setTransparent(((Activity) context).getWindow());
        }
    }

    /**
     * 设置状态栏透明
     * Set the status bar transparent
     *
     * @param window 窗口，可用于Activity和Dialog等  Window, which can be used for activities, Dialog, etc
     */
    public static void setTransparent(@NonNull Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setColor(window, 0x80000000, true);
        }
    }

    /**
     * 设置状态栏是否为黑色文字
     * Sets whether the status bar is black
     *
     * @param context context
     * @param isDark  是否为黑色文字 Whether the text is black
     */
    public static void setTextDark(Context context, boolean isDark) {
        if (context instanceof Activity) {
            setTextDark(((Activity) context).getWindow(), isDark);
        }
    }

    /**
     * 设置状态栏是否为黑色文字
     * Sets whether the status bar is black
     *
     * @param window 窗口，可用于Activity和全屏Dialog  Window for activities and full-screen Dialog
     * @param isDark 是否为黑色文字  Whether the text is black
     */
    private static void setTextDark(Window window, boolean isDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            int systemUiVisibility = decorView.getSystemUiVisibility();
            if (isDark) {
                decorView.setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decorView.setSystemUiVisibility(systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switch (OSUtils.getRomType()) {
                case MIUI:
                    setMIUIDark(window, isDark);
                    break;
                case Flyme:
                    setFlymeDark(window, isDark);
                    break;
                default:
            }
        }
    }

    /**
     * 设置MIUI系统状态栏是否为黑色文字  Set whether the MIUI system status bar is black
     *
     * @param window 窗口，仅可用于Activity  Window, available only for activities
     * @param isDark 是否为黑色文字  Whether the text is black
     */
    private static void setMIUIDark(Window window, boolean isDark) {
        try {
            Class<? extends Window> clazz = window.getClass();
            int darkModeFlag;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(window, isDark ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置Flyme系统状态栏是否为黑色文字
     * Sets whether the Flyme system status bar is black text
     *
     * @param window 窗口  window
     * @param isDark 是否为黑色文字  Whether the text is black
     */
    private static void setFlymeDark(Window window, boolean isDark) {
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (isDark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断颜色是否为深色
     * Determine if the color is dark
     *
     * @param color color
     * @return 是否为深色 Whether it is dark
     */
    public static boolean isDarkColor(@ColorInt int color) {
//        double darkness =
//                1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
//        return darkness >= 0.5;
        return ColorUtils.calculateLuminance(color) < 0.5;
    }
}
