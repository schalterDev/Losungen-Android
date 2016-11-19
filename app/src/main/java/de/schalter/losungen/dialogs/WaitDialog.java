package de.schalter.losungen.dialogs;

import android.content.Context;
import android.support.v7.app.AlertDialog;

/**
 * Created by martin on 15.11.16.
 */

public class WaitDialog {

    private AlertDialog wait;

    public WaitDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setMessage(message);

        wait = builder.create();
    }

    public WaitDialog(Context context, int titleResourceID, int messageResourceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleResourceID);
        builder.setCancelable(false);
        builder.setMessage(messageResourceId);

        wait = builder.create();
    }

    public void show() {
        wait.show();
    }

    public void close() {
        wait.cancel();
    }
}
