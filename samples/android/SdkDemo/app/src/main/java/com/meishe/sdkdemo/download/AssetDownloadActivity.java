package com.meishe.sdkdemo.download;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.base.BaseActivity;
import com.meishe.sdkdemo.edit.interfaces.OnTitleBarClickListener;
import com.meishe.sdkdemo.edit.view.CustomTitleBar;
import com.meishe.sdkdemo.utils.AppManager;
import com.meishe.sdkdemo.utils.asset.NvAsset;
import com.meishe.sdkdemo.utils.dataInfo.TimelineData;

public class AssetDownloadActivity extends BaseActivity {
    public static String SHOW_PROP = "showProp";
    private CustomTitleBar mTitleBar;
    AssetListFragment mAssetListFragment;
    /*
     * 默认设置为主题title
     * The default setting is the theme title
     * */
    private int mTitleResId = R.string.moreTheme;
    /*
     * 默认设置为主题素材类型
     * The default setting is the theme material type
     * */
    private int mAssetType = NvAsset.ASSET_THEME;
    private String mComeFrom;
    private int ratioFlag;
    private int mCategoryId = 0;

    @Override
    protected int initRootView() {
        return R.layout.activity_asset_download;
    }

    @Override
    protected void initViews() {
        mTitleBar = (CustomTitleBar) findViewById(R.id.title_bar);
        mTitleBar.setMainLayoutResource(R.color.white);
        mTitleBar.setTextCenterColor(R.color.black);
        mTitleBar.setBackImageIcon(R.mipmap.back_icon);
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mTitleResId = bundle.getInt("titleResId", R.string.moreTheme);
                mAssetType = bundle.getInt("assetType", NvAsset.ASSET_THEME);
                mCategoryId = bundle.getInt("categoryId",0);
                mComeFrom = bundle.getString("from", "");
                ratioFlag = bundle.getInt("ratioFlag",1);
            }
        }
    }

    @Override
    protected void initTitle() {
        mTitleBar.setTextCenter(mTitleResId);
    }

    @Override
    protected void initData() {
        initAssetFragment();
    }

    @Override
    protected void initListener() {
        mTitleBar.setOnTitleBarClickListener(new OnTitleBarClickListener() {
            @Override
            public void OnBackImageClick() {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
            }

            @Override
            public void OnCenterTextClick() {

            }

            @Override
            public void OnRightTextClick() {

            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        AppManager.getInstance().finishActivity();
        super.onBackPressed();
    }

    private void initAssetFragment() {
        Bundle bundle = new Bundle();
        bundle.putInt("ratio", TimelineData.instance().getMakeRatio());
        bundle.putInt("assetType", mAssetType);
        bundle.putInt("categoryId", mCategoryId);
        bundle.putString("from", mComeFrom);
        bundle.putInt("ratioFlag", ratioFlag);
        mAssetListFragment = new AssetListFragment();
        mAssetListFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .add(R.id.spaceLayout, mAssetListFragment)
                .commit();
        getFragmentManager().beginTransaction().show(mAssetListFragment);
    }
}
