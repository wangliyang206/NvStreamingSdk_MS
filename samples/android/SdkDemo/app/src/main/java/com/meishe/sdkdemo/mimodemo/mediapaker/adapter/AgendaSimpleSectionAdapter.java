/*
 * Copyright (C) 2015 Tomás Ruiz-López.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meishe.sdkdemo.mimodemo.mediapaker.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meishe.sdkdemo.MeicamContextWrap;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.mimodemo.common.utils.Logger;
import com.meishe.sdkdemo.mimodemo.mediapaker.bean.MediaData;
import com.meishe.sdkdemo.mimodemo.mediapaker.fragment.MediaFragment;
import com.meishe.sdkdemo.mimodemo.mediapaker.interfaces.OnClipAdd;
import com.meishe.sdkdemo.mimodemo.mediapaker.interfaces.OnItemClick;
import com.meishe.sdkdemo.mimodemo.mediapaker.interfaces.OnTotalNumChange;
import com.meishe.sdkdemo.mimodemo.mediapaker.utils.MediaConstant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;


public class AgendaSimpleSectionAdapter extends SimpleSectionedAdapter<AgendaItemViewHolder> implements OnItemClick {
    private final String TAG = getClass().getName();

    public void setSelectList(List<MediaData> selectList) {
        this.selectList = selectList;
        notifyDataSetChanged();
    }

    private List<MediaData> selectList = new ArrayList<>();
    private List<List<MediaData>> lists;
    private List<MediaData> listOfParent;
    private RecyclerView recyclerView;
    private OnTotalNumChange mOnTotalNumChange;
    private int tag;
    private Activity mActivity;
    private MediaFragment fragment;
    private int clickType;
    private int limitCount = -1;

    private OnClipAdd mOnClipAddListener;

    public void setOnClipAddListener(OnClipAdd onClipAddListener) {
        this.mOnClipAddListener = onClipAddListener;
    }

    public AgendaSimpleSectionAdapter(List<List<MediaData>> agenda, List<MediaData> listOfOut, RecyclerView recyclerView,
                                      OnTotalNumChange onTotalNumChange, int tag, Activity mActivity, int clickType) {
        this.lists = agenda;
        this.recyclerView = recyclerView;
        this.listOfParent = listOfOut;
        this.mOnTotalNumChange = onTotalNumChange;
        this.tag = tag;
        this.mActivity = mActivity;
        this.clickType = clickType;
        setClickType(clickType);
    }

    public AgendaSimpleSectionAdapter(List<List<MediaData>> agenda, List<MediaData> listOfOut, RecyclerView recyclerView,
                                      OnTotalNumChange onTotalNumChange, int tag, Activity mActivity, int clickType, int limitCount, MediaFragment fragment) {
        this(agenda, listOfOut, recyclerView, onTotalNumChange, tag, mActivity, clickType);
        this.limitCount = limitCount;
        this.fragment = fragment;
        setLimitMediaCount(limitCount);
    }

    public List<MediaData> getSelectList() {
        if (selectList == null) {
            return new ArrayList<>();
        }
        return selectList;
    }

    @Override
    protected String getSectionHeaderTitle(int section) {
        //判断当前日期是否是今天的
        //Determine if the current date is today
        @SuppressLint("SimpleDateFormat")
        String yearMonthDate = MeicamContextWrap.getInstance().getContext().getResources().getString(R.string.yearMonthDate);
        SimpleDateFormat format = new SimpleDateFormat(yearMonthDate);
        String todayTime = format.format(new Date());
        String today = MeicamContextWrap.getInstance().getContext().getResources().getString(R.string.today);
        return todayTime.equals(listOfParent.get(section).getDate()) ? today : listOfParent.get(section).getDate();
    }

    @Override
    protected List<MediaData> getList() {
        return listOfParent;
    }

    @Override
    protected OnItemClick getHeadItemCick() {
        return this;
    }

    @Override
    protected int getSectionCount() {
        return lists.size();
    }

    @Override
    protected int getItemCountForSection(int section) {
        return lists.get(section).size();
    }

    @Override
    protected AgendaItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.mimo_item_media, parent, false);
        view.setTag("1");
        return new AgendaItemViewHolder(view, clickType);
    }

    @Override
    protected void onBindItemViewHolder(AgendaItemViewHolder holder, int section, int position) {
        holder.render(lists.get(section).get(position), section, position, this);
    }

    @Override
    public void OnHeadClick(View v, int position) {
        listOfParent.get(position).setState(!listOfParent.get(position).isState());
        int nowPosition = recyclerView.getChildAdapterPosition(v);
        recyclerView.getAdapter().notifyItemChanged(nowPosition);
        Logger.e(TAG, "OnHeadClick   " + nowPosition);
        for (int i = 0; i < lists.get(position).size(); i++) {
            int currentPosition = nowPosition + i + 1;
            if (listOfParent.get(position).isState()) {
                if (!lists.get(position).get(i).isState()) {
                    totalChange(lists.get(position).get(i).isState(), position, i, currentPosition, true);
                    onTotalChange();
                }
            } else {
                totalChange(lists.get(position).get(i).isState(), position, i, currentPosition, true);
                onTotalChange();
            }
        }
    }


    @Override
    public void OnItemClick(View v, int headListPosition, int childListPosition) {
        if (!lists.get(headListPosition).get(childListPosition).isState() && limitCount != -1 && getTotal() == limitCount) {
            return;
        }
        itemClick(v, headListPosition, childListPosition, true);
    }

    public void itemClick(View v, int headListPosition, int childListPosition, boolean changeTotal) {
        Logger.e(TAG, "OnItemClick  第" + headListPosition + "行    第" + childListPosition + "个");
        if (mOnClipAddListener != null) {
            MediaData mediaData = lists.get(headListPosition).get(childListPosition);
            selectList.add(mediaData);
            mOnClipAddListener.onClipAdd(mediaData.getPath(), mediaData.getDuration());
        }
        if (clickType == MediaConstant.TYPE_ITEMCLICK_SINGLE) {
            onSingleClick(headListPosition, childListPosition, recyclerView.getChildAdapterPosition(v));
        } else {
            //改变总数的值 Change the value of the total
            totalChange(lists.get(headListPosition).get(childListPosition).isState(), headListPosition, childListPosition, recyclerView.getChildAdapterPosition(v), changeTotal);
            checkAndChangeParentItemAfterItemChange(headListPosition);
        }
        //onTotalChange();
    }

    private void onSingleClick(int headListPosition, int childListPosition, int positionInRecycle) {
        if (selectList.size() == 0) {
            selectList.add(lists.get(headListPosition).get(childListPosition));
            refreshView(headListPosition, childListPosition, positionInRecycle);
        } else {
            if (selectList.get(0).getPath().equals(lists.get(headListPosition).get(childListPosition).getPath())) {
                selectList.remove(lists.get(headListPosition).get(childListPosition));
                refreshView(headListPosition, childListPosition, positionInRecycle);
            } else {
                Point point = getPointByData(lists, selectList.get(0));
                refreshView(point.x, point.y, getPositionByData(lists, selectList.get(0)));
                selectList.clear();
                selectList.add(lists.get(headListPosition).get(childListPosition));
                refreshView(headListPosition, childListPosition, positionInRecycle);
            }
        }
    }


    private void refreshView(int headListPosition, int childListPosition, int positionInRecycle) {
        //lists.get(headListPosition).get(childListPosition).setState(!lists.get(headListPosition).get(childListPosition).isState());
        //recyclerView.getAdapter().notifyItemChanged(positionInRecycle);
    }

    private void checkAndChangeParentItemAfterItemChange(int headListPosition) {
        if (listOfParent.get(headListPosition).isState()) {
            listOfParent.get(headListPosition).setState(!listOfParent.get(headListPosition).isState());
            int nowPosition = recyclerView.getChildAdapterPosition(recyclerView.findViewWithTag("Which line：       " + headListPosition));
            recyclerView.getAdapter().notifyItemChanged(nowPosition);
        } else {
            int count = 0;
            for (int i = 0; i < lists.get(headListPosition).size(); i++) {
                if (lists.get(headListPosition).get(i).isState()) {
                    count++;
                } else {
                    count--;
                }
            }
            if (Math.abs(count) == lists.get(headListPosition).size() && count > 0) {
                listOfParent.get(headListPosition).setState(!listOfParent.get(headListPosition).isState());
                int nowPosition = recyclerView.getChildAdapterPosition(recyclerView.findViewWithTag("Which line：       " + headListPosition));
                recyclerView.getAdapter().notifyItemChanged(nowPosition);
            }
        }
    }

    private void totalChange(boolean isAdd, int headListPosition, int childListPosition, int positionInRecycle, boolean changeTotal) {
        Logger.e(TAG, "totalChange   " + positionInRecycle);
        lists.get(headListPosition).get(childListPosition).setState(!lists.get(headListPosition).get(childListPosition).isState());
        if (!isAdd) {
            if (changeTotal) {
                setTotal(getTotal() + 1);
            }
            selectList.add(lists.get(headListPosition).get(childListPosition));
        } else {
            if (changeTotal) {
                setTotal(getTotal() - 1);
            }
            int totalPosition = lists.get(headListPosition).get(childListPosition).getPosition();
            selectList.remove(lists.get(headListPosition).get(childListPosition));
            for (MediaData mediaData : selectList) {
                if (mediaData.getPosition() > totalPosition) {
                    int position = getPositionByData(lists, mediaData);
                    mediaData.setPosition(mediaData.getPosition() - 1);
                    recyclerView.getAdapter().notifyItemChanged(position);
                }
            }
        }
        lists.get(headListPosition).get(childListPosition).setPosition(getTotal());
        recyclerView.getAdapter().notifyItemChanged(positionInRecycle);
    }

    private void onTotalChange() {
        mOnTotalNumChange.onTotalNumChange(selectList, tag);
    }

    private int getTotal() {
        return fragment.getTotalSize();
    }

    private void setTotal(int total) {
        fragment.setTotalSize(total);
    }

    public int getPositionByData(List<List<MediaData>> list, MediaData mediaData) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).size(); j++) {
                if (checkIsThisData(list.get(i).get(j), mediaData)) {
                    return j + getCountFromListAndListByIndex(list, i);
                }
            }
        }
        return -1;
    }

    public Point getPointByData(List<List<MediaData>> list, MediaData mediaData) {
        Point point = new Point(0, 0);
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).size(); j++) {
                if (checkIsThisData(list.get(i).get(j), mediaData)) {
                    return new Point(i, j);
                }
            }
        }
        return point;
    }

    public MediaData getDataByPath(List<List<MediaData>> list, String path) {
        MediaData mediaData = new MediaData();
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).size(); j++) {
                if (checkDataByPath(list.get(i).get(j), path)) {
                    return list.get(i).get(j);
                }
            }
        }
        return mediaData;
    }

    /**
     * 图片没有对应的ID，要根据路径来比较
     * There is no corresponding ID for the picture, so compare according to the path
     *
     * @param listData  列表中的数据 Data in a list
     * @param mediaData 要比对的数据 The data to be compared
     * @return 图片根据路径，视频根据ID Picture by path, video by ID
     */
    private boolean checkIsThisData(MediaData listData, MediaData mediaData) {
        if (mediaData.getType() == MediaConstant.IMAGE) {
            return listData.getPath().equals(mediaData.getPath());
        } else {
            return listData.getId() == mediaData.getId();
        }
    }

    private boolean checkDataByPath(MediaData listData, String path) {
        return listData.getPath().equals(path);
    }

    private int getCountFromListAndListByIndex(List<List<MediaData>> list, int index) {
        int count = 0;
        if (index > 0) {
            for (int i = 0; i < index; i++) {
                count += list.get(i).size();
            }
        }
        return count + index + 1;
    }
}
