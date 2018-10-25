package de.schalter.losungen.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.schalter.losungen.R;
import de.schalter.losungen.files.Files;
import de.schalter.losungen.services.DownloadTask;
import de.schalter.losungen.settings.Tags;

/**
 * Created by martin on 14.11.16.
 */

public class LosungenDownloadDialog {

    private Context context;
    private int year;
    private String language;
    private Runnable onNegative;

    public LosungenDownloadDialog(Context context, int year, String language, Runnable onNegative) {
        this.year = year;
        this.language = language;
        this.context = context;
        this.onNegative = onNegative;
    }

    public void openDialog() {
        // get download_losung_dialog.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View dialogView = li.inflate(R.layout.download_losung_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        TextView titleTextView = dialogView.findViewById(R.id.losungen_download_subtitle);
        titleTextView.setText(R.string.copyright_losungen);

        final EditText urlEditText = dialogView.findViewById(R.id.edit_text_url_download);
        urlEditText.setText(Tags.getUrlLosung(year));

        // set downlaod_losung_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(dialogView);

        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onNegative.run();
            }
        });

        alertDialogBuilder.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                final WaitDialog waitDialog = new WaitDialog(context, R.string.download_starting, R.string.wait_please);
                waitDialog.show();

                Thread backgroundDownload = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Download file and save it
                        final DownloadTask downloadTask = new DownloadTask(context, urlEditText.getText().toString(), "downloads_xml", language + year, false, R.string.download_losung, R.string.xml_downloading);
                        downloadTask.onFinishedListener(new Runnable() {
                            @Override
                            public void run() {
                                //Download finished
                                String pathZipFile = downloadTask.getAbsolutePath();
                                try {
                                    String pathExtractedFile = Files.getInternalCacheDirectory(context) + "/extracted_xml/" + language + year;
                                    unzip(new File(pathZipFile) , new File(pathExtractedFile));

                                    //File-Name in directory is "Losungen Free year.xml"
                                    String pathToXml = pathExtractedFile + "/Losungen Free " + year + ".xml";

                                    //Save the path to the file in SharedPreferences: year_language_xml -> path
                                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putString(year + "_" + language + "_xml", pathToXml);
                                    editor.apply();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                waitDialog.close();
                            }
                        });

                        downloadTask.execute();
                    }
                });
                backgroundDownload.start();
            }
        });

        alertDialogBuilder.show();

    }

    /**
     * Extract a zip file and saves it in the storage
     * @param zipFile path to the zipFile
     * @param targetDirectory path to the target directory (will be created if not exists)
     */
    private static void unzip(File zipFile, File targetDirectory) throws IOException {
        //Create directory for targetDirectory
        targetDirectory.mkdirs();

        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry zipEntry;
            int count;
            byte[] buffer = new byte[8192];

            while ((zipEntry = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, zipEntry.getName());
                File dir = zipEntry.isDirectory() ? file : file.getParentFile();

                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());

                if (zipEntry.isDirectory())
                    continue;

                FileOutputStream fileOutputStream = new FileOutputStream(file);

                try {
                    while ((count = zis.read(buffer)) != -1)
                        fileOutputStream.write(buffer, 0, count);
                } finally {
                    fileOutputStream.close();
                }
            /* if time should be restored as well
            long time = zipEntry.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            zis.close();
        }
    }

}
