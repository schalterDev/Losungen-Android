package de.schalter.losungen.files;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Smarti on 09.01.2016.
 */
public class Files {

    private Runnable onProgress;
    private int lenghtOfFile = -1;

    private int progress;

    /**
     * Write to the very private storage that no other app
     * or user can read
     * @param context Application context
     * @param input Inputstream to write
     * @param folder in which folder this file should be saved
     *               (its possible to write firstFolder/secondFolder)
     * @param fileName the name of the file (with extension)
     * @return absolute Path of the file
     * @throws IOException
     */
    @NonNull
    public String writeToPrivateStorage(Context context, InputStream input,
                                               String folder, String fileName) throws IOException {

        File directory = new File(context.getFilesDir(), folder);
        directory.mkdirs();

        return writeToPrivateStorage(context, input, fileName);
    }

    /**
     * Write to the very private storage that no other app
     * or user can read.
     * The file will not be saved in a specific folder. If you want this please use
     * writeToPrivateStorage(Context, InputStream, String, String)
     * @param context Application context
     * @param input inputstream to write
     * @param fileName the name of the file (with extension)
     * @return absolute path
     * @throws IOException
     */
    @NonNull
    public String writeToPrivateStorage(Context context, InputStream input,
                                               String fileName) throws IOException {

        File file = new File(context.getFilesDir(), fileName);

        write(file, input);

        return file.getAbsolutePath();
    }

