package com.bj.eduteacher.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.bj.eduteacher.BaseActivity;
import com.bj.eduteacher.R;
import com.bj.eduteacher.adapter.DoukeNewDetailAdapter;
import com.bj.eduteacher.api.HttpUtilService;
import com.bj.eduteacher.api.LmsDataService;
import com.bj.eduteacher.api.MLProperties;
import com.bj.eduteacher.dialog.TipsAlertDialog3;
import com.bj.eduteacher.entity.ArticleInfo;
import com.bj.eduteacher.entity.OrderInfo;
import com.bj.eduteacher.entity.TradeInfo;
import com.bj.eduteacher.manager.IntentManager;
import com.bj.eduteacher.manager.ShareHelp;
import com.bj.eduteacher.utils.LL;
import com.bj.eduteacher.utils.NetUtils;
import com.bj.eduteacher.utils.PreferencesUtils;
import com.bj.eduteacher.utils.StringUtils;
import com.bj.eduteacher.utils.T;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;

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
 * Created by zz379 on 2017/3/8.
 * 案例详情页面
 */

public class AnnualCaseDetailActivity extends BaseActivity {

    @BindView(R.id.header_img_back)
    ImageView imgBack;
    @BindView(R.id.header_tv_title)
    TextView tvTitle;
    @BindView(R.id.mXRefreshView)
    XRefreshView mXRefreshView;
    @BindView(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_pay)
    TextView tvPay;

    private DoukeNewDetailAdapter mAdapter;
    private int currentPage = 1;
    public static long lastRefreshTime;
    private List<ArticleInfo> mDataList = new ArrayList<>();
    private String title;
    private String doukeID;

    private String teacherPhoneNumber;

    // 分享
    private IWXAPI api;
    private PopupWindow popPayDetail;
    private boolean isUserPaySuccess = false;  // 是否支付成功
    private String currTradeID = "";     // 当前商户订单号

