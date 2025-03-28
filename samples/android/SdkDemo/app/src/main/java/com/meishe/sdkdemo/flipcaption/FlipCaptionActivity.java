package com.meishe.sdkdemo.flipcaption;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jokecaptionlib.NvBuildJokeDesc;
import com.meicam.sdk.NvsColor;
import com.meicam.sdk.NvsStreamingContext;
import com.meicam.sdk.NvsTimeline;
import com.meicam.sdk.NvsTimelineVideoFx;
import com.meicam.sdk.NvsVideoClip;
import com.meicam.sdk.NvsVideoResolution;
import com.meicam.sdk.NvsVideoTrack;
import com.meishe.base.model.BaseMvpActivity;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.edit.Caption.CaptionFontRecyclerAdaper;
import com.meishe.sdkdemo.edit.Caption.presenter.CaptionPresenter;
import com.meishe.sdkdemo.edit.Caption.view.CaptionView;
import com.meishe.sdkdemo.edit.CompileVideoFragment;
import com.meishe.sdkdemo.edit.VideoFragment;
import com.meishe.sdkdemo.edit.adapter.SpaceItemDecoration;
import com.meishe.sdkdemo.edit.data.AssetItem;
import com.meishe.sdkdemo.edit.data.BackupData;
import com.meishe.sdkdemo.edit.interfaces.OnTitleBarClickListener;
import com.meishe.sdkdemo.edit.view.CustomTitleBar;
import com.meishe.sdkdemo.interfaces.TipsButtonClickListener;
import com.meishe.sdkdemo.utils.AppManager;
import com.meishe.sdkdemo.utils.FileUtils;
import com.meishe.sdkdemo.utils.Logger;
import com.meishe.sdkdemo.utils.ScreenUtils;
import com.meishe.sdkdemo.utils.TimelineUtil;
import com.meishe.sdkdemo.utils.Util;
import com.meishe.sdkdemo.utils.asset.NvAsset;
import com.meishe.sdkdemo.utils.asset.NvAssetManager;
import com.meishe.sdkdemo.utils.dataInfo.ClipInfo;
import com.meishe.sdkdemo.utils.dataInfo.TimelineData;
import com.meishe.sdkdemo.utils.permission.PermissionsActivity;
import com.meishe.sdkdemo.utils.permission.PermissionsChecker;
import com.meishe.utils.ColorUtil;
import com.meishe.utils.ToastUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.meishe.sdkdemo.utils.Constants.ASSET_DOWNLOAD_FAILED;
import static com.meishe.sdkdemo.utils.Constants.ASSET_DOWNLOAD_INPROGRESS;
import static com.meishe.sdkdemo.utils.Constants.ASSET_DOWNLOAD_START_TIMER;
import static com.meishe.sdkdemo.utils.Constants.ASSET_DOWNLOAD_SUCCESS;
import static com.meishe.sdkdemo.utils.Constants.ASSET_LIST_REQUEST_FAILED;
import static com.meishe.sdkdemo.utils.Constants.ASSET_LIST_REQUEST_SUCCESS;
import static com.meishe.sdkdemo.utils.Constants.CaptionColors;

/**
 * All rights reserved,Designed by www.meishesdk.com
 *
 * @Author : yyj
 * @CreateDate : 2019/6/28.
 * @Description :翻转字幕Activity
 * @Description :FlipCaptionActivity
 * @Copyright :www.meishesdk.com Inc.All rights reserved.
 */
public class FlipCaptionActivity extends BaseMvpActivity<CaptionPresenter> implements View.OnClickListener, CaptionView {
    private static final String TAG = "FlipCaptionActivity";
    private static final int REQUEST_CODE_FLIPCAPTION_EDIT = 1101;//flipCaption


    private RelativeLayout mCompilePage;
    private VideoFragment mVideoFragment;
    private CompileVideoFragment mCompileVideoFragment;
    private CustomTitleBar mTitleBar;
    private RelativeLayout mBottomLayout;

    private LinearLayout mCaptionLayout;
    private LinearLayout mColorLayout;
    private LinearLayout mFontLayout;

