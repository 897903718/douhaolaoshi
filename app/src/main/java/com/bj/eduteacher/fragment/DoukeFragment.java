package com.bj.eduteacher.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshViewFooter;
import com.bj.eduteacher.BaseFragment;
import com.bj.eduteacher.R;
import com.bj.eduteacher.activity.AnnualCaseAllActivity;
import com.bj.eduteacher.activity.CourseAllActivity;
import com.bj.eduteacher.activity.DoukeDetailActivity;
import com.bj.eduteacher.activity.FamousTeacherAllActivity;
import com.bj.eduteacher.activity.FamousTeacherDetailActivity;
import com.bj.eduteacher.master.view.MasterAllActivity;
import com.bj.eduteacher.activity.ZhuantiActivity;
import com.bj.eduteacher.course.view.CourseDetailActivity2;
import com.bj.eduteacher.entity.MsgEvent;
import com.bj.eduteacher.group.list.view.GroupAllActivity;
//import com.bj.eduteacher.activity.LiveActivity;
//import com.bj.eduteacher.activity.LiveAllActivity;
import com.bj.eduteacher.activity.PayProtocolActivity;
import com.bj.eduteacher.activity.ResReviewActivity;
import com.bj.eduteacher.activity.WebviewActivity;
import com.bj.eduteacher.activity.ZhuanjiaDetailActivity;
import com.bj.eduteacher.adapter.DoukeListAdapter;
import com.bj.eduteacher.api.LmsDataService;
import com.bj.eduteacher.api.MLConfig;
import com.bj.eduteacher.api.MLProperties;
import com.bj.eduteacher.dialog.TipsAlertDialog3;
import com.bj.eduteacher.dialog.UpdateAPPAlertDialog;
import com.bj.eduteacher.entity.AppVersionInfo;
import com.bj.eduteacher.entity.ArticleInfo;
import com.bj.eduteacher.entity.OrderInfo;
import com.bj.eduteacher.entity.TradeInfo;
import com.bj.eduteacher.login.view.LoginActivity;
import com.bj.eduteacher.manager.IntentManager;
import com.bj.eduteacher.model.CurLiveInfo;
import com.bj.eduteacher.model.MySelfInfo;
import com.bj.eduteacher.prize.view.PrizeActivity;
import com.bj.eduteacher.school.list.view.SchoolAllActivity;
import com.bj.eduteacher.service.DownloadAppService;
import com.bj.eduteacher.tool.Constants;
import com.bj.eduteacher.tool.ShowNameUtil;
import com.bj.eduteacher.utils.AppUtils;
import com.bj.eduteacher.utils.FrescoImageLoader;
import com.bj.eduteacher.utils.LL;
import com.bj.eduteacher.utils.LoginStatusUtil;
import com.bj.eduteacher.utils.NetUtils;
import com.bj.eduteacher.utils.PreferencesUtils;
import com.bj.eduteacher.utils.ScreenUtils;
import com.bj.eduteacher.utils.StringUtils;
import com.bj.eduteacher.utils.T;
import com.bj.eduteacher.videoplayer.view.PlayerActivity;
import com.bj.eduteacher.view.OnRecyclerItemClickListener;
import com.bj.eduteacher.widget.manager.SaveGridLayoutManager;
import com.bj.eduteacher.zzokhttp.OkHttpUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function7;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zz379 on 2017/4/7.
 * 首页
 */