    private boolean supportStatus = false;
    private long currMillis = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annual_case_detail);
        ButterKnife.bind(this);
        api = WXAPIFactory.createWXAPI(this, MLProperties.APP_DOUHAO_TEACHER_ID);

        initToolBar();
        initView();
    }

    private void initToolBar() {
        title = getIntent().getExtras().getString("Title", "");
        doukeID = getIntent().getExtras().getString("ID", "");

        tvTitle.setText(title);
        if (supportStatus) {
            tvPay.setText("已投票（2,235）");
            tvPay.setBackgroundColor(Color.parseColor("#A3A3A3"));
        } else {
            tvPay.setText("投票（2,234）");
            tvPay.setBackgroundColor(Color.parseColor("#FC6345"));
        }
    }

    private void initView() {
        // mXRefreshView.setBackgroundResource(R.color.bg_gray);
        mRecyclerView.setBackground(null);

        // 初始化下拉刷新控件
        mRecyclerView.setHasFixedSize(true);
        // look as listview
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // set Adatper
        mAdapter = new DoukeNewDetailAdapter(mDataList);
        mAdapter.setOnMyItemClickListener(new DoukeNewDetailAdapter.OnMyItemClickListener() {
            @Override
            public void onZhuanjiaClick(View view, int position) {
                ArticleInfo item = mDataList.get(position);
                String price = item.getAgreeNumber();
                String buyType = item.getCommentNumber();
                if (!"0".equals(price) && "0".equals(buyType)) {
                    // 购买资源之前需要先进行登录
                    if (StringUtils.isEmpty(teacherPhoneNumber)) {
                        IntentManager.toLoginActivity(AnnualCaseDetailActivity.this, IntentManager.LOGIN_SUCC_ACTION_FINISHSELF);
                        return;
                    }
                    MobclickAgent.onEvent(AnnualCaseDetailActivity.this, "doc_buy");
                    initPopViewPayDetail(item.getArticleID(), item.getAgreeNumber());
                } else {
                    MobclickAgent.onEvent(AnnualCaseDetailActivity.this, "doc_look");
                    String resID = mDataList.get(position).getArticleID();
                    String resName = mDataList.get(position).getTitle();
                    String previewUrl = mDataList.get(position).getArticlePath();
                    String downloadUrl = mDataList.get(position).getArticlePicture();
                    String resType = item.getPreviewType();  // 目前先根据这个类型来判断是否是视频
                    if ("2".equals(resType)) {
                        Intent intent = new Intent(AnnualCaseDetailActivity.this, ResPlayActivity.class);
                        intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_ID, resID);
                        intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_NAME, resName);
                        intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_PREVIEW_URL, previewUrl);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(AnnualCaseDetailActivity.this, ResReviewActivity.class);
                        intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_ID, resID);
                        intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_NAME, resName);
                        intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_PREVIEW_URL, previewUrl);
                        intent.putExtra(MLProperties.BUNDLE_KEY_MASTER_RES_DOWNLOAD_URL, downloadUrl);
                        startActivity(intent);
                    }
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        // set xRefreshView
        mXRefreshView.setPullRefreshEnable(true);
        mXRefreshView.setPullLoadEnable(false);
        mXRefreshView.restoreLastRefreshTime(lastRefreshTime);
        mXRefreshView.setAutoRefresh(false);
        mXRefreshView.setAutoLoadMore(false);

        mXRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean success) {
                LL.i("刷新数据");
                currentPage = 1;
                mXRefreshView.setAutoLoadMore(true);
                mXRefreshView.setPullLoadEnable(true);
                getMasterDataFromAPI();
            }
        });
    }

    private void initData() {
        teacherPhoneNumber = PreferencesUtils.getString(this, MLProperties.PREFER_KEY_USER_ID, "");

        currentPage = 1;
        getMasterDataFromAPI();
    }

    @OnClick(R.id.header_ll_left)
    void actionBackClick() {
        this.finish();
    }

    private void getMasterDataFromAPI() {
        if (!NetUtils.isConnected(this)) {
            T.showShort(this, "无法连接到网络，请检查您的网络设置");
            cleanXRefreshView();
            return;
        }
        Observable.create(new ObservableOnSubscribe<List<ArticleInfo>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ArticleInfo>> e) throws Exception {
                LmsDataService mService = new LmsDataService();
                // SystemClock.sleep(1000);
                List<ArticleInfo> dataList = mService.getMasterResFromAPI(doukeID, teacherPhoneNumber, currentPage);
                e.onNext(dataList);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<ArticleInfo>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<ArticleInfo> articleInfos) {
                        hideLoadingDialog();
                        loadData(articleInfos);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        LL.e(e);
                        cleanXRefreshView();
                        hideLoadingDialog();
                        T.showShort(AnnualCaseDetailActivity.this, "服务器开小差了，请重试");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void cleanXRefreshView() {
        mXRefreshView.stopRefresh();
        mXRefreshView.stopLoadMore();
    }

    private void loadData(List<ArticleInfo> list) {
        lastRefreshTime = mXRefreshView.getLastRefreshTime();
        mXRefreshView.stopRefresh();
        mDataList.clear();
        // 更新数据
        mDataList.addAll(list);
        mAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.tv_share)
    void showShareDialog() {
        View popViewShare = LayoutInflater.from(this).inflate(R.layout.alert_share_annual_case, null);
        SimpleDraweeView ivPicture = (SimpleDraweeView) popViewShare.findViewById(R.id.iv_picture);
        TextView tvName = (TextView) popViewShare.findViewById(R.id.tv_name);
        TextView tvAuthor = (TextView) popViewShare.findViewById(R.id.tv_author);
        ivPicture.setImageURI(HttpUtilService.BASE_RESOURCE_URL + "46d99fc38ea7e2b6ff35a3b583a1d0f1.png");
        tvName.setText("作品名称：小学数学加减法");
        tvAuthor.setText("主讲人：张一山，上海市");
        ShareHelp.getInstance().showShareDialog(this, popViewShare);
    }

    @OnClick(R.id.tv_pay)
    void clickPayCourse() {
        if (StringUtils.isEmpty(teacherPhoneNumber)) {
            IntentManager.toLoginActivity(this, IntentManager.LOGIN_SUCC_ACTION_FINISHSELF);
            return;
        }
        if (System.currentTimeMillis() - currMillis > 1000) {
            currMillis = System.currentTimeMillis();
            if (supportStatus) {
                supportStatus = false;
                tvPay.setText("投票（2,234）");
                tvPay.setBackgroundColor(Color.parseColor("#FC6345"));
            } else {
                supportStatus = true;
                T.showShort(this, "亲，投票成功！");
                tvPay.setText("已投票（2,235）");
                tvPay.setBackgroundColor(Color.parseColor("#A3A3A3"));
            }
        }
    }

    /*************************** 支付 **************************************/
    private void initPopViewPayDetail(final String masterid, final String realPrice) {
        View popView = LayoutInflater.from(this).inflate(R.layout.pop_pay_masterres_detail, null);
        popPayDetail = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popPayDetail.setAnimationStyle(R.style.MyPopupWindow_anim_style);
        popPayDetail.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popPayDetail.setFocusable(true);
        popPayDetail.setOutsideTouchable(true);
        popPayDetail.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(AnnualCaseDetailActivity.this, 1f);
            }
        });
        TextView tvRealPay = (TextView) popView.findViewById(R.id.tv_realPay);
        tvRealPay.setText("¥ " + (Double.parseDouble(realPrice)) / 100);
        TextView tvReportProtocol = (TextView) popView.findViewById(R.id.tv_report_protocol);
        tvReportProtocol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AnnualCaseDetailActivity.this, PayProtocolActivity.class);
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
                MobclickAgent.onEvent(AnnualCaseDetailActivity.this, "masterres_pay");
            }
        });
        // 显示页面
        showPopViewPayDetail();
    }

    private void showPopViewPayDetail() {
        if (popPayDetail != null && !popPayDetail.isShowing()) {
            setBackgroundAlpha(this, 0.5f);
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
                LmsDataService mService = new LmsDataService();
                OrderInfo info = mService.getTheOrderInfoFromAPI(masterresid, price, teacherPhoneNumber, payType);
                e.onNext(info);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<OrderInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        showLoadingDialog();
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
                            T.showShort(AnnualCaseDetailActivity.this, "订单生成失败");
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
        if (api.getWXAppSupportAPI() >= com.tencent.mm.opensdk.constants.Build.PAY_SUPPORTED_SDK_INT) {
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
            T.showShort(this, "当前手机暂不支持该功能");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("douke_new");
        MobclickAgent.onResume(this);
        initData();
        if (!StringUtils.isEmpty(currTradeID)) {
            queryTheTradeStateFromAPI(currTradeID);
        } else {
            getMasterDataFromAPI();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("douke_new");
        MobclickAgent.onPause(this);
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
                LmsDataService mService = new LmsDataService();
                TradeInfo info = mService.getTheTradeInfoFromAPI(tradeID);
                e.onNext(info);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<TradeInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {

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
                        showLoadingDialog();
                        getMasterDataFromAPI();
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
        TipsAlertDialog3 dialog = new TipsAlertDialog3(this);
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
}
