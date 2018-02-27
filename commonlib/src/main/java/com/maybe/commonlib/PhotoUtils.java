package com.maybe.commonlib;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者：fangluxin  on 2018/2/11 0011 15:17
 * 邮箱：flx_coding@163.com
 * 公司：南京艾思优信息科技有限公司
 */
public class PhotoUtils {

    private PermissionListener permissionListener;


    public  static final  int REQ_TAKE_PHOTO=100;
    public  static final  int REQ_ALBUM=101;
    public static final int REQ_ZOOM = 102;
    private String imgPath;
    private Uri uri;

    /**开启摄像机*/
    private void openCamera(String imgPath, Activity activity) {
        // 指定调用相机拍照后照片的储存路径
        File imgFile = new File(imgPath);
        Uri imgUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            //如果是7.0或以上
            imgUri = FileProvider.getUriForFile(activity, UIUtils.getPackageName(activity) + ".fileprovider", imgFile);
        } else {
            imgUri = Uri.fromFile(imgFile);
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        activity.startActivityForResult(intent, REQ_TAKE_PHOTO);
    }



    /**
     * 拍照,使用存储卡路径（需要申请存储权限），即图片的路径在  存储卡目录下 -> 包名 -> icon文件夹下
     */
    public void takePhoto(final Activity activity){
        imgPath =  FileUtils.generateImgePathInStoragePath(activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //如果是6.0或6.0以上，则要申请运行时权限,这里需要申请拍照的权限
            requestRuntimePermission(activity,new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, new PermissionListener() {
                @Override
                public void onGranted() {
                    //开启摄像头，拍照完的图片保存在对应路径下
                    openCamera(imgPath,activity);
                }

                @Override
                public void onDenied(List<String> deniedPermissions) {
//                    UIUtils.showToast("所需权限被拒绝");
                }
            });
            return;
        }

        openCamera(imgPath,activity);
    }



    /**
     * 申请运行时权限
     */
    public void requestRuntimePermission(Activity activity,String[] permissions, PermissionListener permissionListener) {
        this.permissionListener = permissionListener;
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), 1);
        } else {
            permissionListener.onGranted();
        }
    }



    /**打开相册*/
    public void openAlbum(Activity activity){
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        activity.startActivityForResult(intent, REQ_ALBUM);
    }
}
