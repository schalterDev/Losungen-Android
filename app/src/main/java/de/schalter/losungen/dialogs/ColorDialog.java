package de.schalter.losungen.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.OnColorListener;
import com.larswerkman.lobsterpicker.sliders.LobsterOpacitySlider;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;

import de.schalter.losungen.R;

/**
 * Created by marti on 28.11.2015.
 */
public class ColorDialog {

    private int colorBefore;
    private AlertDialog dialog;
    private LobsterOpacitySlider opacitySlider;

    private Context context;

    private int color;

    /**
     * @param color the color before
     * @param listener what happens when the color is selected
     */
    public ColorDialog(Context context, int color, DialogInterface.OnClickListener listener) {
        colorBefore = color;
        this.color = color;
        this.context = context;
        init(listener);
    }

    /**
     * set up the color picker with transparent chooser and
     * shade chooser
     * @param listener what happens when the color is selected
     */
    private void init(DialogInterface.OnClickListener listener) {
        LayoutInflater li = LayoutInflater.from(context);
        View dialogView = li.inflate(R.layout.dialog_color, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set dialog_import.xml to alertdialog builder
        alertDialogBuilder.setView(dialogView);

        LobsterPicker lobsterPicker = (LobsterPicker) dialogView.findViewById(R.id.lobsterpicker);
        opacitySlider = (LobsterOpacitySlider) dialogView.findViewById(R.id.opacityslider);
        LobsterShadeSlider shadeSlider = (LobsterShadeSlider) dialogView.findViewById(R.id.shadeslider);

        //To enable to color feedback use
        lobsterPicker.setColorHistoryEnabled(true);
        lobsterPicker.addDecorator(shadeSlider);
        shadeSlider.addDecorator(opacitySlider);

        shadeSlider.addOnColorListener(new OnColorListener() {
            @Override
            public void onColorChanged(int color) {

            }

            @Override
            public void onColorSelected(int color) {
                ColorDialog.this.color = color;
            }
        });

        //To set a previous picked color or reference color use
        lobsterPicker.setHistory(colorBefore);
        lobsterPicker.addOnColorListener(new OnColorListener() {
            @Override
            public void onColorChanged(int color) {

            }

            @Override
            public void onColorSelected(int color) {
                ColorDialog.this.color = color;
            }
        });

        alertDialogBuilder.setPositiveButton(R.string.accept, listener);

        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog = alertDialogBuilder.create();
    }

    /**
     * @return the selected color from the dialog
     */
    public int getColor() {
        color = opacitySlider.getColor();
        return color;
    }

    public void show() {
        dialog.show();
    }
}
