package com.meishe.sdkdemo.feedback;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.meicam.sdk.NvsStreamingContext;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.base.BaseActivity;
import com.meishe.sdkdemo.edit.view.CustomTitleBar;
import com.meishe.sdkdemo.utils.BuildHelper;
import com.meishe.sdkdemo.utils.Logger;
import com.meishe.utils.StatusBarUtils;

import androidx.core.content.ContextCompat;
import okhttp3.Request;

public class FeedBackActivity extends BaseActivity {

    private final String TAG = "FeedBackActivity";

    private static int MAX_NUM = 1000;

    CustomTitleBar mTitleBar;
    EditText feedbackEditText;
    EditText feedbackEditTextNumber;
    Button feedbackCommit;
    TextView feedbackTextSize;

    @Override
    protected int initRootView() {
        StatusBarUtils.setColor(this, Color.WHITE);
        StatusBarUtils.setTextDark(this, true);
        return R.layout.activity_feed_back;
    }

    @Override
    protected void initViews() {
        mTitleBar = (CustomTitleBar) findViewById(R.id.title_bar);
        feedbackTextSize = (TextView) findViewById(R.id.feedback_textSize);
        feedbackCommit = (Button) findViewById(R.id.feedback_commit);
        feedbackEditText = (EditText) findViewById(R.id.feedback_editText);
        feedbackEditTextNumber = (EditText) findViewById(R.id.feedback_editText_number);
        setEditTextHint(feedbackEditText, R.string.feedback_hint_text);
        setEditTextHint(feedbackEditTextNumber, R.string.feedback_hint_number);
    }

    private void setEditTextHint(EditText feedbackEditText, int textId) {
        SpannableString ss = new SpannableString(getResources().getString(textId));
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(14, true);
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        feedbackEditText.setHint(new SpannedString(ss));
    }

    @Override
    protected void initTitle() {
        mTitleBar.setTextCenter(R.string.feedback);
        mTitleBar.setMainLayoutResource(R.color.white);
        mTitleBar.setBackImageIcon(R.drawable.main_webview_back);
        mTitleBar.setTextCenterColor(ContextCompat.getColor(FeedBackActivity.this, R.color.ff333333));
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        feedbackCommit.setOnClickListener(this);
        feedbackEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > MAX_NUM) {
                    s.delete(MAX_NUM, s.length());
                }
                int num = s.length();
                feedbackTextSize.setText(String.valueOf(num) + "/1000");
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.feedback_commit) {
            if (isEditTextEmpty(feedbackEditText)) {
                showToastCenter(R.string.feedback_tip1);
            } else if (isEditTextEmpty(feedbackEditTextNumber)) {
                showToastCenter(R.string.feedback_tip2);
            } else {
                NvsStreamingContext.SdkVersion sdkVersion = NvsStreamingContext.getInstance().getSdkVersion();
                String sdkVersionNum = String.valueOf(sdkVersion.majorVersion) + "." + String.valueOf(sdkVersion.minorVersion) + "." + String.valueOf(sdkVersion.revisionNumber);
                FeedBackData feedBackData = new FeedBackData(feedbackEditText.getText().toString(), feedbackEditTextNumber.getText().toString()
                        , sdkVersionNum, BuildHelper.getBrand() + "-" + BuildHelper.getMode() + "-" + BuildHelper.getAndroidVersion());
                Gson gson = new Gson();
                String json = gson.toJson(feedBackData);
                String url = "https://vsapi.meishesdk.com/feedback/index.php?command=feedback";
                OkHttpClientManager.postAsynJson(url, json, new OkHttpClientManager.ResultCallback<FeedBackResponseData>() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Logger.e(TAG, "onError: " + e.toString());
                    }

                    @Override
                    public void onResponse(FeedBackResponseData response) {
                        if (response != null && response.getErrNo() == 0) {
                            showToastCenter(R.string.feedback_tip3);
                        } else {
                            showToastCenter(R.string.feedback_tip4);
                        }
                    }
                });
            }
        }
    }

    private void showToastCenter(int id) {
        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(id), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private boolean isEditTextEmpty(EditText editText) {
        return TextUtils.isEmpty(editText.getText().toString());
    }
}
