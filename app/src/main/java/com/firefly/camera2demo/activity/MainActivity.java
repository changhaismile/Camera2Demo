package com.firefly.camera2demo.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.firefly.camera2demo.R;
import com.firefly.camera2demo.base.PermissionBaseActivity;

import java.util.Arrays;

public class MainActivity extends PermissionBaseActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {
    private String TAG = "MainActivity";
    private TextureView ttvPreview;
    private Button btn_switch;
    private HandlerThread mThreadHandler;
    private Handler mHandler;
    private Size mPreviewSize;

    private CaptureRequest.Builder mPreviewBuilder;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;

    private int mImageWidth = 1920;
    private int mImageHeight = 1080;
    private String[] mPermissions = new String[]{
            Manifest.permission.CAMERA};
    private String mCameraId;
    private String mFontCameraId;
    private String mBackCameraId;

    private CameraManager cameraManager;
    private static Range<Integer>[] FpsRanges;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setPermissions(mPermissions, new InterfacePermission() {
//            @Override
//            public void onAllow() {
//                Toast.makeText(MainActivity.this, "权限已全部允许,可进行初始化操作", Toast.LENGTH_SHORT).show();
//                initView();
//                initLooper();
//
//            }
//        });

        //java 1.8的Lambda表达式
        setPermissions(mPermissions, () -> {
            Toast.makeText(MainActivity.this, "权限已全部允许,可进行初始化操作", Toast.LENGTH_SHORT).show();
            initView();
            initLooper();
        });


    }

    private void initView() {
        btn_switch = findViewById(R.id.btn_switch);
        ttvPreview = findViewById(R.id.ttvPreview);
        btn_switch.setOnClickListener(this);
        cameraList();
        //监听SurfaceTexture状态,SurfaceTexture可用时回调onSurfaceTextureAvailable方法
        ttvPreview.setSurfaceTextureListener(this);
    }

    private void initLooper() {
        //创建HandlerThread,用"CAMERA2"标记
        mThreadHandler = new HandlerThread("CAMERA2");
        //启动线程
        mThreadHandler.start();
        //创建工作线程Handler
        mHandler = new Handler(mThreadHandler.getLooper());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (mCameraId != null) {
            openCamera(mCameraId);
        }

    }

    private void openCamera(String mCameraId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "权限不足", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * 遍历摄像头,检查摄像头是否可用,以及获取摄像头支持的尺寸,FPS等属性
     */
    private void cameraList() {
        //获得所有摄像头的管理者CameraManager
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                characteristics = cameraManager.getCameraCharacteristics(cameraId);

                //后置摄像头"0"
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK) {
                    mBackCameraId = cameraId;
                    //摄像头支持的FPS范围,前后摄像头一样
                    FpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                    //cameraList: backFpsRanges[[12, 15], [15, 15], [14, 20], [20, 20], [14, 25], [25, 25], [14, 30], [30, 30]]
                    Log.i(TAG, "cameraList: backFpsRanges" + Arrays.toString(FpsRanges));
                }
                //前置摄像头"1"
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_FRONT) {
                    mFontCameraId = cameraId;
                }

            }
            if (mBackCameraId != null) {
                mCameraId = mBackCameraId;//默认打开后置摄像头
            } else if (mFontCameraId != null) {
                mCameraId = mFontCameraId;
                Toast.makeText(this, "后置摄像头不可用,已切换前置摄像头", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "摄像头不可用,请检查设备", Toast.LENGTH_SHORT).show();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //获取摄像头支持的所有输出格式和尺寸的管理者StreamConfigurationMap
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        //取摄像头支持的预览尺寸
        mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[2];

        //cameraList: mPreviewSize    [0]->2816x2112  [2]->2160x1064  [15]->208x144(预览模糊)  手机分辨率为2280x1080
        Log.i(TAG, "cameraList: mPreviewSize    " + map.getOutputSizes(SurfaceTexture.class)[2].toString());

    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }


    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview(mCameraDevice);
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }
    };

    private void startPreview(CameraDevice cameraDevice) {
        SurfaceTexture texture = ttvPreview.getSurfaceTexture();
        //设置的就是预览大小
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);
        try {
            //设置捕获请求模式为预览
            mPreviewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //对相机参数进行设置
        setCamera();
        //图片的大小,格式以及捕捉数量(2+1)
        mImageReader = ImageReader.newInstance(mImageWidth, mImageHeight, ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mHandler);
        //添加两个surface，一个TextureView的，另一个ImageReader的,用于页面显示和预览数据回调
        mPreviewBuilder.addTarget(surface);
        mPreviewBuilder.addTarget(mImageReader.getSurface());
        try {
            cameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), mSessionStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void setCamera() {
        //曝光
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        //FPS
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, FpsRanges[5]);
    }


    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            mCaptureSession = cameraCaptureSession;

            try {
                //请求捕获图像
                cameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

        }
    };


//    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
//        @Override
//        public void onImageAvailable(ImageReader imageReader) {
//            Image img = imageReader.acquireNextImage();
//            ByteBuffer buffer = img.getPlanes()[0].getBuffer();
//            byte[] data = new byte[buffer.remaining()];
//            buffer.get(data);
//            img.close();
//
//        }
//    };

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = (ImageReader imageReader) -> {
        //从ImageReader队列获取下一个图像
        Image img = imageReader.acquireNextImage();
//        ByteBuffer buffer = img.getPlanes()[0].getBuffer();
//        byte[] data = new byte[buffer.remaining()];
//        buffer.get(data);
        //注意不用要关闭,否则会报错
        img.close();

    };


    @Override
    protected void onDestroy() {
        closeCamera();
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_switch:
                closeCamera();
                if (mCameraId != null) {
                    if (mCameraId.equals(mBackCameraId) && mFontCameraId != null) {//后置切换前置
                        mCameraId = mFontCameraId;
                    } else if (mCameraId.equals(mFontCameraId) && mBackCameraId != null) {//前置切换成后置
                        mCameraId = mBackCameraId;
                    } else {
                        Toast.makeText(this, "摄像头不可用,请检查设备", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (ttvPreview.isAvailable()) {
                        openCamera(mCameraId);
                    } else {
                        //重新获取textureView
                        ttvPreview.setSurfaceTextureListener(this);
                    }
                    Toast.makeText(this, "切换成功", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "摄像头不可用,请检查设备", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;

        }
    }

    //释放相机资源
    private void closeCamera() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }


}