public class DoukeFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.mXRefreshView)
    XRefreshView mXRefreshView;
    @BindView(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.header_tv_title)
    TextView tvTitle;

    private String teacherPhoneNumber, phoneNumberBack;

    private DoukeListAdapter mAdapter;
    private int currentPage = 1;
    public static long lastRefreshTime;
    private List<ArticleInfo> mDataList = new ArrayList<>();
    private List<ArticleInfo> mBannerList = new ArrayList<>();
    private CompositeDisposable disposables = new CompositeDisposable();

    private Banner banner;
    List<String> headerImages = new ArrayList<>();
    private GridLayoutManager layoutManager;

    private IWXAPI api;
    private PopupWindow popPayDetail;
    private boolean isUserPaySuccess = false;  // 是否支付成功
    private String currTradeID = "";     // 当前商户订单号

    private int columnResNum, columnTeaNum;
    private int resPageSize, teaPageSize;
    private TextView tab1, tab2, tab3, tab4,tab5;
    private String unionid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_douke, container, false);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        init();

        initToolbar();
        initView();
        initData();
        return view;
    }

    private void init() {
        api = WXAPIFactory.createWXAPI(getActivity(), MLProperties.APP_DOUHAO_TEACHER_ID);
        if (ScreenUtils.isPadDevice(getActivity())) {
            columnResNum = 6;
            resPageSize = 15;
            columnTeaNum = 10;
            teaPageSize = 9;
        } else {
            columnResNum = 10;
            resPageSize = 9;
            columnTeaNum = 15;
            teaPageSize = 8;
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected View loadViewLayout(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected void bindViews(View view) {

    }

    @Override
    protected void processLogic() {

    }

    @Override
    protected void setListener() {

    }

    private void initToolbar() {
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.app_name);
        // testRxJavaError();
    }


    private void initView() {
        teacherPhoneNumber = PreferencesUtils.getString(getActivity(), MLProperties.PREFER_KEY_USER_ID, "");
        // 下拉刷新控件
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new SaveGridLayoutManager(getActivity(), 30);
        mAdapter = new DoukeListAdapter(getActivity(), mDataList);
        // 添加header
        LinearLayout headerView = (LinearLayout) mAdapter.setHeaderView(R.layout.recycler_header_banner, mRecyclerView);

        initHeaderView(headerView);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(mRecyclerView) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, int position) {
                if (position > 0) {
                    ArticleInfo item = mDataList.get(position - 1);
                    actionOnItemClick(item);
                }
            }

            @Override
            public void onLongClick(RecyclerView.ViewHolder holder, int position) {

            }
        });

        // set xRefreshView
        mXRefreshView.setMoveForHorizontal(true);   // 在手指横向移动的时候，让XRefreshView不拦截事件
        mXRefreshView.setPullRefreshEnable(true);
        mXRefreshView.setPullLoadEnable(true);
        mXRefreshView.restoreLastRefreshTime(lastRefreshTime);
        mXRefreshView.setAutoRefresh(false);
        mXRefreshView.setAutoLoadMore(true);
        mXRefreshView.setEmptyView(R.layout.recycler_item_douke_empty);
        mXRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isPullDown) {

                currentPage = 1;

                getBannerInfoFromAPI();
                getRefreshDataList();
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                LL.i("加载更多数据");
                currentPage++;
                getDoukeList();
            }
        });

        // 检查新版本 如果是Wi-Fi环境下才进行检查
        if (NetUtils.isConnected(getActivity()) && NetUtils.isWifi(getActivity())) {
            checkAppNewVersion();
        }
    }

    /**
     * 页面的点击动作
     *
     * @param item
     */
    private void actionOnItemClick(ArticleInfo item) {
        if (item.getShowType() == ArticleInfo.SHOW_TYPE_ZHUANJIA) {
            MobclickAgent.onEvent(getActivity(), "expert_click");
            Intent intent = new Intent(getActivity(), ZhuanjiaDetailActivity.class);
            intent.putExtra(MLProperties.BUNDLE_KEY_ZHUANJIA_ID, item.getArticleID());
            intent.putExtra(MLProperties.BUNDLE_KEY_ZHUANJIA_NAME, item.getAuthor());
            intent.putExtra(MLProperties.BUNDLE_KEY_ZHUANJIA_TITLE, item.getTitle());
            intent.putExtra(MLProperties.BUNDLE_KEY_ZHUANJIA_IMG, item.getAuthImg());
            startActivity(intent);
        } else if (item.getShowType() == ArticleInfo.SHOW_TYPE_ZHUANJIA_ALL) {
            String text = item.getTitle();
            if (text.endsWith("专家")) {
                Intent intent = new Intent(getActivity(), MasterAllActivity.class);
                startActivity(intent);
            } else if (text.endsWith("名师")) {
                Intent intent = new Intent(getActivity(), FamousTeacherAllActivity.class);
                startActivity(intent);
            } else if (text.endsWith("课程")) {
                Intent intent = new Intent(getActivity(), CourseAllActivity.class);
                startActivity(intent);
            } else {
                if (bottomTabListener != null) {
                    //add by gpx
                    bottomTabListener.onTabChange(2);
                    //bottomTabListener.onTabChange(1);
                }
            }
        } else if (item.getShowType() == ArticleInfo.SHOW_TYPE_ZHUANJIA_RES) {
            String price = item.getAgreeNumber();
            String buyType = item.getCommentNumber();

            if (!StringUtils.isEmpty(price) && !"0".equals(price) && "0".equals(buyType)) {
                // 如果资源不是免费，需要先登录
                if (LoginStatusUtil.noLogin(getActivity())) {
                    IntentManager.toLoginSelectActivity(getActivity(), IntentManager.LOGIN_SUCC_ACTION_MAINACTIVITY);
                    return;
                }
                if (PreferencesUtils.getString(getActivity(), MLProperties.PREFER_KEY_USER_ID) == null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.putExtra("laiyuan", "bind");
                    startActivity(intent);
                    return;
                }

                MobclickAgent.onEvent(getActivity(), "doc_buy");
                initPopViewPayDetail(item.getArticleID(), item.getAgreeNumber());
            } else {
                MobclickAgent.onEvent(getActivity(), "doc_look");
                String resID = item.getArticleID();
                String resName = item.getTitle();
                String previewUrl = item.getArticlePath();
                String downloadUrl = item.getArticlePicture();
                String resType = item.getPreviewType();  // 目前先根据这个类型来判断是否是视频
                if ("2".equals(resType)) {

                    Intent intent = new Intent(getActivity(), PlayerActivity.class);
                    intent.putExtra("type", "DoukeFragment");
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_ID, resID);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_NAME, resName);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_PREVIEW_URL, previewUrl);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), ResReviewActivity.class);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_ID, resID);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_NAME, resName);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_PREVIEW_URL, previewUrl);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_DOWNLOAD_URL, downloadUrl);
                    startActivity(intent);
                }
            }
        } else if (item.getShowType() == ArticleInfo.SHOW_TYPE_TEACHER) {
            Intent intent = new Intent(getActivity(), FamousTeacherDetailActivity.class);
            intent.putExtra("type","mingshi");
            intent.putExtra(MLProperties.BUNDLE_KEY_ZHUANJIA_ID, item.getArticleID());
            intent.putExtra(MLProperties.BUNDLE_KEY_ZHUANJIA_NAME, item.getAuthor());
            intent.putExtra(MLProperties.BUNDLE_KEY_ZHUANJIA_TITLE, item.getTitle());
            intent.putExtra(MLProperties.BUNDLE_KEY_ZHUANJIA_IMG, item.getAuthImg());
            startActivity(intent);
        } else if (item.getShowType() == ArticleInfo.SHOW_TYPE_DOUKE) {
            String path = item.getArticlePath();
            String id = item.getArticleID();
            if (!StringUtils.isEmpty(path)) {
                Intent intent = new Intent(getActivity(), DoukeDetailActivity.class);
                intent.putExtra(MLProperties.BUNDLE_KEY_DOUKE_ID, id);
                intent.putExtra(MLProperties.BUNDLE_KEY_DOUKE_URL, path);
                //add
                intent.putExtra("title", item.getTitle());
                intent.putExtra("content", item.getContent());
                intent.putExtra("imgurl", item.getArticlePicture());
                intent.putExtra("commentnum", item.getCommentNumber());
                startActivity(intent);
            } else {
                T.showShort(getActivity(), "页面不存在");
            }
        } else if (item.getShowType() == ArticleInfo.SHOW_TYPE_LIVE) {
            // 观看直播需要先进行登录
            if (LoginStatusUtil.noLogin(getActivity())) {
                IntentManager.toLoginSelectActivity(getActivity(), IntentManager.LOGIN_SUCC_ACTION_MAINACTIVITY);
                return;
            }

            if (item.getAuthDesc().equals(MySelfInfo.getInstance().getId())) {

            } else {
                String price = item.getAgreeNumber();
                String buyType = item.getCommentNumber();

                if (!StringUtils.isEmpty(price) && !"0".equals(price) && "0".equals(buyType)) {
                    initPopViewPayDetailForLive(item.getArticleID(), item.getAuthDesc(), item.getAgreeNumber());
                } else {
                    MySelfInfo.getInstance().setIdStatus(Constants.MEMBER);
                    MySelfInfo.getInstance().setJoinRoomWay(false);
                    CurLiveInfo.setHostID(item.getAuthDesc());
                    String phone;
                    if (!StringUtils.isEmpty(item.getAuthDesc())) {
                        phone = item.getAuthDesc().substring(3);
                        if (!StringUtils.isEmpty(phone) && phone.length() > 10) {
                            phone = phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                        }
                    } else {
                        phone = "";
                    }
                    CurLiveInfo.setHostName(ShowNameUtil.getFirstNotNullParams(item.getNickname(), item.getAuthor(), phone));
                    CurLiveInfo.setHostAvator(item.getAuthImg());
                    CurLiveInfo.setRoomNum(Integer.valueOf(item.getArticleID()));
                    CurLiveInfo.setTitle(item.getTitle());
                    CurLiveInfo.setCoverurl(item.getArticlePicture());

                }
            }
        } else if (item.getShowType() == ArticleInfo.SHOW_TYPE_LATEST_RES) {
            String previewType = item.getPreviewType();
            // 如果是文章，跳转到文章详情页面
            if ("文章".equals(previewType)) {
                // 跳转到逗课页面
                Intent intent = new Intent(getActivity(), DoukeDetailActivity.class);
                intent.putExtra(MLProperties.BUNDLE_KEY_DOUKE_ID, item.getArticleID());
                intent.putExtra(MLProperties.BUNDLE_KEY_DOUKE_URL, item.getArticlePath());
                //add
                //add
                intent.putExtra("title", item.getTitle());
                intent.putExtra("content", item.getContent());
                intent.putExtra("imgurl", item.getArticlePicture());
                intent.putExtra("commentnum", item.getCommentNumber());
                startActivity(intent);
                return;
            }
            // 如果是资源，跳转到资源页面
            String price = item.getAgreeNumber();
            String buyType = item.getCommentNumber();

            if (!StringUtils.isEmpty(price) && !"0".equals(price) && "0".equals(buyType)) {
                // 如果资源不是免费，需要先登录
                if (LoginStatusUtil.noLogin(getActivity())) {
                    IntentManager.toLoginSelectActivity(getActivity(), IntentManager.LOGIN_SUCC_ACTION_MAINACTIVITY);
                    return;
                }
                if (PreferencesUtils.getString(getActivity(), MLProperties.PREFER_KEY_USER_ID) == null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.putExtra("laiyuan", "bind");
                    startActivity(intent);
                    return;
                }

                MobclickAgent.onEvent(getActivity(), "doc_buy");
                initPopViewPayDetail(item.getArticleID(), item.getAgreeNumber());
            } else {
                if ("视频".equals(previewType)) {
                    // 跳转动视频播放页面
                    String resID = item.getArticleID();
                    String resName = item.getTitle();
                    String previewUrl = item.getArticlePath();

                    Intent intent = new Intent(getActivity(), PlayerActivity.class);
                    intent.putExtra("type", "DoukeFragment");
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_ID, resID);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_NAME, resName);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_PREVIEW_URL, previewUrl);
                    startActivity(intent);
                } else {
                    // 跳转到文档页面
                    String resID = item.getArticleID();
                    String resName = item.getTitle();
                    String previewUrl = item.getArticlePath();
                    String downloadUrl = item.getAuthor();
                    Intent intent = new Intent(getActivity(), ResReviewActivity.class);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_ID, resID);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_NAME, resName);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_PREVIEW_URL, previewUrl);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_DOWNLOAD_URL, downloadUrl);

                    startActivity(intent);
                }
            }
        } else if (item.getShowType() == ArticleInfo.SHOW_TYPE_COURSE) {
            Intent intent = new Intent(getActivity(), CourseDetailActivity2.class);
            Bundle args = new Bundle();
            args.putString("CourseID", item.getArticleID());
            args.putString("CourseTitle", item.getTitle());
            args.putString("CourseLearnNum", item.getReplyCount());
            args.putString("CourseResNum", item.getReadNumber());
            args.putString("CoursePicture", item.getArticlePicture());

            args.putString("CoursePrice", item.getAgreeNumber());
            args.putString("CourseBuyStatus", item.getCommentNumber());

            //add
            args.putString("CourseJiakeStatus", item.getJiakeStatus());

            args.putString("CourseDesc", item.getAuthDesc());
            args.putString("CourseZhengshu", item.getAuthImg());
            args.putString("CourseShuoming", item.getContent());

            intent.putExtras(args);
            startActivity(intent);
        }
    }


    private void initHeaderView(View headerView) {
        banner = (Banner) headerView.findViewById(R.id.banner);
        //设置banner样式
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        //设置图片加载器
        banner.setImageLoader(new FrescoImageLoader());
        //设置图片集合
        // banner.setImages(headerImages);
        //设置banner动画效果
        banner.setBannerAnimation(Transformer.Default);
        //设置标题集合（当banner样式有显示title时）
        // banner.setBannerTitles(titles);
        //设置自动轮播，默认为true
        banner.isAutoPlay(true);
        //设置轮播时间
        banner.setDelayTime(4000);
        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.CENTER);
        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                MobclickAgent.onEvent(getActivity(), "banner_click");
                if ("zj".equals(mBannerList.get(position).getTitle())) {
                    // 跳转到专家详情页面
                    Intent intent = new Intent(getActivity(), ZhuanjiaDetailActivity.class);
                    intent.putExtra(MLProperties.BUNDLE_KEY_ZHUANJIA_ID, mBannerList.get(position).getArticleID());
                    startActivity(intent);
                } else if ("dk".equals(mBannerList.get(position).getTitle())) {
                    // 跳转到逗课页面
                    Intent intent = new Intent(getActivity(), DoukeDetailActivity.class);
                    intent.putExtra(MLProperties.BUNDLE_KEY_DOUKE_ID, mBannerList.get(position).getArticleID());
                    intent.putExtra(MLProperties.BUNDLE_KEY_DOUKE_URL, mBannerList.get(position).getArticlePath());
                    //add
                    //add
                    intent.putExtra("title", mBannerList.get(position).getTitle());
                    intent.putExtra("content", mBannerList.get(position).getContent());
                    intent.putExtra("imgurl", mBannerList.get(position).getArticlePicture());
                    intent.putExtra("commentnum", mBannerList.get(position).getCommentNumber());
                    startActivity(intent);
                } else if ("sp".equals(mBannerList.get(position).getTitle())) {
                    // 跳转动视频播放页面
                    String resID = mBannerList.get(position).getArticleID();
                    String resName = mBannerList.get(position).getContent();
                    String previewUrl = mBannerList.get(position).getArticlePath();

                    Intent intent = new Intent(getActivity(), PlayerActivity.class);
                    intent.putExtra("type", "DoukeFragment");
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_ID, resID);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_NAME, resName);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_PREVIEW_URL, previewUrl);
                    startActivity(intent);
                } else if ("huodong".equals(mBannerList.get(position).getTitle())) {
                    // 年会活动，案例投票
                    Intent intent = new Intent(getActivity(), AnnualCaseAllActivity.class);
                    intent.putExtra("huodongID", mBannerList.get(position).getArticleID());
                    startActivity(intent);
                } else if ("zy".equals(mBannerList.get(position).getTitle())) {
                    // 跳转到文档页面
                    String resID = mBannerList.get(position).getArticleID();
                    String resName = mBannerList.get(position).getContent();
                    String previewUrl = mBannerList.get(position).getArticlePath();
                    String downloadUrl = mBannerList.get(position).getAuthor();

                    Intent intent = new Intent(getActivity(), ResReviewActivity.class);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_ID, resID);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_NAME, resName);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_PREVIEW_URL, previewUrl);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_DOWNLOAD_URL, downloadUrl);
                    startActivity(intent);
                } else if ("choujiang".equals(mBannerList.get(position).getTitle())) {
                    Intent intent = new Intent(getActivity(), PrizeActivity.class);
                    startActivity(intent);

                } else if ("zhuanti".equals(mBannerList.get(position).getTitle())) {
                    Intent intent = new Intent(getActivity(), ZhuantiActivity.class);
                    intent.putExtra("id", mBannerList.get(position).getZhuanti());
                    intent.putExtra("title", mBannerList.get(position).getZhuanti_name());
                    startActivity(intent);
                    Log.e("专题名字",mBannerList.get(position).getZhuanti_name());
                } else {
                    // 其余的情况跳转到一个单纯的webView页面
                    String resName = mBannerList.get(position).getContent();
                    String previewUrl = mBannerList.get(position).getArticlePath();

                    Intent intent = new Intent(getActivity(), WebviewActivity.class);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_NAME, resName);
                    intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_PREVIEW_URL, previewUrl);
                    startActivity(intent);
                }
            }
        });
        //add by gpx
        tab1 = (TextView) headerView.findViewById(R.id.bt_course);
        tab2 = (TextView) headerView.findViewById(R.id.bt_expert);
        tab3 = (TextView) headerView.findViewById(R.id.bt_famous_teacher);
        tab4 = (TextView) headerView.findViewById(R.id.bt_school);
        tab5 = (TextView) headerView.findViewById(R.id.bt_group);
        tab1.setOnClickListener(this);
        tab2.setOnClickListener(this);
        tab3.setOnClickListener(this);
        tab4.setOnClickListener(this);
        tab5.setOnClickListener(this);

    }

    private void initData() {
        currentPage = 1;
        mDataList.clear();

        getBannerInfoFromAPI();
        getRefreshDataList();
    }

    public void getRefreshDataList() {
        if (!NetUtils.isConnected(getActivity())) {
            T.showShort(getActivity(), "无法连接到网络，请检查您的网络设置");
            cleanXRefreshView();
            return;
        }
        final LmsDataService mService = new LmsDataService();
        // 获取专家卡片列表
        Observable<List<ArticleInfo>> observable1 = Observable.create(new ObservableOnSubscribe<List<ArticleInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ArticleInfo>> e) throws Exception {
                try {
                    List<ArticleInfo> dataList = mService.getMasterCardsFromAPI(currentPage, 5);

                    if(dataList.size()>0){
                        if(!dataList.get(0).getGetmore().equals("0")){
                            int count = dataList.get(0).getCardnum();
                            dataList.add(new ArticleInfo("查看全部" + count + "位专家", ArticleInfo.SHOW_TYPE_ZHUANJIA_ALL));
                        }
                    }

                    if (!e.isDisposed()) {
                        e.onNext(dataList);
                        e.onComplete();
                    }
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io());

        // 获取干货精选列表
        Observable<List<ArticleInfo>> observable2 = Observable.create(new ObservableOnSubscribe<List<ArticleInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ArticleInfo>> e) throws Exception {
                try {
                    List<ArticleInfo> dataList = mService.getDouKeListFromAPI(currentPage, teacherPhoneNumber);
                    if (!e.isDisposed()) {
                        e.onNext(dataList);
                        e.onComplete();
                    }
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io());

        // 获取优课精选列表
        Observable<List<ArticleInfo>> observable3 = Observable.create(new ObservableOnSubscribe<List<ArticleInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ArticleInfo>> e) throws Exception {
                try {
                    List<ArticleInfo> dataList = mService.getMasterRikeFromAPI(teacherPhoneNumber, resPageSize);
                    if (!e.isDisposed()) {
                        e.onNext(dataList);
                        e.onComplete();
                    }
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io());

        // 获取每日一课列表
        Observable<List<ArticleInfo>> observable4 = Observable.create(new ObservableOnSubscribe<List<ArticleInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ArticleInfo>> e) throws Exception {
                try {
                    List<ArticleInfo> dataList = mService.getHomePageLatestRes(teacherPhoneNumber);
                    if (!e.isDisposed()) {
                        e.onNext(dataList);
                        e.onComplete();
                    }
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io());

        // 获取名师卡片列表
        Observable<List<ArticleInfo>> observable5 = Observable.create(new ObservableOnSubscribe<List<ArticleInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ArticleInfo>> e) throws Exception {
                try {
                    List<ArticleInfo> dataList = mService.getFamousTeacherCardsFromAPI(currentPage, teaPageSize);

                    if(dataList.size()>0){
                        if(!dataList.get(0).getGetmore().equals("0")){
                            int count = dataList.get(0).getCardnum();
                            dataList.add(new ArticleInfo("查看全部" + count + "位名师", ArticleInfo.SHOW_TYPE_ZHUANJIA_ALL));
                        }

                    }
                    if (!e.isDisposed()) {
                        e.onNext(dataList);
                        e.onComplete();
                    }
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io());

        // 获取名师成长课程列表
        Observable<List<ArticleInfo>> observable6 = Observable.create(new ObservableOnSubscribe<List<ArticleInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ArticleInfo>> e) throws Exception {
                try {
                    unionid = PreferencesUtils.getString(getActivity(), MLProperties.PREFER_KEY_WECHAT_UNIONID, "");
                    List<ArticleInfo> dataList = mService.getHomePageCourseList(teacherPhoneNumber, unionid, "1");

                    if(dataList.size()>0){
                        if(!dataList.get(0).getGetmore().equals("0")){
                            int count = dataList.get(0).getCardnum();
                            dataList.add(new ArticleInfo("查看全部" + count + "门课程", ArticleInfo.SHOW_TYPE_ZHUANJIA_ALL));
                        }

                    }

                    if (!e.isDisposed()) {
                        e.onNext(dataList);
                        e.onComplete();
                    }
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io());

        // 获取正在直播的列表
        Observable<List<ArticleInfo>> observable7 = Observable.create(new ObservableOnSubscribe<List<ArticleInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ArticleInfo>> e) throws Exception {
                try {
                    List<ArticleInfo> liveList = mService.getLiveListFromAPI(teacherPhoneNumber, "1", 2, 0);
                    if (!e.isDisposed()) {
                        e.onNext(liveList);
                        e.onComplete();
                    }
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io());

        Observable.zip(observable1, observable2, observable3, observable4, observable5, observable6, observable7
                , new Function7<List<ArticleInfo>, List<ArticleInfo>, List<ArticleInfo>, List<ArticleInfo>, List<ArticleInfo>, List<ArticleInfo>, List<ArticleInfo>, List<ArticleInfo>>() {
                    @Override
                    public List<ArticleInfo> apply(@NonNull List<ArticleInfo> data1, @NonNull List<ArticleInfo> data2, @NonNull List<ArticleInfo> data3, @NonNull List<ArticleInfo> data4, @NonNull List<ArticleInfo> data5, @NonNull List<ArticleInfo> data6, @NonNull List<ArticleInfo> data7) throws Exception {
                        List<ArticleInfo> dataList = new ArrayList<>();
                        if (data4.size() > 0) {
                            dataList.add(new ArticleInfo("每日一课", ArticleInfo.SHOW_TYPE_DECORATION));
                            dataList.addAll(data4);
                        }
                        if (data7.size() > 0) {
                            dataList.add(new ArticleInfo("正在直播", ArticleInfo.SHOW_TYPE_DECORATION));
                            dataList.addAll(data7);
                            dataList.add(new ArticleInfo("查看全部直播", ArticleInfo.SHOW_TYPE_ZHUANJIA_ALL));
                        }
                        if (data6.size() > 0) {
                            dataList.add(new ArticleInfo("名师成长课程", ArticleInfo.SHOW_TYPE_DECORATION));
                            dataList.addAll(data6);
                        }
                        if (data3.size() > 0) {
                            dataList.add(new ArticleInfo("优课精选", ArticleInfo.SHOW_TYPE_DECORATION));
                            dataList.addAll(data3);
                            dataList.add(new ArticleInfo("查看全部", ArticleInfo.SHOW_TYPE_ZHUANJIA_ALL));
                        }
                        if (data1.size() > 0) {
                            dataList.add(new ArticleInfo("专家驻场", ArticleInfo.SHOW_TYPE_DECORATION));
                            dataList.addAll(data1);
                        }
                        if (data5.size() > 0) {
                            dataList.add(new ArticleInfo("名师堂", ArticleInfo.SHOW_TYPE_DECORATION));
                            dataList.addAll(data5);
                        }
                        if (data2.size() > 0) {
                            dataList.add(new ArticleInfo("干货精选", ArticleInfo.SHOW_TYPE_DECORATION));
                            dataList.addAll(data2);
                        }
                        return dataList;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<ArticleInfo>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(@NonNull List<ArticleInfo> result) {
                        loadRefreshData(result);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        LL.e(e);
                        hideLoadingDialog();
                        cleanXRefreshView();
                        T.showShort(getActivity(), "服务器开小差了，请重试");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void loadRefreshData(List<ArticleInfo> result) {
        hideLoadingDialog();

        lastRefreshTime = mXRefreshView.getLastRefreshTime();
        mXRefreshView.stopRefresh();
        mDataList.clear();
        mDataList.addAll(result);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mAdapter.getAdapterItemCount() == 0) {
                    return 30;
                }

                if (mAdapter.getItemViewType(position) == DoukeListAdapter.ShowType.ITEM_TYPE_ZHUANJIA_RES.ordinal()) {
                    return columnResNum;
                } else if (mAdapter.getItemViewType(position) == DoukeListAdapter.ShowType.ITEM_TYPE_TEACHER.ordinal()) {
                    return columnTeaNum;
                } else {
                    return 30;
                }
            }
        });

        mAdapter.notifyDataSetChanged();
        if (null == mAdapter.getCustomLoadMoreView()) {
            mAdapter.setCustomLoadMoreView(new XRefreshViewFooter(getActivity()));
        }
    }

    /**
     * RecyclerView 加载更多，获取更多逗课文章
     */
    private void getDoukeList() {
        if (!NetUtils.isConnected(getActivity())) {
            T.showShort(getActivity(), "无法连接到网络，请检查您的网络设置");
            cleanXRefreshView();
            return;
        }
        Observable.create(new ObservableOnSubscribe<List<ArticleInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ArticleInfo>> e) throws Exception {
                try {
                    LmsDataService mService = new LmsDataService();
                    List<ArticleInfo> dataList = mService.getDouKeListFromAPI(currentPage, teacherPhoneNumber);


                    e.onNext(dataList);
                    e.onComplete();
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<ArticleInfo>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                        disposables.add(d);

                    }

                    @Override
                    public void onNext(@NonNull List<ArticleInfo> result) {
                        //T.showShort(getActivity(), "有新数据");

                        cleanXRefreshView();
                        loadData(result);

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        LL.e(e);
                        cleanXRefreshView();
                        T.showShort(getActivity(), "服务器开小差了，请重试");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void loadData(List<ArticleInfo> list) {
        lastRefreshTime = mXRefreshView.getLastRefreshTime();
        if (list == null || list.size() < 10) {
            mXRefreshView.setPullLoadEnable(false);
        }
        // mXRefreshView.stopLoadMore();
        // 更新数据
        mDataList.addAll(list);
        mAdapter.notifyDataSetChanged();
//        if (null == mAdapter.getCustomLoadMoreView()) {
//            mAdapter.setCustomLoadMoreView(new XRefreshViewFooter(getActivity()));
//        }
    }

    private void cleanXRefreshView() {
        mXRefreshView.stopRefresh();
        mXRefreshView.stopLoadMore();
    }

    private void getBannerInfoFromAPI() {
        Observable.create(new ObservableOnSubscribe<List<ArticleInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ArticleInfo>> e) throws Exception {
                try {
                    LmsDataService dataService = new LmsDataService();
                    List<ArticleInfo> list = dataService.getBannerInfoFromAPI();
                    if (!e.isDisposed()) {
                        e.onNext(list);
                        e.onComplete();
                    }
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<ArticleInfo>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(@NonNull List<ArticleInfo> result) {
                        loadBannerInfo(result);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.getMessage();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void loadBannerInfo(List<ArticleInfo> result) {
        mBannerList.clear();
        mBannerList.addAll(result);
        headerImages.clear();
        for (ArticleInfo item : result) {
            headerImages.add(item.getArticlePicture());
        }

        //设置图片集合
        banner.setImages(headerImages);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("主页数据", "手机号==" + PreferencesUtils.getString(getActivity(), MLProperties.PREFER_KEY_USER_ID, ""));
        phoneNumberBack = PreferencesUtils.getString(getActivity(), MLProperties.PREFER_KEY_USER_ID, "");
        if (!phoneNumberBack.equals(teacherPhoneNumber)) {
            // 前后两次手机号不同的时候刷新数据
            teacherPhoneNumber = phoneNumberBack;
            initData();
        }
        // 查询订单
        if (!StringUtils.isEmpty(currTradeID)) {
            queryTheTradeStateFromAPI(currTradeID);
        }
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        MobclickAgent.onPageStart("douke");
        if (getActivity() != null) {
            phoneNumberBack = PreferencesUtils.getString(getActivity(), MLProperties.PREFER_KEY_USER_ID, "");
            if (!phoneNumberBack.equals(teacherPhoneNumber)) {
                // 前后两次手机号不同的时候刷新数据
                teacherPhoneNumber = phoneNumberBack;
                initData();
            }
        }
    }

    @Override
    protected void onInVisible() {
        super.onInVisible();
        MobclickAgent.onPageEnd("douke");
    }

    @Override
    public void onPause() {
        super.onPause();
        // 停止轮询
        disposables.clear();
    }

    /*************************** 支付 **************************************/
    private void initPopViewPayDetail(final String masterid, final String realPrice) {
        View popView = LayoutInflater.from(getActivity()).inflate(R.layout.pop_pay_masterres_detail, null);
        popPayDetail = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popPayDetail.setAnimationStyle(R.style.MyPopupWindow_anim_style);
        popPayDetail.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popPayDetail.setFocusable(true);
        popPayDetail.setOutsideTouchable(true);
        popPayDetail.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(getActivity(), 1f);
            }
        });
        TextView tvRealPay = (TextView) popView.findViewById(R.id.tv_realPay);
        tvRealPay.setText("¥ " + (Double.parseDouble(realPrice)) / 100);
        TextView tvReportProtocol = (TextView) popView.findViewById(R.id.tv_report_protocol);
        tvReportProtocol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PayProtocolActivity.class);
                startActivity(intent);
            }
        });
        TextView tvPay = (TextView) popView.findViewById(R.id.tv_payReport);
        tvPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开始支付,隐藏支付窗口
                hidePopViewPayDetail();
                // 获取订单号
                getTheOrderFromAPI(masterid, String.valueOf(realPrice), "masterres");
                MobclickAgent.onEvent(getActivity(), "masterres_pay");
            }
        });
        // 显示页面
        showPopViewPayDetail();
    }

    private void initPopViewPayDetailForLive(final String masterid, final String sxbroomuser, final String realPrice) {
        View popView = LayoutInflater.from(getActivity()).inflate(R.layout.pop_pay_masterres_detail, null);
        popPayDetail = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popPayDetail.setAnimationStyle(R.style.MyPopupWindow_anim_style);
        popPayDetail.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popPayDetail.setFocusable(true);
        popPayDetail.setOutsideTouchable(true);
        popPayDetail.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(getActivity(), 1f);
            }
        });
        TextView tvRealPay = (TextView) popView.findViewById(R.id.tv_realPay);
        tvRealPay.setText("¥ " + (Double.parseDouble(realPrice)) / 100);
        TextView tvReportProtocol = (TextView) popView.findViewById(R.id.tv_report_protocol);
        tvReportProtocol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PayProtocolActivity.class);
                startActivity(intent);
            }
        });
        TextView tvPay = (TextView) popView.findViewById(R.id.tv_payReport);
        tvPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开始支付,隐藏支付窗口
                hidePopViewPayDetail();
                // 获取订单号
                getTheOrderFromAPIForLive(masterid, sxbroomuser, String.valueOf(realPrice), "suixinbo");
            }
        });
        // 显示页面
        showPopViewPayDetail();
    }

    private void showPopViewPayDetail() {
        if (popPayDetail != null && !popPayDetail.isShowing()) {
            setBackgroundAlpha(getActivity(), 0.5f);
            popPayDetail.showAtLocation(mRecyclerView, Gravity.BOTTOM, 0, popPayDetail.getHeight());
        }
    }

    private void hidePopViewPayDetail() {
        if (popPayDetail != null && popPayDetail.isShowing()) {
            popPayDetail.dismiss();
            popPayDetail = null;
        }
    }

    /**
     * 设置背景透明度
     *
     * @param activity
     * @param bgAlpha
     */
    public void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 生成订单
     */
    private void getTheOrderFromAPI(final String masterresid, final String price, final String payType) {
        Observable.create(new ObservableOnSubscribe<OrderInfo>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<OrderInfo> e) throws Exception {
                try {
                    LmsDataService mService = new LmsDataService();
                    OrderInfo info = mService.getTheOrderInfoFromAPI(masterresid, price, teacherPhoneNumber, unionid, payType);
                    if (!e.isDisposed()) {
                        e.onNext(info);
                    }
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<OrderInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(OrderInfo orderInfo) {
                        // 获取订单成功后 发起微信支付
                        if ("SUCCESS".equals(orderInfo.getResult_code())
                                && "SUCCESS".equals(orderInfo.getReturn_code())
                                && "OK".equals(orderInfo.getReturn_msg())) {
                            startPay(orderInfo);
                        } else {
                            // 获取订单失败
                            T.showShort(getActivity(), "订单生成失败");
                        }
                        hideLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideLoadingDialog();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void getTheOrderFromAPIForLive(final String masterresid, final String sxbroomuser, final String price, final String payType) {
        Observable.create(new ObservableOnSubscribe<OrderInfo>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<OrderInfo> e) throws Exception {
                try {
                    LmsDataService mService = new LmsDataService();
                    OrderInfo info = mService.getTheOrderInfoFromAPIForLive(masterresid, sxbroomuser, price, teacherPhoneNumber, payType);
                    if (!e.isDisposed()) {
                        e.onNext(info);
                    }
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<OrderInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(OrderInfo orderInfo) {
                        // 获取订单成功后 发起微信支付
                        if ("SUCCESS".equals(orderInfo.getResult_code())
                                && "SUCCESS".equals(orderInfo.getReturn_code())
                                && "OK".equals(orderInfo.getReturn_msg())) {
                            startPay(orderInfo);
                        } else {
                            // 获取订单失败
                            T.showShort(getActivity(), "订单生成失败");
                        }
                        hideLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideLoadingDialog();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 开始支付
     *
     * @param orderInfo
     */
    private void startPay(OrderInfo orderInfo) {
        if (api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT) {
            currTradeID = orderInfo.getOut_trade_no();  // 获取当前商户订单号
            PayReq request = new PayReq();
            request.appId = orderInfo.getAppid();
            request.partnerId = orderInfo.getMch_id();   // 商户号
            request.prepayId = orderInfo.getPrepay_id();
            request.packageValue = "Sign=WXPay";
            request.nonceStr = orderInfo.getNonce_str();
            request.timeStamp = orderInfo.getTimeStamp();
            request.extData = "app data";
            request.sign = orderInfo.getSign();
            api.sendReq(request);
        } else {
            T.showShort(getActivity(), "当前手机暂不支持该功能");
        }
    }

    /**
     * 查询订单支付状态
     *
     * @param tradeID
     */
    private void queryTheTradeStateFromAPI(final String tradeID) {
        Observable.create(new ObservableOnSubscribe<TradeInfo>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<TradeInfo> e) throws Exception {
                try {
                    LmsDataService mService = new LmsDataService();
                    TradeInfo info = mService.getTheTradeInfoFromAPI(tradeID);
                    if (!e.isDisposed()) {
                        e.onNext(info);
                    }
                } catch (InterruptedIOException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                } catch (InterruptedException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                        return;
                    }
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<TradeInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(TradeInfo tradeInfo) {
                        currTradeID = "";
                        if ("SUCCESS".equals(tradeInfo.getResult_code())) {
                            if ("SUCCESS".equals(tradeInfo.getTrade_state())) {
                                showAlertDialog("支付成功", "您现在就可以去查看完整资料啦");
                            } else if ("NOTPAY".equals(tradeInfo.getTrade_state())) {
                                showAlertDialog("支付失败", "支付遇到问题，请重试");
                            }
                        } else {
                            showAlertDialog("支付失败", "支付遇到问题，请重试");
                        }
                        // 刷新页面
                        // showLoadingDialog();
                        getRefreshDataList();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private void showAlertDialog(String title, String content) {
        TipsAlertDialog3 dialog = new TipsAlertDialog3(getActivity());
        dialog.setTitleText(title);
        dialog.setContentText(content);
        dialog.setConfirmText("好的");
        dialog.setConfirmClickListener(new TipsAlertDialog3.OnSweetClickListener() {
            @Override
            public void onClick(TipsAlertDialog3 sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 检查新版本
     */
    private void checkAppNewVersion() {
        Observable.create(new ObservableOnSubscribe<AppVersionInfo>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<AppVersionInfo> emitter) throws Exception {
                if (emitter.isDisposed()) return;
                if (getActivity() == null) emitter.onComplete();
                LmsDataService mService = new LmsDataService();
                AppVersionInfo info;
                String versionName = AppUtils.getVersionName(getActivity());
                String qudao = AppUtils.getMetaDataFromApplication(getActivity(), MLConfig.KEY_CHANNEL_NAME);
                try {
                    info = mService.checkNewVersion(versionName, qudao);
                } catch (Exception e) {
                    e.printStackTrace();
                    LL.e(e);
                    info = new AppVersionInfo();
                    info.setErrorCode("0");
                }
                if (!emitter.isDisposed()) {
                    emitter.onNext(info);
                    emitter.onComplete();
                }

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AppVersionInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(AppVersionInfo info) {
//                        Log.e("版本apk信息",info.getTitle());
//                        Log.e("版本apk信息",info.getContent());
//                        Log.e("版本apk下载地址",info.getDownloadUrl());
                        if (StringUtils.isEmpty(info.getErrorCode()) || info.getErrorCode().equals("0")) {
                            return;
                        }
                        if (info.getErrorCode().equals("1")) {
                            showNewVersionDialog(info.getTitle(), info.getContent(), info.getDownloadUrl());
                        } else if (info.getErrorCode().equals("2")) {
                            // T.showShort(getActivity(), info.getMessage());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void showNewVersionDialog(String title, String content, final String downloadUrl) {
        createUpdateAppDialog(title, content, new UpdateAPPAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(final UpdateAPPAlertDialog sweetAlertDialog) {
                // confirm
                RxPermissions rxPermissions = new RxPermissions(getActivity());
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(@NonNull Boolean success) throws Exception {
                                if (success) {
                                    startDownloadAppService(downloadUrl);
                                    sweetAlertDialog.startDownload();
                                } else {
                                    sweetAlertDialog.dismiss();
                                }
                            }
                        });
            }
        }, new UpdateAPPAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(UpdateAPPAlertDialog sweetAlertDialog) {
                // cancel
                sweetAlertDialog.dismiss();
            }
        }, new UpdateAPPAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(UpdateAPPAlertDialog sweetAlertDialog) {
                // cancel Download
                sweetAlertDialog.dismiss();
                stopDownloadAppService(downloadUrl);
            }
        });
    }

    private void startDownloadAppService(String downloadUrl) {
        Intent intent = new Intent(getActivity(), DownloadAppService.class);
        Bundle args = new Bundle();
        args.putString(MLConfig.KEY_BUNDLE_DOWNLOAD_URL, downloadUrl);
        intent.putExtras(args);
        getActivity().startService(intent);
    }

    private void stopDownloadAppService(String downloadUrl) {
        OkHttpUtils.getInstance().cancelTag(DownloadAppService.FILENAME);
    }


    /********* 在fragment中切换tab *********/

    private ChangeBottomTabListener bottomTabListener;

    public void setBottomTabListener(ChangeBottomTabListener bottomTabListener) {
        this.bottomTabListener = bottomTabListener;
    }

    /**
     * 复现Rxjava出现RxCachedThreadScheduler-n导致app崩溃的问题
     */
    private void testRxJavaError() {
        final Disposable disposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; i < 10; i++) {
                    e.onNext(i);
                    Thread.sleep(1000);
                }
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                // throw new Exception("第 " + integer + " 秒");
                return "第 " + integer + " 秒";
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LL.i("testrxjava", s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LL.i("testrxjava", "onError(): " + throwable.getMessage());
                    }
                });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                disposable.dispose();
                if (disposable.isDisposed()) {
                    LL.i("testrxjava", "已经取消订阅");
                } else {
                    LL.i("testrxjava", "没有取消订阅");
                }
            }
        }, 8000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void udateUI(MsgEvent event) {
        if (event.getAction().equals("courseStudyBuySuccess")) {
            currentPage = 1;
            mDataList.clear();

            getBannerInfoFromAPI();
            getRefreshDataList();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_course:
                Intent intent1 = new Intent(getActivity(), CourseAllActivity.class);
                startActivity(intent1);
                break;
            case R.id.bt_expert:
                Intent intent2 = new Intent(getActivity(), MasterAllActivity.class);
                startActivity(intent2);

                break;
            case R.id.bt_famous_teacher:
                Intent intent3 = new Intent(getActivity(), FamousTeacherAllActivity.class);
                startActivity(intent3);
                break;
            case R.id.bt_school:
                Intent intent4 = new Intent(getActivity(), SchoolAllActivity.class);
                startActivity(intent4);
                break;
            case R.id.bt_group:
                Intent intent5 = new Intent(getActivity(), GroupAllActivity.class);
                intent5.putExtra("type","DoukeFragment");
                startActivity(intent5);
                break;
        }
    }

}
