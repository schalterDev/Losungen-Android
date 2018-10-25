package schalter.dev.customizelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by martin on 16.04.16.
 */
public abstract class Colors {

    private static final int BLUE = 0;
    private static final int YELLOW = 1;
    private static final int GREEN = 2;
    private static final int RED = 3;
    private static final int DARK = 4;
    private static final int LIGHT = 5;

    public static final int PRIMARY = R.attr.colorPrimary;
    public static final int PRIMARY_DARK = R.attr.colorPrimaryDark;
    public static final int PRIMARY_LIGHT = R.attr.colorPrimaryLight;
    public static final int ACCENT = R.attr.colorAccent;
    public static final int FONT = R.attr.colorFont;
    public static final int FONT_DISABLED = R.attr.colorFontDisabled;
    public static final int SECOND_FONT = R.attr.colorSecondFont;
    public static final int ICONS_TOOLBAR = R.attr.colorIconsToolbar;
    public static final int BACKGROUND = R.attr.colorBackground;
    public static final int SECOND_BACKGROUND = R.attr.colorSecondBackground;
    public static final int WINDOWS_BACKGROUND = R.attr.colorWindowsBackground;

    public static int getColor(Context context, int attrColor) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attrColor, typedValue, true);

        return typedValue.data;
    }

    public static int getTheme(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        int settingColor = settings.getInt(DesignPref.TAGSETTING, 0);
        switch (settingColor) {
            case BLUE:
                return R.style.AppTheme_Blue;
            case YELLOW:
                return R.style.AppTheme_Yellow;
            case GREEN:
                return R.style.AppTheme_Green;
            case RED:
                return R.style.AppTheme_Red;
            case DARK:
                return R.style.AppTheme_Dark;
            case LIGHT:
                return R.style.AppTheme_Light;
            default:
                return R.style.AppTheme_Blue;
        }
    }

    public static void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getColor(activity, color));
        }

    }
}
