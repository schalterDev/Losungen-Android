package de.schalter.losungen.files;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Smarti on 09.01.2016.
 */
public class Files {

    public static String writeToInternalStorage(Context context, InputStream input, String folder, String fileName) throws IOException {
        File directory = new File(context.getFilesDir(), folder);
        if (!directory.mkdirs()) {

        }

        return writeToInternalStorage(context, input, fileName);
    }

    public static String writeToInternalStorage(Context context, InputStream input, String fileName) throws IOException {
        File file = new File(context.getFilesDir(), fileName);
        OutputStream output = new FileOutputStream(fileName);
        byte data[] = new byte[1024];

        long total = 0;
        int count;

        while ((count = input.read(data)) != -1) {
            total += count;
            output.write(data, 0, count);
        }

        output.flush();
        output.close();
        input.close();

        return file.getAbsolutePath();
    }

    public static String writeToExternalPrivateStorage(InputStream input, String fileName, String folder) throws IOException {
        if(isExternalStorageWritable()) {
            File file = new File(Environment.getExternalStorageDirectory() + "/" + folder, fileName);

            File directory = new File(Environment.getExternalStorageDirectory(), folder);

            if (!directory.mkdirs()) {

            }

            OutputStream output = new FileOutputStream(fileName);
            byte data[] = new byte[1024];

            int count;

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    public static String writeToExternalStorage(String environmentDirectory, InputStream input, String fileName, String folder) throws IOException {
        if(isExternalStorageWritable()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    environmentDirectory) + "/" + folder, fileName);

            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    environmentDirectory), folder);

            if (!directory.mkdirs()) {

            }

            OutputStream output = new FileOutputStream(fileName);
            byte data[] = new byte[1024];

            int count;

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
