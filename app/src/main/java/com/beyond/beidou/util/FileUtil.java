package com.beyond.beidou.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.BuildConfig;
import com.beyond.beidou.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author: 李垚
 * @date: 2021/4/12
 */
public class FileUtil {
    /**
     * 判断指定目录的文件夹是否存在，如果不存在则需要创建新的文件夹
     *
     * @param fileName 指定目录
     * @return 返回创建结果 TRUE or FALSE
     */
    public static boolean fileIsExist(String fileName) {
        //传入指定的路径，然后判断路径是否存在
        File file = new File(fileName);
        if (file.exists())
            return true;
        else {
            //file.mkdirs() 创建文件夹
            return file.mkdirs();
        }
    }

    public static void saveBitmap(String name, Bitmap bm, Context mContext) {
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

    public static void openExcelFile(Context context, String fileUrl) {
        if (fileUrl != null) {
            try {
                Uri uri;
                File file = new File(fileUrl);
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                //添加第三方读权限
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (Build.VERSION.SDK_INT > 23) {
                    //Android 7.0之后
                    uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);//给目标文件临时授权
                } else {
                    uri = Uri.fromFile(file);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                LogUtil.e("该文件是否存在", fileIsExist(fileUrl) + " ");
                intent.setDataAndType(uri, "application/vnd.ms-excel");  //.xls
                context.startActivity(intent);
            } catch (Exception e) {
                //没有安装第三方的软件会提示
                LogUtil.e("打开文件异常信息", e.toString());
                Toast toast = Toast.makeText(context, "没有找到打开该文件的应用程序", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast.makeText(context, "文件路径错误", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public static void openDownload(Context context, String fileUrl) {
        //getUrl()获取文件目录，例如返回值为/storage/sdcard1/MIUI/music/mp3_hd/单色冰淇凌_单色凌.mp3
        File file = new File(fileUrl);
        //获取父目录
        File parentFile = new File(file.getParent());
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.fromFile(parentFile), "*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        context.startActivity(intent);
    }


    public static void shareFile(Context context, String fileName) {
        File file = new File(fileName);
        if (null != file && file.exists()) {
            Intent share = new Intent(Intent.ACTION_SEND);
            //android7以上用FileProvider
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                share.putExtra(Intent.EXTRA_STREAM, contentUri);
            } else {
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            }
            share.setType("application/octet-stream");
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share, "分享文件"));
        } else {
            Toast.makeText(context, "未选中分享的文件", Toast.LENGTH_SHORT).show();
        }
    }

    public static void zip(String src, String dest) throws IOException {
        //定义压缩输出流
        ZipOutputStream out = null;
        try {
            //传入源文件
            File fileOrDirectory = new File(src);
            File outFile = new File(dest);
            //传入压缩输出流
            //创建文件前几级目录
            if (!outFile.exists()) {
                File parentFile = outFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
            }
            //可以通过createNewFile()函数这样创建一个空的文件，也可以通过文件流的使用创建
            out = new ZipOutputStream(new FileOutputStream(outFile));
            //判断是否是一个文件或目录
            //如果是文件则压缩
            if (fileOrDirectory.isFile()) {
                zipFileOrDirectory(out, fileOrDirectory, "");
            } else {
                //否则列出目录中的所有文件递归进行压缩

                File[] entries = fileOrDirectory.listFiles();
                for (int i = 0; i < entries.length; i++) {
                    zipFileOrDirectory(out, entries[i], fileOrDirectory.getName() + "/");//传入最外层目录名
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void zipFileOrDirectory(ZipOutputStream out, File fileOrDirectory, String curPath) throws IOException {
        FileInputStream in = null;
        try {
            //判断是否为目录
            if (!fileOrDirectory.isDirectory()) {
                byte[] buffer = new byte[4096];
                int bytes_read;
                in = new FileInputStream(fileOrDirectory);//读目录中的子项
                //归档压缩目录
                ZipEntry entry = new ZipEntry(curPath + fileOrDirectory.getName());//压缩到压缩目录中的文件名字
                //getName() 方法返回的路径名的名称序列的最后一个名字，这意味着表示此抽象路径名的文件或目录的名称被返回。
                //将压缩目录写到输出流中
                out.putNextEntry(entry);//out是带有最初传进的文件信息，一直添加子项归档目录信息
                while ((bytes_read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes_read);
                }
                out.flush();
                out.closeEntry();
            } else {
                //列出目录中的所有文件
                File[] entries = fileOrDirectory.listFiles();
                for (int i = 0; i < entries.length; i++) {
                    //递归压缩
                    zipFileOrDirectory(out, entries[i], curPath + fileOrDirectory.getName() + "/");//第一次传入的curPath是空字符串
                }//目录没有后缀所以直接可以加"/"
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void yeZip(List<String> filePaths, String destPath) throws FileNotFoundException {
//        ZipOutputStream zipOutputStream= new FileOutputStream(new File(destPath));
    }

    public static void saveProjectCache(BaseActivity activity, String projectInfo) {
        File file = new File(activity.getCacheDir(), "projectCache-" + activity.getStringFromSP("presentPlatform"));
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(projectInfo.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getProjectCache(BaseActivity activity) {
        File file = new File(activity.getCacheDir(), "projectCache-" + activity.getStringFromSP("presentPlatform"));
        if (!file.exists()) {
            return null;
        } else {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                int length = fis.available();
                byte[] bytes = new byte[length];
                fis.read(bytes);
                return new String(bytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(fis != null){
                    try{
                        fis.close();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static boolean fileExist(String filePath) {
        return new File(filePath).exists();
    }

    public static void fileDelete(String filePath){
        new File(filePath).delete();
    }

}