    /**
     * Save the file on the first external storage. The folder is private
     * and not indexed for media but readable by every application with
     * read external storage permission
     * @param input inputstream to write
     * @param fileName name of the file (with extension)
     * @param folder in which folder this file should be saved
     *               (its possible to write firstFolder/second
     * @return absolute path or null if its not possible to write to external folder
     * @throws IOException
     */
    public String writeToExternalPrivateStorage(InputStream input, String folder,
                                                       String fileName) throws IOException {
        if(isExternalStorageWritable()) {
            File directory = new File(Environment.getExternalStorageDirectory(), folder);
            directory.mkdirs();

            File file = new File(directory, fileName);

            write(file, input);

            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Returns the external cache directory. There can be more than one if a sd-cards exists
     * it always selects the cache on the sd-card if possible
     * @param context application context
     * @return returns the absolute path of the directory and null if no cache directory exists
     */
    public static String getExternalCacheDirectory(Context context) {
        //External path is internal SD-Card or external SD-Card (if internal is not available)
        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        //All external directions (app specific)
        File[] filesCacheDir = ContextCompat.getExternalCacheDirs(context);

        File directory = null;

        if(filesCacheDir.length > 1) {
            for (File aFilesCacheDir : filesCacheDir) {
                //Is not internal SD-Card
                if (!aFilesCacheDir.getAbsolutePath().contains(externalPath)) {
                    return aFilesCacheDir.getAbsolutePath();
                }
            }
        } else if(filesCacheDir.length == 1) {
            return filesCacheDir[0].getAbsolutePath();
        } else {
            return null;
        }

        return null;
    }

    /**
     * Returns the internal cache directory.
     * @param context application context
     * @return returns the absolute path of the directory and null if no cache directory exists
     */
    public static String getInternalCacheDirectory(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    /**
     * Try to write file to second external storage with subfolder. Since Kitkat its not
     * possible to write into every folder but into cache folder. Be sure that this files
     * will be deleted when the user deletes your application.
     * If it fails to write to second external storage, it will try to write to first
     * external storage. If this doesnt work it returns null
     * @param context application context
     * @param input inputstream to write
     * @param folder in which folder this file should be saved
     *               (its possible to write firstFolder/second
     * @param fileName name of the file (with extension)
     * @return absolute path or null if it couldnt save to external folder
     * @throws IOException
     */
    public String writeToRealExternalCacheStorage(Context context, InputStream input,
                                                         String folder, String fileName)
            throws IOException {
        //External path is internal SD-Card or external SD-Card (if internal is not available)
        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        //All external directions (app specific)
        File[] filesCacheDir = ContextCompat.getExternalCacheDirs(context);

        File directory = null;

        if(filesCacheDir.length > 1) {
            for(int i = 0; i < filesCacheDir.length; i++) {
                //Is not internal SD-Card
                if(!filesCacheDir[i].getAbsolutePath().contains(externalPath)) {
                    directory = new File(filesCacheDir[i].getAbsolutePath(), folder);
                }

                if(directory == null)
                    directory = new File(filesCacheDir[1].getAbsolutePath(), folder);
            }
        } else if(filesCacheDir.length == 1) {
            directory = new File(filesCacheDir[0].getAbsolutePath(), folder);
        } else {
            return null;
        }

        if(directory != null) {
            File finalFile = new File(directory, fileName);

            //Create folders if not exists
            directory.mkdirs();

            write(finalFile, input);

            return finalFile.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Try to write file to second external storage with no subfolder. Since Kitkat its not
     * possible to write into every folder but into cache folder. Be sure that this files
     * will be deleted when the user deletes your application.
     * If it fails to write to second external storage, it will try to write to first
     * external storage. If this doesnt work it returns null
     * @param context application context
     * @param input inputstream to write
     * @param fileName name of the file (with extension)
     * @return absolute path or null if it couldnt save to external folder
     * @throws IOException
     */
    public String writeToRealExternalCacheStorage(Context context, InputStream input,
                                                  String fileName)
            throws IOException {
        //External path is internal SD-Card or external SD-Card (if internal is not available)
        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        //All external directions (app specific)
        File[] filesCacheDir = ContextCompat.getExternalCacheDirs(context);

        File directory = null;

        if(filesCacheDir.length > 1) {
            for(int i = 0; i < filesCacheDir.length; i++) {
                //Is not internal SD-Card
                if(!filesCacheDir[i].getAbsolutePath().contains(externalPath)) {
                    directory = new File(filesCacheDir[i].getAbsolutePath());
                }

                if(directory == null)
                    directory = new File(filesCacheDir[1].getAbsolutePath());
            }
        } else if(filesCacheDir.length == 1) {
            directory = new File(filesCacheDir[0].getAbsolutePath());
        } else {
            return null;
        }

        if(directory != null) {
            File finalFile = new File(directory, fileName);

            write(finalFile, input);

            return finalFile.getAbsolutePath();
        } else {
            return null;
        }
    }


    /**
     * Write file to first external storage
     * @param environmentDirectory for example: Environment.DIRECTORY_PICTURES
     * @param input inputstream to write
     * @param folder in which folder this file should be saved
     *               (its possible to write firstFolder/second
     * @param fileName name of the file (with extension)
     * @return full path or null
     * @throws IOException
     */

    public String writeToExternalStorage(String environmentDirectory, InputStream input,
                                         String folder, String fileName) throws IOException {
        if(isExternalStorageWritable()) {
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    environmentDirectory), folder);
            directory.mkdirs();

            File file = new File(directory, fileName);

            write(file, input);

            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Write file to first external storage
     * @param environmentDirectory for example: Environment.DIRECTORY_PICTURES
     * @param input inputstream to write
     * @param fileName name of the file (with extension)
     * @return full path or null
     * @throws IOException
     */

    public String writeToExternalStorage(String environmentDirectory, InputStream input,
                                         String fileName) throws IOException {
        if(isExternalStorageWritable()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    environmentDirectory), fileName);

            write(file, input);

            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Write file with progress update
     * It updates "progress" and fires onProgress runnable
     * @param file where to write
     * @param input what to write
     * @throws IOException
     */
    private void write(File file, InputStream input) throws IOException {
        OutputStream output = new FileOutputStream(file);
        byte data[] = new byte[1024];

        int count;
        long total = 0;

        boolean progressUpdate = (onProgress != null & lenghtOfFile != -1);
        progress = -1;

        while ((count = input.read(data)) != -1) {
            total += count;
            output.write(data, 0, count);

            if(progressUpdate) {
                progress = (int) ((total*100)/lenghtOfFile);
                onProgress.run();
            }
        }

        output.flush();
        output.close();
        input.close();
    }

    /**
     * Checks if external storage is available for read and write
     * @return is writeable
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Return inputstream needed for other methods from file
     * @param file
     * @return
     */
    public static InputStream getInputStreamFromFile(File file) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     *
     * @param onProgress Runnable that will be fired every 1024 bytes
     */
    public void setOnProgress(Runnable onProgress) {
        this.onProgress = onProgress;
    }

    /**
     *
     * @return progress in percent
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Is usefull if you write a file which will be downloaded
     * @param lenghtOfFile the lenght of the file to download
     */
    public void setLenghtOfFile(int lenghtOfFile) {
        this.lenghtOfFile = lenghtOfFile;
    }
}