    private RelativeLayout mFlipCaptionColorRect;
    private FlipCaptionColorList mFlipCaptionColorList;
    private ImageView mCaptionColorFinish;
    private RelativeLayout mFlipCaptionFontRect;
    private RecyclerView mCaptionFontRecycleList;
    private ImageView mCaptionFontFinish;

    private NvsStreamingContext mStreamingContext;
    private NvsTimeline mTimeline;

    private ArrayList<String> mCaptionColorList = new ArrayList<>();
    private ArrayList<AssetItem> mCaptionFontList = new ArrayList<>();
    private ArrayList<AssetItem> mServerCaptionFontList = new ArrayList<>();
    private ArrayList<FlipCaptionDataInfo> mFlipDataInfoList = new ArrayList<>();
    private String mCaptionFontPath;

    private NvAssetManager mAssetManager;
    private int mFontType = NvAsset.ASSET_FONT;

    private CaptionFontRecyclerAdaper mCaptionFontAdapter;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private int mFontCurClickPos = 0;

    NvBuildJokeDesc mBuildJokeDesc;
    List<String> permissionList;
    private PermissionsChecker mPermissionsChecker;
    static final int REQUEST_CODE = 110;

    private FlipCaptionActivity.CaptionStyleHandler m_handler = new FlipCaptionActivity.CaptionStyleHandler(this);

