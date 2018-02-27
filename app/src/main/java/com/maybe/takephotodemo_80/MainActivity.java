package com.maybe.takephotodemo_80;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.maybe.commonlib.FileUtils;
import com.maybe.commonlib.PermissionListener;
import com.maybe.commonlib.UIUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static com.maybe.commonlib.PhotoUtils.REQ_ALBUM;
import static com.maybe.commonlib.PhotoUtils.REQ_TAKE_PHOTO;
import static com.maybe.commonlib.PhotoUtils.REQ_ZOOM;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button select;
    private Button take;
    private ImageView iv_center;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 101;
    private PermissionListener permissionListener;
    private Uri outputUri;
    private String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        select = findViewById(R.id.select);
        take = findViewById(R.id.take);
        iv_center = findViewById(R.id.iv_center);
        select.setOnClickListener(this);
        take.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select://选取图片


                openAlbum();

                break;

            case R.id.take://拍摄图片
                takePhoto();


                break;
        }
    }


    /**
     * 拍照,使用存储卡路径（需要申请存储权限），即图片的路径在  存储卡目录下 -> 包名 -> icon文件夹下
     */
    public void takePhoto() {
        imgPath = FileUtils.generateImgePathInStoragePath(MainActivity.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //如果是6.0或6.0以上，则要申请运行时权限,这里需要申请拍照的权限
            requestRuntimePermission(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, new PermissionListener() {
                @Override
                public void onGranted() {
                    //开启摄像头，拍照完的图片保存在对应路径下
                    openCamera(imgPath);
                }

                @Override
                public void onDenied(List<String> deniedPermissions) {
                    Toast.makeText(MainActivity.this, "所需权限被拒绝", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        openCamera(imgPath);
    }

    /**
     * 打开相册
     */
    public void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        startActivityForResult(intent, REQ_ALBUM);
    }

    /**
     * 开启摄像机
     */
    private void openCamera(String imgPath) {
        // 指定调用相机拍照后照片的储存路径
        File imgFile = new File(imgPath);
        Uri imgUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            //如果是7.0或以上
            imgUri = FileProvider.getUriForFile(this, UIUtils.getPackageName(MainActivity.this) + ".fileprovider", imgFile);
        } else {
            imgUri = Uri.fromFile(imgFile);
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(intent, REQ_TAKE_PHOTO);
    }

    /**
     * 申请运行时权限
     */
    public void requestRuntimePermission(String[] permissions, PermissionListener permissionListener) {
        this.permissionListener = permissionListener;
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
        } else {
            permissionListener.onGranted();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK://调用图片选择处理成功
                String zoomImgPath = "";
                Bitmap bm = null;
                File temFile = null;
                File srcFile = null;
                File outPutFile = null;
                switch (requestCode) {
                    case REQ_TAKE_PHOTO:// 拍照后在这里回调
                        srcFile = new File(imgPath);
                        outPutFile = new File(FileUtils.generateImgePathInStoragePath(MainActivity.this));
                        outputUri = Uri.fromFile(outPutFile);
                        FileUtils.startPhotoZoom(this, srcFile, outPutFile, REQ_ZOOM);// 发起裁剪请求
                        break;

                    case REQ_ALBUM:// 选择相册中的图片
                        if (data != null) {
                            Uri sourceUri = data.getData();
                            String[] proj = {MediaStore.Images.Media.DATA};

                            // 好像是android多媒体数据库的封装接口，具体的看Android文档
                            Cursor cursor = managedQuery(sourceUri, proj, null, null, null);

                            // 按我个人理解 这个是获得用户选择的图片的索引值
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                            cursor.moveToFirst();
                            // 最后根据索引值获取图片路径
                            imgPath = cursor.getString(column_index);

                            srcFile = new File(imgPath);
                            outPutFile = new File(FileUtils.generateImgePathInStoragePath(MainActivity.this));
                            outputUri = Uri.fromFile(outPutFile);
                            FileUtils.startPhotoZoom(this, srcFile, outPutFile, REQ_ZOOM);// 发起裁剪请求
                        }
                        break;

                    case REQ_ZOOM://裁剪后回调
                        //  Bundle extras = data.getExtras();
                        if (data != null) {
                            //  bm = extras.getParcelable("data");
                            if (outputUri != null) {
                                bm = decodeUriAsBitmap(outputUri);
                                //如果是拍照的,删除临时文件
                                temFile = new File(imgPath);
//                                if (temFile.exists()) {
//                                    temFile.delete();
//                                }

                                String scaleImgPath = FileUtils.saveBitmapByQuality(bm, 100, MainActivity.this);//复制并压缩到自己的目录并压缩
                                //进行上传，上传成功后显示新图片,这里不演示上传的逻辑，上传只需将scaleImgPath路径下的文件上传即可。
                                iv_center.setImageBitmap(bm);//显示在iv上
                            }
                        } else {
                            Toast.makeText(this, "选择图片发生错误，图片可能已经移位或删除", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                break;
        }
    }


    public Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            // 先通过getContentResolver方法获得一个ContentResolver实例，
            // 调用openInputStream(Uri)方法获得uri关联的数据流stream
            // 把上一步获得的数据流解析成为bitmap
            bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    List<String> deniedPermissions = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        int grantResult = grantResults[i];
                        String permission = permissions[i];
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            deniedPermissions.add(permission);
                        }
                    }
                    if (deniedPermissions.isEmpty()) {
                        permissionListener.onGranted();
                    } else {
                        permissionListener.onDenied(deniedPermissions);
                    }
                }
                break;
        }
    }


}
