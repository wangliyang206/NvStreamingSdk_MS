package com.meishe.sdkdemo.mimodemo.mediapaker.utils;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.meishe.sdkdemo.MeicamContextWrap;
import com.meishe.sdkdemo.mimodemo.mediapaker.bean.MediaData;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.core.os.BuildCompat;

/**
 * Created by ${gexinyu} on 2018/5/28.
 */

public class MediaUtils {

    /**
     * 读取手机中所有图片信息
     * Read all the picture information in the phone
     */
    public static void getAllPhotoInfo(final Activity activity, final LocalMediaCallback localMediaCallback) {
        ThreadPoolUtils.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final List<MediaData> mediaBeen = new ArrayList<>();
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                String[] projection = {MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Thumbnails.DATA
                };
                //全部图片 All pictures
                String where = MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?";
                //指定格式 Specified format
                String[] whereArgs = {"image/jpeg", "image/png", "image/jpg"};
                //查询 query
                Cursor mCursor = activity.getContentResolver().query(
                        mImageUri, projection, null, null,
                        null);
                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        // 获取图片的路径 The path to get the picture
                        int thumbPathIndex = mCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
                        int timeIndex = mCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                        int pathIndex = mCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        int id = mCursor.getColumnIndex(MediaStore.Images.Media._ID);

                        Long date = mCursor.getLong(timeIndex) * 1000;
                        String filepath, thumbPath;
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P || BuildCompat.isAtLeastQ()) {
                            thumbPath = MediaStore.Images.Media
                                    .EXTERNAL_CONTENT_URI
                                    .buildUpon()
                                    .appendPath(String.valueOf(mCursor.getInt(id))).build().toString();
                            filepath = thumbPath;
                            if (FileManagerUtils.isContentUriExists(activity.getApplicationContext(), Uri.parse(filepath))) {
                                MediaData fi = new MediaData(id, MediaConstant.IMAGE, filepath, "", getPhotoUri(mCursor), date, "", false);
                                mediaBeen.add(fi);
                            }
                        } else {
                            thumbPath = mCursor.getString(thumbPathIndex);
                            filepath = mCursor.getString(pathIndex);
                            //判断文件是否存在，存在才去加入
                            //Check whether the file exists. Add the file only when it exists
                            boolean b = FileManagerUtils.fileIsExists(filepath);
                            if (b) {
                                File f = new File(filepath);
                                MediaData fi = new MediaData(id, MediaConstant.IMAGE, filepath, thumbPath, null, date, f.getName(), false);
                                mediaBeen.add(fi);
                            }
                        }
                    }
                    mCursor.close();
                }
                //更新界面 Update interface
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //采用冒泡排序的方式排列数据
                        //The data is arranged by bubble sort
                        sortByTimeRepoList(mediaBeen);
                        localMediaCallback.onLocalMediaCallback(mediaBeen);
                    }
                });
            }
        });
    }

    /**
     * 获取手机中所有视频的信息
     * Get information about all the videos on the phone
     */
    public static void getAllVideoInfos(final Activity activity, final LocalMediaCallback localMediaCallback) {
        ThreadPoolUtils.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final List<MediaData> videoList = new ArrayList<>();
                Uri mVideoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.Video.Thumbnails._ID
                        , MediaStore.Video.Thumbnails.DATA
                        , MediaStore.Video.Media.DURATION
                        , MediaStore.Video.Media.SIZE
                        , MediaStore.Video.Media.DATE_ADDED
                        , MediaStore.Video.Media.DISPLAY_NAME
                        , MediaStore.Video.Media.DATE_MODIFIED};
                //全部视频 Full video
                String where = MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Video.Media.MIME_TYPE + "=? or "
                        + MediaStore.Video.Media.MIME_TYPE + "=? or "
                        + MediaStore.Video.Media.MIME_TYPE + "=? or "
                        + MediaStore.Video.Media.MIME_TYPE + "=? or "
                        + MediaStore.Video.Media.MIME_TYPE + "=? or "
                        + MediaStore.Video.Media.MIME_TYPE + "=? or "
                        + MediaStore.Video.Media.MIME_TYPE + "=? or "
                        + MediaStore.Video.Media.MIME_TYPE + "=?";
                String[] whereArgs = {"video/mp4", "video/3gp", "video/aiv", "video/rmvb", "video/vob", "video/flv",
                        "video/mkv", "video/mov", "video/mpg"};
                Cursor mCursor = activity.getContentResolver().query(mVideoUri,
                        projection, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC ");

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        // 获取视频的路径
//                        int videoId = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Video.Media._ID));
//                        String path;
//                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P || BuildCompat.isAtLeastQ()){
//                            path =MediaStore.Video.Media
//                                    .EXTERNAL_CONTENT_URI
//                                    .buildUpon()
//                                    .appendPath(String.valueOf(videoId)).build().toString();
//                        }else{
//                            path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DATA));
//                        }
//                        long duration = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Video.Media.DURATION));
//                        long size = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Video.Media.SIZE)) / 1024; //单位kb
//                        if (size < 0) {
//                            //某些设备获取size<0，直接计算
//                            Log.e("dml", "this video size < 0 " + path);
//                            size = new File(path).length() / 1024;
//                        }
//                        String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
//                        //用于展示相册初始化界面
//                        int timeIndex = mCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
//                        Long date = mCursor.getLong(timeIndex) *1000;
//                        //需要判断当前文件是否存在  一定要加，不然有些文件已经不存在图片显示不出来。这里适配Android Q
//                        synchronized (activity) {
//                            boolean fileIsExists;
//                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P || BuildCompat.isAtLeastQ()) {
//                                fileIsExists = FileManagerUtils.isContentUriExists(activity.getApplicationContext(), Uri.parse(path));
//                                if (fileIsExists) {
//                                    videoList.add(new MediaData(videoId, MediaConstant.VIDEO, path, "", getVideoUri(mCursor), duration, date, displayName, false));
//                                }
//                            } else {
//                                fileIsExists = FileManagerUtils.fileIsExists(path);
//                                if (fileIsExists) {
//                                    videoList.add(new MediaData(videoId, MediaConstant.VIDEO, path, path, null, duration, date, displayName, false));
//                                }
//                            }
//                        }


                        // 获取视频的路径 Get the path to the video
                        int videoId = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Video.Media._ID));
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DATA));
                        long duration = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                        long size = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Video.Media.SIZE)) / 1024; //单位kb
                        if (size < 0) {
                            //某些设备获取size<0，直接计算 Some devices get size&lt; 0, just calculate it
                            Log.e("dml", "this video size < 0 " + path);
                            size = new File(path).length() / 1024;
                        }
                        String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                        //用于展示相册初始化界面 Used to display the album initialization interface
                        int timeIndex = mCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                        Long date = mCursor.getLong(timeIndex) * 1000;
                        //需要判断当前文件是否存在  一定要加，不然有些文件已经不存在图片显示不出来
                        //Need to determine whether the current file exists must be added, otherwise some files no longer exist picture can not be displayed
                        boolean fileIsExists = FileManagerUtils.fileIsExists(path);
                        if (fileIsExists) {
                            videoList.add(new MediaData(videoId, MediaConstant.VIDEO, path, path, null, duration, date, displayName, false));
                        }
                    }
                    mCursor.close();
                }
                //更新界面
                //Update interface
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        localMediaCallback.onLocalMediaCallback(videoList);
                    }
                });
            }
        });
    }

    /**
     * 所有信息
     * All information
     */
    public static void getAllPhotoAndVideo(final Activity activity, final LocalMediaCallback localMediaCallback) {
        getAllPhotoInfo(activity, new LocalMediaCallback() {
            @Override
            public void onLocalMediaCallback(final List<MediaData> allPhotos) {
                getAllVideoInfos(activity, new LocalMediaCallback() {
                    @Override
                    public void onLocalMediaCallback(List<MediaData> allVideos) {
                        //图片 picture
                        final List<MediaData> allMediaList = new ArrayList<>();

                        final List<MediaData> allPhotoList = new ArrayList<>();
                        if (allPhotos != null && allPhotos.size() > 0) {
                            allPhotoList.addAll(allPhotos);
                        }
                        //视频 video
                        final List<MediaData> allVideoList = new ArrayList<>();
                        if (allVideos != null && allVideos.size() > 0) {
                            allVideoList.addAll(allVideos);
                        }
                        allMediaList.addAll(allPhotoList);
                        allMediaList.addAll(allVideoList);
                        //采用冒泡排序的方式排列数据
                        //The data is arranged by bubble sort
                        sortByTimeRepoList(allMediaList);
                        localMediaCallback.onLocalMediaCallback(allMediaList);
                    }
                });
            }
        });
    }

    /**
     * 根据类型返回media
     * Returns media according to type
     *
     * @param mActivity
     * @param index
     */
    public static void getMediasByType(Activity mActivity, int index, LocalMediaCallback localMediaCallback) {
        if (index == MediaConstant.ALL_MEDIA) {
            getAllPhotoAndVideo(mActivity, localMediaCallback);
        } else if (index == MediaConstant.VIDEO) {
            getAllVideoInfos(mActivity, localMediaCallback);
        } else {
            getAllPhotoInfo(mActivity, localMediaCallback);
        }
    }

    /**
     * 根据时间进行排序
     * Sort by time
     *
     * @param itemInfoList
     */
    public static void sortByTimeRepoList(List<MediaData> itemInfoList) {
        Collections.sort(itemInfoList, new Comparator<MediaData>() {
                    @Override
                    public int compare(MediaData item1, MediaData item2) {
                        Date date1 = new Date(item1.getData() * 1000L);
                        Date date2 = new Date(item2.getData() * 1000L);
                        return date2.compareTo(date1);
                    }
                }
        );
    }


    /**
     * 获取数据库字段
     * Get database field
     *
     * @param uri
     */
    private static void getUriColumns(Uri uri) {
        Cursor cursor = MeicamContextWrap.getInstance().getContext().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String[] columns = cursor.getColumnNames();

        for (String string : columns) {
            System.out.println(cursor.getColumnIndex(string) + " = " + string);
        }
        cursor.close();
    }

    public static ListOfAllMedia groupListByTime(List<MediaData> allMediaTemp) {
        //分组算法 Grouping algorithm
        Map<String, List<MediaData>> skuIdMap = new LinkedHashMap<>();
        for (MediaData mediaData : allMediaTemp) {
            String strTime = new SimpleDateFormat("yyyy年MM月dd日").format(new Date(mediaData.getData()));
            List<MediaData> tempList = skuIdMap.get(strTime);
            /*如果取不到数据,那么直接new一个空的ArrayList**/
            if (tempList == null) {
                tempList = new ArrayList<>();
                tempList.add(mediaData);
                skuIdMap.put(strTime, tempList);
            } else {
                tempList.add(mediaData);
            }
        }
        List<List<MediaData>> lists = new ArrayList<>();
        List<MediaData> listOfOut = new ArrayList<>();
        for (Map.Entry<String, List<MediaData>> entry : skuIdMap.entrySet()) {
            MediaData mediaData = new MediaData();
            mediaData.setData(getIntTime(entry.getKey()));
            listOfOut.add(mediaData);
            lists.add(entry.getValue());
        }
        return new ListOfAllMedia(listOfOut, lists);
    }

    private static long getIntTime(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        Date date;
        try {
            date = sdr.parse(time);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public interface LocalMediaCallback {

        void onLocalMediaCallback(List<MediaData> allMediaTemp);
    }

    public static class ListOfAllMedia {
        private List<MediaData> listOfParent;
        private List<List<MediaData>> listOfAll;

        public ListOfAllMedia(List<MediaData> listOfParent, List<List<MediaData>> listOfAll) {
            this.listOfParent = listOfParent;
            this.listOfAll = listOfAll;
        }

        public List<MediaData> getListOfParent() {
            if (listOfParent == null) {
                return new ArrayList<>();
            }
            return listOfParent;
        }

        public void setListOfParent(List<MediaData> listOfParent) {
            this.listOfParent = listOfParent;
        }

        public List<List<MediaData>> getListOfAll() {
            if (listOfAll == null) {
                return new ArrayList<>();
            }
            return listOfAll;
        }

        public void setListOfAll(List<List<MediaData>> listOfAll) {
            this.listOfAll = listOfAll;
        }
    }

    public static Uri getPhotoUri(Cursor cursor) {
        return getMediaUri(cursor, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    public static Uri getVideoUri(Cursor cursor) {
        return getMediaUri(cursor, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
    }

    public static Uri getMediaUri(Cursor cursor, Uri uri) {
        String id = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
        return Uri.withAppendedPath(uri, id);
    }

}
