package com.meishe.sdkdemo.utils;

import android.content.Context;
import android.text.TextUtils;

import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.edit.data.FilterItem;
import com.meishe.sdkdemo.edit.data.ParseJsonFile;
import com.meishe.sdkdemo.edit.data.Props;
import com.meishe.sdkdemo.edit.data.PropsList;
import com.meishe.sdkdemo.utils.asset.NvAsset;
import com.meishe.sdkdemo.utils.dataInfo.TimelineData;
import com.meishe.sdkdemo.utils.dataInfo.VideoClipFxInfo;
import com.meishe.utils.ColorUtil;
import com.meishe.utils.PathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by admin on 2018/11/15.
 */

public class AssetFxUtil {

    /**
     * 获取道具列表（新）
     * 解析assetPath对应的json文件，组装所给的NvAsset列表，
     * Get Item List (new)
     * Parse the json file corresponding to assetPath, assemble the given NvAsset list,
     */
    public static List<Props> getPropsList(Context context, List<NvAsset> assetsLocalList, String assetPath) {
        List<Props> localList = null;
        if (!TextUtils.isEmpty(assetPath)) {
            PropsList propsList = ParseJsonFile.fromJson(ParseJsonFile.readAssetJsonFile(context, assetPath), PropsList.class);
            if (propsList != null) {
                localList = propsList.getPropsList();
            }

        }
        List<Props> propsList = new ArrayList<>();
        if (assetsLocalList != null) {
            boolean localHas = false;
            for (NvAsset asset : assetsLocalList) {
                if (localList != null) {
                    for (Props props : localList) {
                        if (props.getUuid().equals(asset.uuid)) {
                            localHas = true;
                            break;
                        }
                    }
                }
                if (!localHas) {
                    Props props = new Props(asset);
                    propsList.add(props);
                }
            }
        }
        if (localList != null) {
            propsList.addAll(localList);
        }
        return propsList;
    }

    /*
     * 获取人脸道具数据
     * Get face prop data
     * */
    public static ArrayList<FilterItem> getFaceUDataList(ArrayList<NvAsset> faceULocalDataList, ArrayList<NvAsset> faceUBundleDataList) {
        ArrayList<FilterItem> faceUPropDataList = new ArrayList<>();
        FilterItem filterItem = new FilterItem();
        //filterItem.setFilterName("");
        filterItem.setPackageId("");
        filterItem.setImageId(R.mipmap.no);
        faceUPropDataList.add(filterItem);

        if (faceULocalDataList != null) {
            for (NvAsset asset : faceULocalDataList) {
                FilterItem newFilterItem = new FilterItem();
                if (asset.isReserved()) {
                    String coverPath = "";
                    //初始化本地到具 Initializes the local to tool
                    coverPath = "arface/" + asset.uuid;
                    coverPath = PathUtils.getAssetFileBySuffixPic(coverPath);
                    /*
                     * 加载assets/arface文件夹下的图片
                     * Load images in assets / arface folder
                     * */
                    asset.coverUrl = "file:///android_asset/" + coverPath;
                }
                if (TextUtils.isEmpty(asset.coverUrl)) {
                    continue;
                }
                newFilterItem.setFilterName(asset.name);
                newFilterItem.setFilterMode(FilterItem.FILTERMODE_PACKAGE);
                newFilterItem.setPackageId(asset.uuid);
                newFilterItem.setImageUrl(asset.coverUrl);
                newFilterItem.setCategoryId(asset.categoryId);
                faceUPropDataList.add(newFilterItem);
            }
        }

        if (faceUBundleDataList != null) {
            for (NvAsset asset : faceUBundleDataList) {
                FilterItem newFilterItem = new FilterItem();
                if (asset.isReserved()) {
                    String coverPath = "arface/" + asset.uuid;
                    coverPath = PathUtils.getAssetFileBySuffixPic(coverPath);
                    /*
                     * 加载assets/arface文件夹下的图片
                     * Load images in assets / arface folder
                     * */
                    asset.coverUrl = "file:///android_asset/" + coverPath;
                }
                newFilterItem.setFilterMode(FilterItem.FILTERMODE_BUNDLE);
                // newFilterItem.setFilterName(asset.bundledLocalDirPath);
                newFilterItem.setFilterName(asset.name);
                newFilterItem.setPackageId(asset.uuid);
                newFilterItem.setImageUrl(asset.coverUrl);
                newFilterItem.setCategoryId(asset.categoryId);
                faceUPropDataList.add(newFilterItem);
            }
        }

        return faceUPropDataList;
    }

