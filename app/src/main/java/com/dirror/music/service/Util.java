package com.dirror.music.service;

import android.content.Context;
import android.graphics.Bitmap;

import com.dirror.music.App;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util {

    public static void writeImageToFile(Bitmap bitmap) {
        Context context = App.context;
        //create a file to write bitmap data

        try {
            File f = new File(context.getExternalCacheDir(), "cover.jpg");
            f.createNewFile();

            //Convert bitmap to byte array

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();

            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
