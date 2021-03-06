package com.bj.eduteacher.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.bj.eduteacher.R;
import com.bj.eduteacher.login.view.LoginActivity;
import com.bj.eduteacher.login.view.LoginSelectActivity;

/**
 * Created by zz379 on 2017/9/30.
 */

public class IntentManager {

    public static final String LOGIN_SUCC_ACTION_MAINACTIVITY = "0";
    public static final String LOGIN_SUCC_ACTION_FINISHSELF = "1";


    public static void toLoginSelectActivity(Context context, String loginSuccAction) {
        Intent intent = new Intent(context, LoginSelectActivity.class);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(R.anim.act_bottom_top_in, R.anim.act_alpha_out);
    }
}