    /*
     * 获取人脸道具数据选中位置
     * Get face prop data selected position
     * */
    public static int getSelectedFaceUPropPos(ArrayList<FilterItem> faceUDataArrayList, String curFaceArSceneId) {
        if (faceUDataArrayList == null || faceUDataArrayList.size() == 0) {
            return -1;
        }

        if (TextUtils.isEmpty(curFaceArSceneId)) {
            return 0;
        }

        for (int i = 0; i < faceUDataArrayList.size(); i++) {
            FilterItem newFilterItem = faceUDataArrayList.get(i);
            if (newFilterItem == null) {
                continue;
            }
            String faceArSceneId = newFilterItem.getPackageId();
            if (curFaceArSceneId.equals(faceArSceneId)) {
                return i;
            }
        }

        return 0;
    }

    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * 获取滤镜数据
     * Get filter data
     * */
    public static ArrayList<FilterItem> getFilterData(Context context,
                                                      ArrayList<NvAsset> filterAssetList,
                                                      List<FilterItem> builtinVideoFxList,
                                                      boolean isAddCartoon,
                                                      boolean isFitRatio) {
        ArrayList<FilterItem> filterList = new ArrayList<>();
        FilterItem filterItem = new FilterItem();
//        if (isZh(context)) {
//            filterItem.setFilterName("无");
//        } else {
//            filterItem.setFilterName("no");
//        }
        filterItem.setFilterName(context.getResources().getString(R.string.no));
        filterItem.setImageId(R.mipmap.no);
        filterItem.setBackgroundColor(ColorUtil.MAKEUP_DEFAULT_TEXT_BG);
        filterList.add(filterItem);

        if (isAddCartoon) {
            /*
             * 新增漫画特效
             * Added comic effects
             * */
            FilterItem cartoon1 = new FilterItem();
            cartoon1.setIsCartoon(true);
//            if (isZh(context)) {
//                cartoon1.setFilterName("水墨");
//            } else {
//                cartoon1.setFilterName("Ink painting");
//            }
            //cartoon1.setFilterName("水墨");
            cartoon1.setFilterName(context.getResources().getString(R.string.ink_painting));
            cartoon1.setImageId(R.mipmap.sage);
            cartoon1.setFilterMode(FilterItem.FILTERMODE_BUILTIN);
            cartoon1.setStrokenOnly(true);
            cartoon1.setGrayScale(true);
            cartoon1.setIsSpecialFilter(true);
            cartoon1.setBackgroundColor(ColorUtil.FILTER_BG_0);
            filterList.add(cartoon1);

            FilterItem cartoon2 = new FilterItem();
            cartoon2.setIsCartoon(true);
//            if (isZh(context)) {
//                cartoon2.setFilterName("漫画书");
//            } else {
//                cartoon2.setFilterName("The comic book");
//            }
            //cartoon2.setFilterName("漫画书");
            cartoon2.setFilterName(context.getResources().getString(R.string.the_comic_book));
            cartoon2.setImageId(R.mipmap.maid);
            cartoon2.setFilterMode(FilterItem.FILTERMODE_BUILTIN);
            cartoon2.setStrokenOnly(false);
            cartoon2.setGrayScale(false);
            cartoon2.setIsSpecialFilter(true);
            cartoon2.setBackgroundColor(ColorUtil.FILTER_BG_1);
            filterList.add(cartoon2);

            FilterItem cartoon3 = new FilterItem();
            cartoon3.setIsCartoon(true);
//            if (isZh(context)) {
//                cartoon3.setFilterName("单色");
//            } else {
//                cartoon3.setFilterName("Single");
//            }
            cartoon3.setFilterName(context.getResources().getString(R.string.single));
            cartoon3.setImageId(R.mipmap.mace);
            cartoon3.setFilterMode(FilterItem.FILTERMODE_BUILTIN);
            cartoon3.setStrokenOnly(false);
            cartoon3.setGrayScale(true);
            cartoon3.setIsSpecialFilter(true);
            cartoon3.setBackgroundColor(ColorUtil.FILTER_BG_2);
            filterList.add(cartoon3);

            /*
             * 分割条的占位
             * Placeholder for split bar
             * */
            FilterItem splitItem = new FilterItem();
            filterList.add(splitItem);
        }
        /*
         * 暂时先注掉内建滤镜特效
         * Note out the built-in filter effects temporarily
         * */
        int[] resImags = {
                R.mipmap.sagenew,
                R.mipmap.maidnew,
                R.mipmap.macenew,
                R.mipmap.lacenew,
                R.mipmap.mallnew,
                R.mipmap.sapnew,
                R.mipmap.saranew,
                R.mipmap.pinkynew,
                R.mipmap.sweetnew,
                R.mipmap.freshnew
        };
        if (builtinVideoFxList != null) {
            for (int i = 0; i < builtinVideoFxList.size(); i++) {
                FilterItem newFilterItem = builtinVideoFxList.get(i);
                if (i < resImags.length) {
                    newFilterItem.setImageId(resImags[i]);
                }
                newFilterItem.setFilterMode(FilterItem.FILTERMODE_BUILTIN);
                newFilterItem.setBackgroundColor(ColorUtil.getFilterRandomBgColor());
                filterList.add(newFilterItem);
            }
        }

        if (isZh(context)) {
            String bundlePath = "filter/info_Zh.txt";
            Util.getBundleFilterInfo(context, filterAssetList, bundlePath);
        } else {
            String bundlePath = "filter/info.txt";
            Util.getBundleFilterInfo(context, filterAssetList, bundlePath);
        }


        int ratio = TimelineData.instance().getMakeRatio();
        int cartoonSize = filterList.size();
        for (NvAsset asset : filterAssetList) {
            if (asset.ratioFlag != 1) {
                if (isFitRatio && (ratio & asset.aspectRatio) == 0) {
                    continue;
                }
            }


            FilterItem newFilterItem = new FilterItem();
            if (asset.isReserved()) {
                String coverPath = "filter/" + asset.uuid;
//                coverPath += asset.uuid + File.separator;
//                coverPath += asset.uuid;
//                coverPath += ".png";

                coverPath = PathUtils.getAssetFileBySuffixPic(coverPath);
                /*
                 * 加载assets/filter文件夹下的图片
                 * Load images in the assets / filter folder
                 * */
                asset.coverUrl = "file:///android_asset/" + coverPath;
            }
            newFilterItem.setFilterMode(FilterItem.FILTERMODE_PACKAGE);
            newFilterItem.setFilterName(asset.name);
            newFilterItem.setPackageId(asset.uuid);
            newFilterItem.setImageUrl(asset.coverUrl);
            newFilterItem.setBackgroundColor(ColorUtil.getFilterRandomBgColor());
            newFilterItem.setIsAdjusted(asset.isAdjusted);
            if ("647136C2-D334-4FFC-8949-36F2B3CC94DC".equals(asset.uuid)) {
                filterList.add(cartoonSize, newFilterItem);
            } else {
                filterList.add(newFilterItem);
            }
        }


        return filterList;
    }

