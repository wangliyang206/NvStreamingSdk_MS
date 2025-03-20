package com.meishe.sdkdemo.capturescene;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.meicam.sdk.NvsStreamingContext;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.capturescene.view.CaptureSceneEffectView;

import androidx.fragment.app.Fragment;

public class CaptureSceneFragment extends Fragment {
    private static final String ARG_TYPE = "Type";
    private int mType;
    private CaptureSceneEffectView mCaptureSceneEffectView;
    private NvsStreamingContext mStreamingContext;
    private FrameLayout mRootContainer;

    public CaptureSceneFragment() {
        // Required empty public constructor
    }

    public static CaptureSceneFragment newInstance(int type) {
        CaptureSceneFragment fragment = new CaptureSceneFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(ARG_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_capture_scene, container, false);
        mRootContainer = view.findViewById(R.id.fl_root);
        mStreamingContext = NvsStreamingContext.getInstance();
        if (mCaptureSceneEffectView == null) {
            mCaptureSceneEffectView = new CaptureSceneEffectView(getContext());
            mRootContainer.addView(mCaptureSceneEffectView);
        }
        mCaptureSceneEffectView.setStreamingContext(mStreamingContext, getActivity(), mType);
        return view;
    }

    public void clearCaptureScene() {
        mCaptureSceneEffectView.clearCaptureScene();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCaptureSceneEffectView.onDestroy();
    }

    public void addLocalResource(String filePath) {
        if (mCaptureSceneEffectView != null) {
            mCaptureSceneEffectView.addLocalResource(filePath);
        }
    }
}