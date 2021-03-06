package com.bj.eduteacher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshViewFooter;
import com.bj.eduteacher.BaseActivity;
import com.bj.eduteacher.R;
import com.bj.eduteacher.adapter.DoukeCommentAdapter;
import com.bj.eduteacher.api.LmsDataService;
import com.bj.eduteacher.api.MLConfig;
import com.bj.eduteacher.api.MLProperties;
import com.bj.eduteacher.entity.CommentInfo;
import com.bj.eduteacher.entity.MsgEvent;
import com.bj.eduteacher.integral.presenter.IntegralPresenter;
import com.bj.eduteacher.integral.view.IViewintegral;
import com.bj.eduteacher.manager.IntentManager;
import com.bj.eduteacher.utils.KeyBoardUtils;
import com.bj.eduteacher.utils.LL;
import com.bj.eduteacher.utils.LoginStatusUtil;
import com.bj.eduteacher.utils.PreferencesUtils;
import com.bj.eduteacher.utils.StringUtils;
import com.bj.eduteacher.utils.T;
import com.bj.eduteacher.widget.DecorationForDouke;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zz379 on 2017/2/3.
 * 逗课文章评论页面
 */

public class DoukeCommentActivity extends BaseActivity implements IViewintegral{

    @BindView(R.id.mXRefreshView)
    XRefreshView mXRefreshView;
    @BindView(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.edt_content)
    EditText edtContent;
    @BindView(R.id.tv_send)
    TextView tvSend;
    private IntegralPresenter presenter;

    private DoukeCommentAdapter mAdapter;
    private int currentPage = 1;
    public static long lastRefreshTime;

