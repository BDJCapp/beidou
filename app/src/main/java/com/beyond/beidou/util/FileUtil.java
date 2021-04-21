package com.beyond.beidou.util;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author: 李垚
 * @date: 2021/4/12
 */
public class FileUtil {
    /**
     * 判断指定目录的文件夹是否存在，如果不存在则需要创建新的文件夹
     * @param fileName 指定目录
     * @return 返回创建结果 TRUE or FALSE
     */
    public static  boolean fileIsExist(String fileName)
    {
        //传入指定的路径，然后判断路径是否存在
        File file=new File(fileName);
        if (file.exists())
            return  true;
        else{
            //file.mkdirs() 创建文件夹
            return file.mkdirs();
        }
    }

    public static void saveBitmap(String name, Bitmap bm, Context mContext)
    {
        LogUtil.e("Save Bitmap", "Ready to save picture");
        //指定我们想要存储文件的地址
        String TargetPath = mContext.getFilesDir() + "/images/";
        LogUtil.e("Save Bitmap", "Save Path=" + TargetPath);
        //判断指定文件夹的路径是否存在
        if (!FileUtil.fileIsExist(TargetPath)) {
            LogUtil.e("Save Bitmap", "TargetPath isn't exist");
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, name);

            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                //存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
                LogUtil.e("Save Bitmap", "The picture is save to your phone!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
