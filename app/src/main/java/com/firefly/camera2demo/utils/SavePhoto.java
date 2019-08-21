package com.firefly.camera2demo.utils;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author sfs
 * @create 19-8-21
 * @Describe
 */
public class SavePhoto {
    private static File mImageFile;
    private static String FileName;
    private String TAG = "SavePhoto";
    private static SavePhoto savePhoto = new SavePhoto();

    public void imageSaver(Image image) {

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        FileName = System.currentTimeMillis() + ".jpg";

        File root = new File(Environment.getExternalStorageDirectory(), "DCIM/Camera");
        mImageFile = new File(root, FileName);
        Log.i(TAG, "imageSaver: mImageFile  i" + mImageFile);//   /storage/emulated/0/DCIM/Camera/1566357462417.jpg
        Log.d(TAG, "imageSaver: mImageFile  d" + mImageFile);//   /storage/emulated/0/DCIM/Camera/1566357462417.jpg
        Log.e(TAG, "imageSaver: mImageFile  e" + mImageFile);//   /storage/emulated/0/DCIM/Camera/1566357462417.jpg

//        String galleryPath= Environment.getExternalStorageDirectory()
//                + File.separator + Environment.DIRECTORY_DCIM
//                +File.separator+"Camera"+File.separator+"myPicture.jpg";
//        Log.i(TAG, "imageSaver: mImageFile1 "+galleryPath);


        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mImageFile);
            fos.write(data, 0, data.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void updatePhoto(Activity activity) {

        //通知相册更新
        try {
            MediaStore.Images.Media.insertImage(activity.getContentResolver(), mImageFile.toString(), FileName, null);



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(mImageFile);
        intent.setData(uri);
        activity.sendBroadcast(intent);

    }


    public static SavePhoto getSavePhoto() {
        return savePhoto;
    }
}