    private List<CommentInfo> mDataList = new ArrayList<>();
    private String newsID,type;
    private String userPhoneNumber,unionid;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_douke_comment);
        ButterKnife.bind(this);
        // 初始化页面
        initToolBar();
        initView();
        initData();
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        unionid = PreferencesUtils.getString(this, MLProperties.PREFER_KEY_WECHAT_UNIONID,"");

        presenter = new IntegralPresenter(this,this);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        TextView tvTitle = (TextView) this.findViewById(R.id.header_tv_title);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText("评论");

        LinearLayout llHeaderLeft = (LinearLayout) this.findViewById(R.id.header_ll_left);
        llHeaderLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoukeCommentActivity.this.finish();
            }
        });

        ImageView imgBack = (ImageView) this.findViewById(R.id.header_img_back);
        imgBack.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initView() {
        mRecyclerView.setHasFixedSize(true);
        // look as listview
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        // set Adatper
        mAdapter = new DoukeCommentAdapter(mDataList);
        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.setAdapter(mAdapter2);
        mRecyclerView.addItemDecoration(new DecorationForDouke(this, LinearLayoutManager.VERTICAL, 3));

        // set xRefreshView
        mXRefreshView.setPullRefreshEnable(true);
        mXRefreshView.setPullLoadEnable(true);
        mXRefreshView.restoreLastRefreshTime(lastRefreshTime);
        mXRefreshView.setAutoRefresh(false);
        mXRefreshView.setAutoLoadMore(true);
        mXRefreshView.setEmptyView(R.layout.recycler_item_comment_empty);
        mXRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isRefresh) {
                LL.i("刷新数据");
                currentPage = 1;
                mXRefreshView.setPullLoadEnable(true);
                getDoukeCommentFromAPI(currentPage);
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                LL.i("加载更多数据");
                currentPage++;
                getDoukeCommentFromAPI(currentPage);
            }
        });
    }

    @Override
    protected void initData() {
        userPhoneNumber = PreferencesUtils.getString(this, MLProperties.PREFER_KEY_USER_ID, "");
        Log.e("手机号",userPhoneNumber+"xxx");
        newsID = getIntent().getStringExtra(MLProperties.BUNDLE_KEY_DOUKE_ID);
        Log.e("文章id",newsID+"yyy");
        currentPage = 1;
        mXRefreshView.setPullLoadEnable(true);
        getDoukeCommentFromAPI(currentPage);
    }

    private void loadData(List<CommentInfo> list) {
        lastRefreshTime = mXRefreshView.getLastRefreshTime();
        if (mXRefreshView.mPullRefreshing) {
            mDataList.clear();
            mXRefreshView.stopRefresh();
        }
        if (list == null || list.size() < 10) {
            mXRefreshView.setPullLoadEnable(false);
        }
        mXRefreshView.stopLoadMore();
        // 更新数据
        mDataList.addAll(list);
        mAdapter.notifyDataSetChanged();
        if (mDataList.size() >= 10 && null == mAdapter.getCustomLoadMoreView()) {
            mAdapter.setCustomLoadMoreView(new XRefreshViewFooter(this));
        }
    }

    private void cleanXRefreshView() {
        mXRefreshView.stopRefresh();
        mXRefreshView.stopLoadMore();
    }

    @OnClick(R.id.tv_send)
    void actionSendClick() {
        //tvSend.setEnabled(false);
        String content = edtContent.getText().toString().trim();

        KeyBoardUtils.closeKeybord(this.getCurrentFocus().getWindowToken(), this);

        if(LoginStatusUtil.noLogin(this)){
            IntentManager.toLoginSelectActivity(this, IntentManager.LOGIN_SUCC_ACTION_MAINACTIVITY);
        }else {
            if(!StringUtils.isEmpty(edtContent.getText().toString().trim())){
                sendCommentContent(content);
            }else {
                T.showShort(this,"评论内容不能为空");
            }

        }
        edtContent.setText("");
    }

    private void getDoukeCommentFromAPI(final int currentPage) {
        Observable.create(new ObservableOnSubscribe<List<CommentInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<CommentInfo>> e) throws Exception {
                LmsDataService mService = new LmsDataService();
                List<CommentInfo> dataList = mService.getDoukeAllCommentFromAPI(newsID, String.valueOf(currentPage));
                e.onNext(dataList);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<CommentInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<CommentInfo> classItemInfos) {
                        loadData(classItemInfos);
                    }

                    @Override
                    public void onError(Throwable e) {
                        cleanXRefreshView();
                        T.showShort(DoukeCommentActivity.this, "服务器开小差了，请重试");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void sendCommentContent(final String content) {
        Observable.create(new ObservableOnSubscribe<String[]>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String[]> e) throws Exception {
                userPhoneNumber = PreferencesUtils.getString(DoukeCommentActivity.this, MLProperties.PREFER_KEY_USER_ID, "");
                String unionid = PreferencesUtils.getString(DoukeCommentActivity.this, MLProperties.PREFER_KEY_WECHAT_UNIONID,"");

                LmsDataService mService = new LmsDataService();
                String[] result = mService.postDoukeCommentFromAPI(newsID, userPhoneNumber,
                        MLConfig.KEY_DOUKE_COMMENT_JIAOSHI, content,unionid);
                e.onNext(result);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String[]>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String[] result) {
                        tvSend.setEnabled(true);
                        if (!StringUtils.isEmpty(result[0]) && "1".equals(result[0])) {
                            // 发布成功
                            mXRefreshView.startRefresh();

                            //add by gpx
                            setResult(100);

                            EventBus.getDefault().post(new MsgEvent("pinglunsuccess",type));
                            String unionid = PreferencesUtils.getString(DoukeCommentActivity.this, MLProperties.PREFER_KEY_WECHAT_UNIONID,"");
                            presenter.getDouBi("pinglun",userPhoneNumber,"getdoubi",unionid);
                        } else {
                            // 发布失败
                            T.showShort(DoukeCommentActivity.this, "发布评论失败");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        tvSend.setEnabled(true);
                        T.showShort(DoukeCommentActivity.this, "服务器开小差了，请重试");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("comment");
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("comment");
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestory();
    }

    @Override
    public void getDouBi(com.bj.eduteacher.integral.model.Doubi doubi) {
        //T.showShort(this,"获取成功");
        EventBus.getDefault().post(new MsgEvent("getdoubisuccess",doubi.getData().getUser_doubinum_sum()));
    }
}
