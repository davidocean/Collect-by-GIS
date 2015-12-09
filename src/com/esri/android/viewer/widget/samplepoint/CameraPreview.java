package com.esri.android.viewer.widget.samplepoint;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
	private final String TAG = "CameraPreview";

    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Size mPreviewSize;//显示大小
    Size mPictureSize;//照片大小
    List<Size> mSupportedPreviewSizes;//设备支持的显示大小
    List<Size> mSupportedPictureSizes;//设备支持的照片大小
    Camera mCamera;
    Context mContext;
    // Preview类的构造方法 
    CameraPreview(Context context, SurfaceView sv) {
        super(context);
        mSurfaceView = sv;
        //addView(mSurfaceView);
        mContext = context;
        // 获得SurfaceHolder对象 
        mHolder = mSurfaceView.getHolder();
        // 指定用于捕捉拍照事件的SurfaceHolder.Callback对象 
        mHolder.addCallback(this);
        // 设置SurfaceHolder对象的类型 
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
    	mCamera = camera;
    	if (mCamera != null) {
    		mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
    		mPreviewSize = getMaxSize(mSupportedPreviewSizes);
    		mSupportedPictureSizes = mCamera.getParameters().getSupportedPictureSizes();
    		mPictureSize = getMaxSize(mSupportedPictureSizes);//获取支持的最大分辨率
    		requestLayout();

    		// get Camera parameters
    		Camera.Parameters params = mCamera.getParameters();
    		List<String> focusModes = params.getSupportedFocusModes();
    		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
    			// set the focus mode
    			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
    			// set Camera parameters
    			mCamera.setParameters(params);
    		}
    	}
    }

    /*
     * 获取设备支持最大分辨率-------说明不同设备最大分辨率所处列表位置会不同
     */
    private Size getMaxSize(List<Size> sizels) {
    	Size sizeresult = sizels.get(0);
    	for(int i=0;i<sizels.size();i++){
    		if(sizels.get(i).height>sizeresult.height||sizels.get(i).width>sizeresult.width){
    			sizeresult = sizels.get(i);
    		}
    	}
		return sizeresult;
	}

	@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }


    // 在surface创建时激发
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

//    private int getPreviewDegree(Context context) {
//    	// 获得手机的方向  
//    	Activity  act = (Activity) context;
//        int rotation =act.getWindowManager().getDefaultDisplay()  
//                .getRotation();  
//        int degree = 0;  
//        // 根据手机的方向计算相机预览画面应该选择的角度  
//        switch (rotation) {  
//        case Surface.ROTATION_0:  
//            degree = 90;  
//            break;  
//        case Surface.ROTATION_90:  
//            degree = 0;  
//            break;  
//        case Surface.ROTATION_180:  
//            degree = 270;  
//            break;  
//        case Surface.ROTATION_270:  
//            degree = 180;  
//            break;  
//        }  
//        return degree;  
//	}

    // 在surface销毁时激发
	public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
		// 释放手机摄像头 
		//mCamera.release(); 
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }


//    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
//        final double ASPECT_TOLERANCE = 0.1;
//        double targetRatio = (double) w / h;
//        if (sizes == null) return null;
//
//        Size optimalSize = null;
//        double minDiff = Double.MAX_VALUE;
//
//        int targetHeight = h;
//
//        // Try to find an size match aspect ratio and size
//        for (Size size : sizes) {
//            double ratio = (double) size.width / size.height;
//            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
//            if (Math.abs(size.height - targetHeight) < minDiff) {
//                optimalSize = size;
//                minDiff = Math.abs(size.height - targetHeight);
//            }
//        }
//
//        // Cannot find the one match the aspect ratio, ignore the requirement
//        if (optimalSize == null) {
//            minDiff = Double.MAX_VALUE;
//            for (Size size : sizes) {
//                if (Math.abs(size.height - targetHeight) < minDiff) {
//                    optimalSize = size;
//                    minDiff = Math.abs(size.height - targetHeight);
//                }
//            }
//        }
//        return optimalSize;
//    }

	 // 在surface的大小发生改变时激发
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	if(mCamera != null) {
    		Camera.Parameters parameters = mCamera.getParameters();
			//以下注释掉的是设置预览时的图像以及拍照的一些参数  
    		//parameters.setPictureFormat(PixelFormat.JPEG);  
    		parameters.setFocusMode("auto");  		 
    		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
    		parameters.setPictureSize(mPictureSize.width, mPictureSize.height); // 设置保存的图片尺寸 
    		parameters.setJpegQuality(100);
    		
//    		parameters.set("测试", "测试");
//    		parameters.set("test", "test");
//    		parameters.set("test", 1);
//    		parameters.setGpsAltitude (12.22);
//    		parameters.setGpsLatitude(100);
//    		parameters.setGpsLongitude(30);
//    		parameters.setGpsProcessingMethod("GPS");
    		//parameters.setJpegQuality(100); // 设置照片质量  

    		requestLayout();

    		mCamera.setParameters(parameters);
    		mCamera.startPreview();
    	}
    }
}
