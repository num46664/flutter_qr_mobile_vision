package com.github.rmtmckenzie.qrmobilevision;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;


import java.io.IOException;
import java.util.List;

/**
 * Implements QrCamera using Deprecated Camera API
 */
@TargetApi(19)
@SuppressWarnings("deprecation")
class QrCameraC1 implements QrCamera {

    private Camera.CameraInfo info = new Camera.CameraInfo();
    private int targetWidth, targetHeight;
    private Camera camera;
    private final SurfaceTexture texture;
    private final QrDetector detector;

    QrCameraC1(int width, int height, SurfaceTexture texture, QrDetector detector) {
        this.texture = texture;
        targetHeight = height;
        targetWidth = width;
        this.detector = detector;
    }

    @Override
    public void start() {
        camera = Camera.open();
        Camera.getCameraInfo(0, info);
        Camera.Parameters parameters = camera.getParameters();
        Size size = getAppropriateSize(parameters.getSupportedPreviewSizes());
        parameters.setPreviewSize(size.width,size.height);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.setParameters(parameters);
        texture.setDefaultBufferSize(size.width, size.height);

        detector.useNV21(size.width,size.height);


        try {

            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (data != null) detector.detect(data);
                    else System.out.println("It's NULL!");
                }
            });
            camera.setPreviewTexture(texture);
            camera.startPreview();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getWidth() {
        return camera.getParameters().getPreviewSize().width;
    }

    @Override
    public int getHeight() {
        return camera.getParameters().getPreviewSize().height;
    }

    @Override
    public int getOrientation() {
        return info.orientation;
    }

    @Override
    public void stop() {
        camera.stopPreview();
        camera.setPreviewCallback(null);
        camera.release();
    }

    //Size here is Camera.Size, not android.util.Size as in the QrCameraC2 version of this method
    private Size getAppropriateSize(List<Size> sizes) {
        Size s = sizes.get(0);
        if (info.orientation % 180 == 0) {
            for (Size size : sizes) {
                if (size.height < targetHeight || size.width < targetWidth) {
                    break;
                }
                s = size;
            }
        } else {
            for (Size size : sizes) {
                if (size.height < targetWidth || size.width < targetHeight) {
                    break;
                }
                s = size;
            }
        }
        return s;
    }
}
