package schalter.dev.customizelibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.TypedValue;

/**
 * Created by martin on 16.04.16.
 */
public abstract class Colors {

    public static final int BLUE = 0;
    public static final int YELLOW = 1;
    public static final int GREEN = 2;
    public static final int RED = 3;
    public static final int DARK = 4;
    public static final int LIGHT = 5;

    public static final int PRIMARY = R.attr.colorPrimary;
    public static final int PRIMARYDARK = R.attr.colorPrimaryDark;
    public static final int PRIMARYLIGT = R.attr.colorPrimaryLight;
    public static final int ACCENT = R.attr.colorAccent;
    public static final int FONT = R.attr.colorFont;
    public static final int FONTDISABLED = R.attr.colorFontDisabled;
    public static final int FONTSECOND = R.attr.colorSecondFont;
    public static final int TOOLBARICON = R.attr.colorIconsToolbar;
    public static final int BACKGROUND = R.attr.colorBackground;
    public static final int BACKGROUNDSECOND = R.attr.colorSecondBackground;
    public static final int BACKGROUNDWINDOWS = R.attr.colorWindowsBackground;
    public static final int INDICATOR = R.attr.colorAccent;

    public static int getColor(Context context, int attrColor) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attrColor, typedValue, true);
        int colorFont = typedValue.data;

        return colorFont;
    }

    public static int getTheme(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        int settingColor = settings.getInt(DesignPref.TAGSETTING, 0);
        switch (settingColor) {
            case BLUE:
                //Blue
                return R.style.AppTheme_Blue;
            case YELLOW:
                //Yellow
                return R.style.AppTheme_Yellow;
            case GREEN:
                //Green
                return R.style.AppTheme_Green;
            case RED:
                //Red
                return R.style.AppTheme_Red;
            case DARK:
                //Dark
                return R.style.AppTheme_Dark;
            case LIGHT:
                //Light
                return R.style.AppTheme_Light;
        }

        return R.style.AppTheme_Blue;
    }
}