    public static List<FilterItem> getBuildInFilter(Context context) {
        String[] buildInFilterName = new String[]{
                context.getString(R.string.sage),
                context.getString(R.string.maid),
                context.getString(R.string.mace),
                context.getString(R.string.lace),
                context.getString(R.string.mall),
                context.getString(R.string.sap),
                context.getString(R.string.sara),
                context.getString(R.string.pinky),
                context.getString(R.string.sweet),
                context.getString(R.string.fresh)};
        String[] buildInFilte = new String[]{
                "Sage", "Maid", "Mace", "Lace", "Mall", "Sap", "Sara", "Pinky", "Sweet", "Fresh"};
        List<FilterItem> buildInFilter = new ArrayList<>();
        for (int i = 0; i < buildInFilterName.length; i++) {
            String name = buildInFilterName[i];
            FilterItem filterItem = new FilterItem();
            filterItem.setFilterName(name);
            filterItem.setFilterId(buildInFilte[i]);
            buildInFilter.add(filterItem);
        }
        return buildInFilter;
    }


    /*
     * 获取滤镜当前选择位置
     * Get the current selected position of the filter
     * */
    public static int getSelectedFilterPos(ArrayList<FilterItem> filterDataArrayList, VideoClipFxInfo videoClipFxInfo) {
        if (filterDataArrayList == null || filterDataArrayList.size() == 0) {
            return -1;
        }

        String fxName = videoClipFxInfo.getFxId();
        if (TextUtils.isEmpty(fxName)) {
            return 0;
        }

        for (int i = 0; i < filterDataArrayList.size(); i++) {
            FilterItem newFilterItem = filterDataArrayList.get(i);
            if (newFilterItem == null) {
                continue;
            }

            int filterMode = newFilterItem.getFilterMode();
            String filterName;
            if (filterMode == FilterItem.FILTERMODE_BUILTIN) {
                filterName = newFilterItem.getFilterName();
            } else {
                filterName = newFilterItem.getPackageId();
            }

            if (fxName.equals(filterName)) {
                return i;
            }
        }

        return 0;
    }

}