    @Override
    public void getFontsBack(ArrayList<AssetItem> data) {
        mServerCaptionFontList.clear();
        mServerCaptionFontList.addAll(data);
        updateFontList();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void onDownloadProgress(int position) {
        updateFontItem();
    }

    @Override
    public void onDownloadFinish(int position, NvAsset assetInfo) {
        applyLastSelFont(assetInfo.uuid);
    }

    @Override
    public void onDownloadError(int position) {
    }

    static class CaptionStyleHandler extends Handler {
        WeakReference<FlipCaptionActivity> mWeakReference;

        public CaptionStyleHandler(FlipCaptionActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final FlipCaptionActivity activity = mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case ASSET_LIST_REQUEST_SUCCESS:
                        activity.updateFontList();
                        break;
                    case ASSET_LIST_REQUEST_FAILED:
                        activity.fontListRequestFail();
                        break;
                    case ASSET_DOWNLOAD_START_TIMER:
                        activity.startProgressTimer();
                        String progressUuid = (String) msg.obj;
                        activity.fontItemCopy(progressUuid);
                        break;
                    case ASSET_DOWNLOAD_SUCCESS:
                        String successUuid = (String) msg.obj;
                        activity.fontItemCopy(successUuid);
                        activity.applyLastSelFont(successUuid);
                        activity.updateFontItem();
                        break;
                    case ASSET_DOWNLOAD_FAILED:
                        activity.fontDownloadFail();
                        activity.updateFontItem();
                        break;
                    case ASSET_DOWNLOAD_INPROGRESS:
                        activity.updateFontDownloadProgress();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected int bindLayout() {
        AppManager.getInstance().addActivity(this);
        mStreamingContext = NvsStreamingContext.getInstance();
        permissionList = Util.getWriteReadPermission();
        return R.layout.activity_flip_caption;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    protected void initView() {
        mTitleBar = (CustomTitleBar) findViewById(R.id.title_bar);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mCompilePage = (RelativeLayout) findViewById(R.id.compilePage);
        mCaptionLayout = (LinearLayout) findViewById(R.id.captionLayout);
        mColorLayout = (LinearLayout) findViewById(R.id.colorLayout);
        mFontLayout = (LinearLayout) findViewById(R.id.fontLayout);

        mFlipCaptionColorRect = (RelativeLayout) findViewById(R.id.flipCaptionColorRect);
        mFlipCaptionColorList = (FlipCaptionColorList) findViewById(R.id.flipCaptionColorList);
        mCaptionColorFinish = (ImageView) findViewById(R.id.captionColorFinish);
        mFlipCaptionFontRect = (RelativeLayout) findViewById(R.id.flipCaptionFontRect);
        mCaptionFontRecycleList = (RecyclerView) findViewById(R.id.captionFontRecycleList);
        mCaptionFontFinish = (ImageView) findViewById(R.id.captionFontFinish);
        initTitle();
    }

    protected void initTitle() {
        mTitleBar.setTextCenter(R.string.flipCaption);
        mTitleBar.setTextRight(R.string.compile);
        mTitleBar.setTextRightVisible(View.VISIBLE);
    }

    @Override
    protected void requestData() {
        mTimeline = createTimeline();
        if (mTimeline == null) {
            return;
        }

        initCompileVideoFragment();
        readFlipCaptionLrc();
        initFlipCaptionData();
        initVideoFragment();
        loadVideoClipFailTips();
        initListener();
        mPresenter.getFonts();
    }

    protected void initListener() {
        mTitleBar.setOnTitleBarClickListener(new OnTitleBarClickListener() {
            @Override
            public void OnBackImageClick() {
                removeTimeline();
            }

            @Override
            public void OnCenterTextClick() {

            }

            @Override
            public void OnRightTextClick() {
                mCompilePage.setVisibility(View.VISIBLE);
                mCompileVideoFragment.compileVideo();
            }
        });

        mCaptionLayout.setOnClickListener(this);
        mColorLayout.setOnClickListener(this);
        mFontLayout.setOnClickListener(this);
        mCaptionColorFinish.setOnClickListener(this);
        mCaptionFontFinish.setOnClickListener(this);

        mFlipCaptionColorRect.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        mFlipCaptionFontRect.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        mCompilePage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        if (mCompileVideoFragment != null) {
            mCompileVideoFragment.setCompileVideoListener(new CompileVideoFragment.OnCompileVideoListener() {
                @Override
                public void compileProgress(NvsTimeline timeline, int progress) {

                }

                @Override
                public void compileFinished(NvsTimeline timeline) {
                    mCompilePage.setVisibility(View.GONE);
                }

                @Override
                public void compileFailed(NvsTimeline timeline) {
                    mCompilePage.setVisibility(View.GONE);
                }

                @Override
                public void compileCompleted(NvsTimeline nvsTimeline, boolean isCanceled) {
                    mCompilePage.setVisibility(View.GONE);
                }

                @Override
                public void compileVideoCancel() {
                    mCompilePage.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.captionLayout) {
            resetFlipDataInfo();
            mCaptionLayout.setEnabled(false);
            BackupData.instance().setFlipDataInfoList(mFlipDataInfoList);
            AppManager.getInstance().jumpActivityForResult(FlipCaptionActivity.this, FlipCaptionEditActivity.class, null, REQUEST_CODE_FLIPCAPTION_EDIT);
        } else if (id == R.id.colorLayout) {
            mFlipCaptionColorRect.setVisibility(View.VISIBLE);
        } else if (id == R.id.fontLayout) {
            mFlipCaptionFontRect.setVisibility(View.VISIBLE);
        } else if (id == R.id.captionColorFinish) {
            mFlipCaptionColorList.setSelectedPos(-1);
            mFlipCaptionColorList.notifyDataSetChanged();
            mFlipCaptionColorRect.setVisibility(View.GONE);
        } else if (id == R.id.captionFontFinish) {
            mFlipCaptionFontRect.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasAllPermission()) {
            checkPermissions();
        }
        mCaptionLayout.setEnabled(true);
    }

    public boolean hasAllPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AppManager.getInstance().finishActivity();
        removeTimeline();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case REQUEST_CODE_FLIPCAPTION_EDIT:
                mFlipDataInfoList = BackupData.instance().getFlipDataInfoList();
                applyJokeCaption();
                break;
        }
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
        /*
         * 存储素材数据线程
         * Store material data thread
         * */
        new Thread(new Runnable() {
            @Override
            public void run() {
                NvAssetManager.sharedInstance().setAssetInfoToSharedPreferences(mFontType);
            }
        }).start();
    }

    private void loadVideoClipFailTips() {
        /*
         * 导入视频无效，提示
         * The imported video is invalid
         * */
        if (mTimeline == null || (mTimeline != null && mTimeline.getDuration() <= 0)) {
            String[] versionName = getResources().getStringArray(R.array.clip_load_failed_tips);
            Util.showDialog(FlipCaptionActivity.this, versionName[0], versionName[1], new TipsButtonClickListener() {
                @Override
                public void onTipsButtoClick(View view) {
                    removeTimeline();
                    AppManager.getInstance().finishActivity();
                }
            });
        }
    }

    private NvsTimeline createTimeline() {
        NvsVideoResolution videoResolution = new NvsVideoResolution();
        videoResolution.imageWidth = 720;
        videoResolution.imageHeight = 1280;
        NvsTimeline timeline = TimelineUtil.newTimeline(videoResolution);
        if (timeline == null) {
            return null;
        }
        NvsVideoTrack videoTrack = timeline.appendVideoTrack();
        if (videoTrack == null) {
            return null;
        }
        ArrayList<ClipInfo> videoClipArray = TimelineData.instance().getClipInfoData();
        for (int i = 0; i < videoClipArray.size(); i++) {
            ClipInfo clipInfo = videoClipArray.get(i);
            NvsVideoClip videoClip = videoTrack.appendClip(clipInfo.getFilePath());
            if (videoClip == null) {
                Logger.e(TAG, "failed to add clip -->" + clipInfo.getFilePath());
            }
        }
        return timeline;
    }

    private void resetFlipDataInfo() {
        for (int index = 0; index < mFlipDataInfoList.size(); ++index) {
            mFlipDataInfoList.get(index).setSelectItem(false);
        }
    }

    private void removeTimeline() {
        TimelineUtil.removeTimeline(mTimeline);
        mTimeline = null;
    }

    private void initCompileVideoFragment() {
        mCompileVideoFragment = new CompileVideoFragment();
        mCompileVideoFragment.setTimeline(mTimeline);
        getFragmentManager().beginTransaction()
                .add(R.id.compilePage, mCompileVideoFragment)
                .commit();
        getFragmentManager().beginTransaction().show(mCompileVideoFragment);
    }

    private void initVideoFragment() {
        mVideoFragment = new VideoFragment();
        mVideoFragment.setFragmentLoadFinisedListener(new VideoFragment.OnFragmentLoadFinisedListener() {
            @Override
            public void onLoadFinished() {
                applyJokeCaption();
            }
        });
        mVideoFragment.setTimeline(mTimeline);
        Bundle bundle = new Bundle();
        bundle.putInt("titleHeight", mTitleBar.getLayoutParams().height);
        bundle.putInt("bottomHeight", mBottomLayout.getLayoutParams().height);
        bundle.putBoolean("playBarVisible", true);
        bundle.putInt("ratio", TimelineData.instance().getMakeRatio());
        mVideoFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .add(R.id.spaceLayout, mVideoFragment)
                .commit();
        getFragmentManager().beginTransaction().show(mVideoFragment);
    }

    /**
     * 读本地预存的字幕文件
     * Read local pre-stored subtitle files
     */
    private void readFlipCaptionLrc() {
        String bundleFlipCaptionLrcPath = "flipcaption/Jam.lrc";
        parseFlipCaptionLrc(bundleFlipCaptionLrcPath, true);
    }

    private void parseFlipCaptionLrc(String flipCaptionLrcPath, boolean isBundle) {
        try {
            InputStream inputStream = isBundle ? this.getAssets().open(flipCaptionLrcPath) :
                    new FileInputStream(flipCaptionLrcPath);
            if (inputStream == null) {
                return;
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String infoStrLine;
            while ((infoStrLine = bufferedReader.readLine()) != null) {
                String newCaptionText = infoStrLine.substring(infoStrLine.indexOf("]") + 1);
                if (TextUtils.isEmpty(newCaptionText)) {
                    continue;
                }
                FlipCaptionDataInfo flipCaptionDataInfo = new FlipCaptionDataInfo();
                flipCaptionDataInfo.setCaptionText(infoStrLine);
                flipCaptionDataInfo.setSelectItem(false);
                mFlipDataInfoList.add(flipCaptionDataInfo);
            }
            bufferedReader.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initFlipCaptionData() {
        mAssetManager = NvAssetManager.sharedInstance();
        mAssetManager.searchLocalAssets(mFontType);//查找字体文件 Find font file
        mAssetManager.searchReservedAssets(mFontType, "font");
        assetDataRequest();

        initCaptionColorList();
        initCaptionFontList();
        initCaptionFontRecyclerView();
    }

    private void applyJokeCaption() {
        removeAllFx();
        ArrayList<String> dataList = new ArrayList<>();
        for (int index = 0; index < mFlipDataInfoList.size(); ++index) {
            dataList.add(mFlipDataInfoList.get(index).getCaptionText());
        }
        if (mBuildJokeDesc == null) {
            mBuildJokeDesc = new NvBuildJokeDesc(this, dataList, mVideoFragment.getLiveWindow(), mTimeline);
        }

        mBuildJokeDesc.updateTextList(dataList);

        /*
         * 参数1为行号，参数2为颜色RGBA。格式:255,255,255,255
         * Parameter 1 is the line number and parameter 2 is the color RGBA. Format: 255,255,255,255
         */
        StringBuilder colorResult = new StringBuilder();
        for (int index = 0; index < mFlipDataInfoList.size(); ++index) {
            String captionColor = mFlipDataInfoList.get(index).getCaptionColor();
            if (!TextUtils.isEmpty(captionColor)) {
                NvsColor color = ColorUtil.colorStringtoNvsColor(captionColor);
                int colorArray[] = ColorUtil.nvsColortoRgba(color);
                colorResult.setLength(0);
                colorResult.append(colorArray[1]);
                colorResult.append(",");
                colorResult.append(colorArray[2]);
                colorResult.append(",");
                colorResult.append(colorArray[3]);
                colorResult.append(",");
                colorResult.append(255);
                mBuildJokeDesc.setFontColorSingleLine(index, colorResult.toString());
            }
        }

        /*
         * 应用字体，参数为字体文件的路径
         * Apply the font, the parameter is the path of the font file
         */
        String fontFamily = mStreamingContext.registerFontByFilePath(mCaptionFontPath);
        if (TextUtils.isEmpty(fontFamily)) {
            fontFamily = "none";
        }
        mBuildJokeDesc.setFont(fontFamily);
        String jokeFxDesc = mBuildJokeDesc.buildJokeFxDesc();
        NvsTimelineVideoFx timelineVideoFx = mTimeline.addBuiltinTimelineVideoFx(0, mTimeline.getDuration(), "Storyboard");
        String localXmlPath = FileUtils.getExternalFilePath(getApplicationContext(), "local/flipXml/");
        if (!TextUtils.isEmpty(localXmlPath)) {
            String xmlContent = "";
            File file = new File(localXmlPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            if (file.isDirectory() && file.list().length > 0) {
                xmlContent = FileUtils.readFile(getApplicationContext(), localXmlPath + File.separator + file.list()[0]);
            }
            if (!TextUtils.isEmpty(xmlContent)) {
                jokeFxDesc = xmlContent;
            }
        }
        if (timelineVideoFx != null) {
            timelineVideoFx.setStringVal("Description String", jokeFxDesc);
            timelineVideoFx.setBooleanVal("Is Caption", true);
        }
        long timeStamp = mStreamingContext.getTimelineCurrentPosition(mTimeline);
        seekTimeline(timeStamp);
        mVideoFragment.updateCurPlayTime(timeStamp);
    }

    private void removeAllFx() {
        if (mTimeline != null) {
            NvsTimelineVideoFx videoFx = mTimeline.getFirstTimelineVideoFx();
            while (videoFx != null) {
                videoFx = mTimeline.removeTimelineVideoFx(videoFx);
            }
        }
    }

    private void initCaptionFontRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mCaptionFontRecycleList.setLayoutManager(layoutManager);
        mCaptionFontAdapter = new CaptionFontRecyclerAdaper(this);
        mCaptionFontAdapter.setAssetInfoList(mCaptionFontList);
        mCaptionFontRecycleList.setAdapter(mCaptionFontAdapter);
        mCaptionFontRecycleList.addItemDecoration(new SpaceItemDecoration(0, ScreenUtils.dip2px(this, 8)));
        mCaptionFontAdapter.setOnItemClickListener(new CaptionFontRecyclerAdaper.OnFontItemClickListener() {
            @Override
            public void onItemDownload(View view, int position) {
                int count = mCaptionFontList.size();
                if (position <= 0 || position >= count) {
                    return;
                }
                if (mFontCurClickPos == position) {
                    //重复点击，不作处理防止素材多次下载
                    //Double-click prevention
                    return;
                }
                mFontCurClickPos = position;
//                mAssetManager.downloadAsset(mFontType, mCaptionFontList.get(position).getAsset().uuid);
                mPresenter.downloadAsset(mCaptionFontList.get(position).getAsset(), mFontCurClickPos);
            }

            @Override
            public void onItemClick(View view, int pos) {
                mCaptionFontPath = mCaptionFontList.get(pos).getAsset().localDirPath;
                applyJokeCaption();
            }
        });
    }

    private void initCaptionFontList() {
        mCaptionFontList.clear();
        AssetItem noneFontInfo = new AssetItem();
        noneFontInfo.setAsset(new NvAsset());
        noneFontInfo.setImageRes(R.mipmap.captionstyle_no);
        noneFontInfo.setAssetMode(AssetItem.ASSET_NONE);
        mCaptionFontList.add(noneFontInfo);
        mCaptionFontList.addAll(mServerCaptionFontList);
//        ArrayList<NvAsset> usableAsset = getFontAssetsDataList();
//        for (int index = 0; index < usableAsset.size(); ++index) {
//            AssetItem localFontInfo = new AssetItem();
//            localFontInfo.setAsset(usableAsset.get(index));
//            localFontInfo.setAssetMode(AssetItem.ASSET_LOCAL);
//            mCaptionFontList.add(localFontInfo);
//        }
//        List<NvAsset> usableAssets = mAssetManager.getUsableAssets(mFontType, NvAsset.AspectRatio_All, 0);
//        if (usableAssets != null) {
//            for (NvAsset asset : usableAssets) {
//                NvAsset ass = new NvAsset();
//                ass.copyAsset(asset);
//                ass.coverUrl = "file:///android_asset/font/" + ass.uuid + ".png";
//                AssetItem localFontInfo = new AssetItem();
//                //todo 添加本地asset文件
//                ass.localDirPath = ass.bundledLocalDirPath;
//                localFontInfo.setAsset(ass);
//                localFontInfo.setAssetMode(AssetItem.ASSET_LOCAL);
//                mCaptionFontList.add(localFontInfo);
//            }
//        }

    }

    /**
     * 获取字体数据列表
     * Get font data list
     */
    private ArrayList<NvAsset> getFontAssetsDataList() {

        return mAssetManager.getRemoteAssetsWithPage(mFontType, NvAsset.AspectRatio_All, 0, 1, 10);
    }

    private void assetDataRequest() {
        mAssetManager.downloadRemoteAssetsInfo(mFontType, 1, NvAsset.AspectRatio_All, 0, 1, 10);
        mAssetManager.setManagerlistener(new NvAssetManager.NvAssetManagerListener() {
            @Override
            public void onRemoteAssetsChanged(boolean hasNext) {
                m_handler.sendEmptyMessage(ASSET_LIST_REQUEST_SUCCESS);
            }

            @Override
            public void onRemoteAssetsChanged(List<NvAsset> assetDataList, boolean hasNext) {

            }

            @Override
            public void onGetRemoteAssetsFailed() {
                m_handler.sendEmptyMessage(ASSET_LIST_REQUEST_FAILED);
            }

            @Override
            public void onDownloadAssetProgress(String uuid, int progress) {
                sendHandleMsg(uuid, ASSET_DOWNLOAD_START_TIMER);
            }

            @Override
            public void onDonwloadAssetFailed(String uuid) {
                sendHandleMsg(uuid, ASSET_DOWNLOAD_FAILED);
            }

            @Override
            public void onDonwloadAssetSuccess(String uuid) {
                sendHandleMsg(uuid, ASSET_DOWNLOAD_SUCCESS);
            }

            @Override
            public void onFinishAssetPackageInstallation(String uuid) {

            }

            @Override
            public void onFinishAssetPackageUpgrading(String uuid) {

            }
        });
    }

    private void sendHandleMsg(String uuid, int what) {
        Message sendMsg = m_handler.obtainMessage();
        if (sendMsg == null)
            sendMsg = new Message();
        sendMsg.what = what;
        sendMsg.obj = uuid;
        m_handler.sendMessage(sendMsg);
    }


    private void updateFontList() {
        initCaptionFontList();
//        mCaptionFontAdapter.setAssetInfoList(mCaptionFontList);
        mCaptionFontAdapter.notifyDataSetChanged();
    }

    private void updateFontDownloadProgress() {
        boolean isDownloadState = false;
        for (int i = 0; i < mCaptionFontList.size(); ++i) {
            NvAsset asset = mCaptionFontList.get(i).getAsset();
            if (asset == null) {
                continue;
            }
            if (asset.downloadStatus == NvAsset.DownloadStatusInProgress
                    || asset.downloadStatus == NvAsset.DownloadStatusPending) {
                isDownloadState = true;
            }
        }
        //是下载状态，通知更新数据
        //Is download status, notification update data
        if (isDownloadState) {
            updateFontItem();
        }
    }

    private void applyLastSelFont(String uuid) {
        String curClickUuid = mCaptionFontList.get(mFontCurClickPos).getAsset().uuid;
        if (!TextUtils.isEmpty(curClickUuid) && curClickUuid.equals(uuid)) {
            mCaptionFontPath = mCaptionFontList.get(mFontCurClickPos).getAsset().localDirPath;
            mCaptionFontAdapter.setSelectedPos(mFontCurClickPos);
            if (mTimeline != null) {
                applyJokeCaption();
            }
        }
    }

    private void updateFontItem() {
        mCaptionFontAdapter.notifyDataSetChanged();
    }

    private void fontItemCopy(String uuid) {
        NvAsset curAsset = null;
        ArrayList<NvAsset> usableAsset = getFontAssetsDataList();
        for (int index = 0; index < usableAsset.size(); ++index) {
            curAsset = usableAsset.get(index);
            if (curAsset == null) {
                continue;
            }
            if (!TextUtils.isEmpty(curAsset.uuid) && uuid.equals(curAsset.uuid)) {
                break;
            }
        }

        for (int i = 0; i < mCaptionFontList.size(); ++i) {
            NvAsset asset = mCaptionFontList.get(i).getAsset();
            if (asset == null) {
                continue;
            }
            if (curAsset != null && !TextUtils.isEmpty(asset.uuid) && asset.uuid.equals(uuid)) {
                mCaptionFontList.get(i).getAsset().copyAsset(curAsset);
            }
        }
    }

    private void fontListRequestFail() {
        ToastUtil.showToast(this, this.getResources().getString(R.string.check_network));
    }

    private void fontDownloadFail() {
        ToastUtil.showToast(this, this.getResources().getString(R.string.download_failed));
    }

    /**
     * 初始化字幕字体颜色列表
     * Initialize the subtitle font color list
     */
    private void initCaptionColorList() {
        for (int index = 0; index < CaptionColors.length; ++index) {
            mCaptionColorList.add(CaptionColors[index]);
        }
        mFlipCaptionColorList.setCaptionColorInfolist(mCaptionColorList);
        mFlipCaptionColorList.notifyDataSetChanged();
        mFlipCaptionColorList.setCaptionColorListener(new FlipCaptionColorList.OnFlipCaptionColorListener() {
            @Override
            public void onCaptionColor(int pos) {
                for (int index = 0; index < mFlipDataInfoList.size(); ++index) {
                    mFlipDataInfoList.get(index).setCaptionColor(mCaptionColorList.get(pos));
                }
                applyJokeCaption();
            }
        });
    }


    private void startProgressTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    m_handler.sendEmptyMessage(ASSET_DOWNLOAD_INPROGRESS);
                }
            };
            mTimer.schedule(mTimerTask, 0, 50);
        }
    }

    private void stopProgressTimer() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void seekTimeline(long stamp) {
        mVideoFragment.seekTimeline(stamp, 0);
    }
}
