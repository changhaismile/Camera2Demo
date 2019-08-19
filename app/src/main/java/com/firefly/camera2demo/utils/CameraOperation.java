package com.firefly.camera2demo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.view.TextureView;
import android.widget.Toast;

import com.firefly.camera2demo.activity.MainActivity;

/**
 * @author sfs
 * @create 19-8-17
 * @Describe
 */
public class CameraOperation {
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private TextureView ttvPreview;
    private Context mContext;

    public CameraOperation(Context mContext, ImageReader mImageReader, CameraDevice mCameraDevice, CameraCaptureSession mCaptureSession, TextureView ttvPreview) {
        this.mImageReader = mImageReader;
        this.mCameraDevice = mCameraDevice;
        this.mCaptureSession = mCaptureSession;
        this.ttvPreview = ttvPreview;
        this.mContext = mContext;
    }

    private void switchCamera(String cameraId) {

        //后置切换前置
        if (cameraId.equals("0")) {

            //关闭预览释放资源
            closeCamera();
            reOpenCamera("1");


        } else {
            //前置切换后置


        }

    }

    private void reOpenCamera(String cameraId) {

        if (ttvPreview.isAvailable()) {

        }


    }

    private void openCamra(String mCameraId) {

    }

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
