package com.cdv.customvideofx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.meicam.sdk.NvsCaptureVideoFx;
import com.meicam.sdk.NvsLiveWindowExt;
import com.meicam.sdk.NvsStreamingContext;

/**
 * @ProjectName: customvideofx
 * @Package: com.cdv.customvideofx
 * @ClassName: MainArActivity
 * @Description:
 * @Author: WLY
 * @CreateDate: 2025/3/20 17:01
 */
public class MainArActivity extends AppCompatActivity {
    private static final String TAG = "MainArActivity";

    private static final String MAX_FACES_RESPECT_MIN = "Max Faces Respect Min";                    // 最大人脸个数
    private static final String SINGLE_BUFFER_MODE = "Single Buffer Mode";                          // 单缓冲模式
    private static final String AR_SCENE = "AR Scene";                                              // AR场景
    public static final String ADVANCED_BEAUTY_INTENSITY = "Advanced Beauty Intensity";             // 高级美颜强度
    public static final String AR_SCENE_ID_KEY = "Scene Id";
    private boolean mIsSingleBufferMode = true;                                                     // 是否使用单缓冲模式

    // 美摄SDK，核心类实现视频采集、预览、录制
    private NvsStreamingContext mStreamingContext;
    private NvsCaptureVideoFx mArSceneFaceEffect;

    /**
     * 默认前置摄像头
     * Default front-facing camera
     */
    private int mCurrentDeviceIndex = 1;


    @Override
    protected void onDestroy() {
        NvsStreamingContext.close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (mStreamingContext != null)
            mStreamingContext.stop();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCapturePreview();

        String effectId = "";
        // 魔法盾牌  https://qasset.meishesdk.com/material/download/20250311/0/3BF6E56F-68FE-4CC5-8744-3596296F31CA.6.zip
        effectId = "3BF6E56F-68FE-4CC5-8744-3596296F31CA";

        // 爱心满满 https://qasset.meishesdk.com/material/download/20250311/0/140DAD4A-BED7-43BB-8349-50C21BE9194D.4.zip
        effectId = "140DAD4A-BED7-43BB-8349-50C21BE9194D";

        // 柿柿如意 https://qasset.meishesdk.com/material/download/20250311/0/898A6346-8AE3-49DA-A657-03462B944D8A.8.zip
        // 面部彩绘 https://qasset.meishesdk.com/material/download/20250311/0/EDE25C04-776D-4736-A498-1A32001E1279.1.zip

        // 安装效果
//        effectId = installEffect("/storage/emulated/0/Android/data/com.meishe.ms106sdkdemo/files/NvStreamingSdk/Asset/ArScene/898A6346-8AE3-49DA-A657-03462B944D8A.8.zip");
        showProp(effectId);
    }

    /*private String installEffect(String effectPackageFilePath) {
        NvAsset asset = NvAssetManager.sharedInstance().installAssetPackage(
                effectPackageFilePath,
                mAssetType, true);
//        StringBuilder stringBuilder = new StringBuilder();
//        NvsAssetPackageManager assetPackageManagerEffect = mNvsEffectSdkContext.getAssetPackageManager();
//        assetPackageManagerEffect.installAssetPackage(effectPackageFilePath, null,
//                mInstallType, false, stringBuilder);
//        PreferencesManager.get().putString(stringBuilder.toString(), effectPackageFilePath);
        return asset.uuid;
    }*/

    private void showProp(String sceneId) {
        mArSceneFaceEffect.setStringVal(AR_SCENE_ID_KEY, sceneId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //美摄SDK初始化
        //Init meicam sdk
        mStreamingContext = NvsStreamingContext.init(this, null);

        setContentView(R.layout.activity_main_ar);

        NvsLiveWindowExt liveWindowExt = (NvsLiveWindowExt) findViewById(R.id.liveWindowext);
        // 连接预览窗口和美摄SDK
        mStreamingContext.connectCapturePreviewWithLiveWindowExt(liveWindowExt);

        // 切换前后置摄像头
        findViewById(R.id.btn_switch_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentDeviceIndex == 0) {
                    mCurrentDeviceIndex = 1;
                } else {
                    mCurrentDeviceIndex = 0;
                }
                doStartCapturePreview();
            }
        });

        initBeautyFx();
    }

    private void initBeautyFx() {
        mArSceneFaceEffect = getCaptureVideoFx(AR_SCENE);
        if (mArSceneFaceEffect == null) {
            mArSceneFaceEffect = mStreamingContext.appendBuiltinCaptureVideoFx(AR_SCENE);
        }

        if (mArSceneFaceEffect != null) {
            // 支持的人脸个数，是否需要使用最小的设置 The number of faces supported, whether you need to use a minimum setting
            mArSceneFaceEffect.setBooleanVal(MAX_FACES_RESPECT_MIN, true);
            //这个返回true的情况下 必须使用双buffer 不受设置影响 If this returns true, double buffers must be used regardless of the setting
            if (mStreamingContext.isAndroidCameraPreferDualBufferAR()) {
                mArSceneFaceEffect.setBooleanVal(SINGLE_BUFFER_MODE, false);
            } else {
                // 是否设置单buffer模式 Whether to set the single buffer mode
                mArSceneFaceEffect.setBooleanVal(SINGLE_BUFFER_MODE, mIsSingleBufferMode);
            }


            mArSceneFaceEffect.setFloatVal(ADVANCED_BEAUTY_INTENSITY, 0);
        }

    }

    /**
     * 获取拍摄视频特效
     * Get capture video fx
     *
     * @param buildInName the build name
     */
    private NvsCaptureVideoFx getCaptureVideoFx(String buildInName) {
        for (int i = 0; i < mStreamingContext.getCaptureVideoFxCount(); i++) {
            NvsCaptureVideoFx videoFx = mStreamingContext.getCaptureVideoFxByIndex(i);
            if (videoFx != null && TextUtils.equals(videoFx.getBuiltinCaptureVideoFxName(), buildInName)) {
                return videoFx;
            }
        }
        return null;
    }

    /**
     * 开始预览
     * Start capture preview
     */
    private void startCapturePreview() {
        if (mStreamingContext == null)
            return;

        if (mStreamingContext.getStreamingEngineState() == NvsStreamingContext.STREAMING_ENGINE_STATE_CAPTUREPREVIEW)
            return;

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
                return;
            }
        }

        doStartCapturePreview();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    doStartCapturePreview();

                break;
            }
        }
    }

    private void doStartCapturePreview() {
        mStreamingContext.startCapturePreview(mCurrentDeviceIndex, NvsStreamingContext.VIDEO_CAPTURE_RESOLUTION_GRADE_HIGH, 0, null);

//        mStreamingContext.startCapturePreview(mCurrentDeviceIndex, NvsStreamingContext.VIDEO_CAPTURE_RESOLUTION_GRADE_HIGH,
//                NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_DONT_USE_SYSTEM_RECORDER |
//                        NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_CAPTURE_BUDDY_HOST_VIDEO_FRAME |
//                        NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_ENABLE_TAKE_PICTURE |
//                        NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_STRICT_PREVIEW_VIDEO_SIZE, null);
    }

}
