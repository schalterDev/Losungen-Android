package schalter.dev.customizelibrary;

/**
 * Created by martin on 16.04.16.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomToolbar extends Toolbar {

    public CustomToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomToolbar(Context context) {
        super(context);
    }

    int itemColor;
    Context context;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        itemColor = Colors.getColor(getContext(), Colors.ICONS_TOOLBAR);
        colorizeToolbar(this, itemColor, (Activity) context);

        if(context instanceof Activity)
            Colors.setStatusBarColor((Activity) context, Colors.PRIMARY_DARK);
    }

    public void setItemColor(int color) {
        itemColor = color;
        colorizeToolbar(this, itemColor, (Activity) context);
    }


    /**
     * Use this method to colorize toolbar icons to the desired target color
     *
     * @param toolbarView       toolbar view being colored
     * @param toolbarIconsColor the target color of toolbar icons
     * @param activity          reference to activity needed to register observers
     */
    public static void colorizeToolbar(Toolbar toolbarView, int toolbarIconsColor, Activity activity) {
        final PorterDuffColorFilter colorFilter
                = new PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.SRC_IN);

        for (int i = 0; i < toolbarView.getChildCount(); i++) {
            final View v = toolbarView.getChildAt(i);

            doColorizing(v, colorFilter, toolbarIconsColor);
        }

        toolbarView.setTitleTextColor(toolbarIconsColor);
        toolbarView.setSubtitleTextColor(toolbarIconsColor);
    }

    public static void doColorizing(View v, final ColorFilter colorFilter, int toolbarIconsColor) {
        if (v instanceof ImageButton) {
            ((ImageButton) v).getDrawable().setAlpha(255);
            ((ImageButton) v).getDrawable().setColorFilter(colorFilter);
        }

        if (v instanceof ImageView) {
            ((ImageView) v).getDrawable().setAlpha(255);
            ((ImageView) v).getDrawable().setColorFilter(colorFilter);
        }

        if (v instanceof AutoCompleteTextView) {
            ((AutoCompleteTextView) v).setTextColor(toolbarIconsColor);
        }

        if (v instanceof TextView) {
            ((TextView) v).setTextColor(toolbarIconsColor);
        }

        if (v instanceof EditText) {
            ((EditText) v).setTextColor(toolbarIconsColor);
        }

        if (v instanceof ViewGroup) {
            for (int lli = 0; lli < ((ViewGroup) v).getChildCount(); lli++) {
                doColorizing(((ViewGroup) v).getChildAt(lli), colorFilter, toolbarIconsColor);
            }
        }

        if (v instanceof ActionMenuView) {
            for (int j = 0; j < ((ActionMenuView) v).getChildCount(); j++) {

                //Changing the color of any ActionMenuViews - icons that
                //are not back button, nor text, nor overflow menu icon.
                final View innerView = ((ActionMenuView) v).getChildAt(j);

                if (innerView instanceof ActionMenuItemView) {
                    int drawablesCount = ((ActionMenuItemView) innerView).getCompoundDrawables().length;
                    for (int k = 0; k < drawablesCount; k++) {
                        if (((ActionMenuItemView) innerView).getCompoundDrawables()[k] != null) {
                            final int finalK = k;

                            //Important to set the color filter in seperate thread,
                            //by adding it to the message queue
                            //Won't work otherwise.
                            //Works fine for my case but needs more testing

                            ((ActionMenuItemView) innerView).getCompoundDrawables()[finalK].setColorFilter(colorFilter);

//                              innerView.post(new Runnable() {
//                                  @Override
//                                  public void run() {
//                                      ((ActionMenuItemView) innerView).getCompoundDrawables()[finalK].setColorFilter(colorFilter);
//                                  }
//                              });
                        }
                    }
                }
            }
        }
    }

}