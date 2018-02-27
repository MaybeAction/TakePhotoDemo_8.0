package com.maybe.commonlib;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * 作者：fangluxin  on 2018/2/11 0011 14:20
 * 邮箱：flx_coding@163.com
 * 公司：南京艾思优信息科技有限公司
 */
public class UIUtils {

    /**
     * 得到resources对象
     *
     * @return
     */
    public static Resources getResource(Context ctx) {
        return ctx.getResources();
    }


    /**
     * 得到string.xml中的字符串
     *
     * @param resId
     * @return
     */
    public static String getString(int resId,Context ctx) {
        return getResource(ctx).getString(resId);
    }

    /**
     * 得到string.xml中的字符串，带点位符
     */
    public static String getString(int id, Context ctx, Object... formatArgs) {
        return getResource(ctx).getString(id, formatArgs);
    }

    /**
     * 得到string.xml中和字符串数组
     *
     * @param resId
     * @return
     */
    public static String[] getStringArr(int resId,Context ctx) {
        return getResource(ctx).getStringArray(resId);
    }

    /**
     * 得到colors.xml中的颜色
     *
     * @param colorId
     * @return
     */
    public static int getColor(int colorId,Context ctx) {
        return getResource(ctx).getColor(colorId);
    }

    /**
     * 得到应用程序的包名
     *
     * @return
     */
    public static String getPackageName(Context ctx) {
        return ctx.getPackageName();
    }


    /**
     * dip-->px
     */
    public static int dip2Px(int dip,Context ctx) {
        // px/dip = density;
        // density = dpi/160
        // 320*480 density = 1 1px = 1dp
        // 1280*720 density = 2 2px = 1dp

        float density = getResource(ctx).getDisplayMetrics().density;
        int px = (int) (dip * density + 0.5f);
        return px;
    }

    /**
     * px-->dip
     */
    public static int px2dip(int px,Context ctx) {

        float density = getResource(ctx).getDisplayMetrics().density;
        int dip = (int) (px / density + 0.5f);
        return dip;
    }

    /**收起软键盘*/
    public static void hideInput(View view,Context ctx){
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0) ;
    }

    public static void showInput(View view,Context ctx){
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view,0);
    }

    /**获取屏幕高度*/
    public static int getScreenHeight(Context ctx){
        DisplayMetrics dm = new DisplayMetrics();
        //取得窗口属性
        WindowManager  wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);

        //窗口的宽度
        return  dm.heightPixels;
    }

    /**获取屏幕宽度*/
    public static int getScreenWidth(Context ctx){
        DisplayMetrics dm = new DisplayMetrics();
        //取得窗口属性
        WindowManager  wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);

        //窗口的宽度
        return  dm.widthPixels;
    }
}
