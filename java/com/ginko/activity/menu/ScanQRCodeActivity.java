package com.ginko.activity.menu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.ginko.activity.exchange.ExchangeItem;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.api.request.CBRequest;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.customview.QRScannerCameraPreview;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.ContactUserInfoVo;
import com.ginko.vo.SharedInfoVO;
import com.ginko.vo.UserWholeProfileVO;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class ScanQRCodeActivity extends MyBaseActivity implements View.OnClickListener{

    /* UI variables */
    private Button btnPrev;

    /* variables*/
    private Camera mCamera;
    private QRScannerCameraPreview mPreview;
    private Handler autoFocusHandler;

    private ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    private final int GOTO_PERMISSION_WITH_SCANNED_USERID = 1;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case GOTO_PERMISSION_WITH_SCANNED_USERID:
                    final int userId = msg.arg1;
                    if(userId > 0)
                    {
                        String contactFullName = "";
                        boolean isUnexchangedContact = true;
                        if(MyApp.g_contactItems!= null && MyApp.g_contactItems.size() >0 && MyApp.g_contactIDs.contains(new Integer(userId)))
                        {
                            for(int i=0;i<MyApp.g_contactItems.size();i++)
                            {
                                if(MyApp.g_contactItems.get(i).getContactId() == userId)
                                {
                                    contactFullName = MyApp.g_contactItems.get(i).getFullName();
                                    isUnexchangedContact = false;
                                    MyApp.getInstance().showSimpleAlertDiloag(ScanQRCodeActivity.this, contactFullName + " is already a contact.", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ScanQRCodeActivity.this.finish();
                                        }
                                    });

                                    return;
                                }
                            }
                        }
                        else if(userId == MyApp.getInstance().g_userId)
                        {
                            MyApp.getInstance().showSimpleAlertDiloag(ScanQRCodeActivity.this, "This is your own QR code.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ScanQRCodeActivity.this.finish();
                                }
                            });

                            return;
                        }

                        final Integer nUserId = Integer.valueOf(userId);

                        UserInfoRequest.getInfo(Integer.valueOf(userId), new ResponseCallBack<UserWholeProfileVO>() {
                            @Override
                            public void onCompleted(JsonResponse<UserWholeProfileVO> response) {
                                if (response.isSuccess()) {
                                    UserWholeProfileVO myProfileInfo = response.getData();
                                    if (myProfileInfo.getShare() == null)//non-exchanged info
                                    {
                                        Intent contactSharingSettingIntent = new Intent(ScanQRCodeActivity.this, ShareYourLeafActivity.class);
                                        contactSharingSettingIntent.putExtra("contactID", String.valueOf(nUserId));
                                        contactSharingSettingIntent.putExtra("contactFullname", "");
                                        contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                                        contactSharingSettingIntent.putExtra("isInviteContact", true);
                                        ScanQRCodeActivity.this.startActivity(contactSharingSettingIntent);
                                        ScanQRCodeActivity.this.finish();

                                    } else {
                                        SharedInfoVO shareObj = myProfileInfo.getShare();
                                        final String shared_home_fids = shareObj.getSharedHomeFIds();
                                        final String shared_work_fids = shareObj.getSharedWorkFIds();
                                        final int sharingStatus = shareObj.getSharingStatus();
                                        int sharelimit = shareObj.getShareLimit();
                                        ContactUserInfoVo contactUserInfoVo = myProfileInfo.getContactUserInfo();
                                        String strFullName = contactUserInfoVo.getFirstName()+" "+contactUserInfoVo.getMiddleName();
                                        strFullName = strFullName.trim();
                                        strFullName = strFullName + " "+contactUserInfoVo.getLastName();
                                        strFullName = strFullName.trim();
                                        final String  contactName = strFullName;
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRCodeActivity.this);
                                        builder.setTitle("Confirm");
                                        builder.setCancelable(false);
                                        builder.setMessage(getResources().getString(R.string.str_confirm_dialog_goto_permission_pending_contact));
                                        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //TODO
                                                dialog.dismiss();
                                                Intent contactSharingSettingIntent = new Intent(ScanQRCodeActivity.this, ShareYourLeafActivity.class);
                                                contactSharingSettingIntent.putExtra("contactID", String.valueOf(nUserId));
                                                contactSharingSettingIntent.putExtra("contactFullname", contactName);
                                                contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                                                contactSharingSettingIntent.putExtra("isPendingRequest" , true);
                                                contactSharingSettingIntent.putExtra("email", "");
                                                contactSharingSettingIntent.putExtra("shared_home_fids", shared_home_fids);
                                                contactSharingSettingIntent.putExtra("shared_work_fids", shared_work_fids);
                                                contactSharingSettingIntent.putExtra("sharing_status", sharingStatus);
                                                contactSharingSettingIntent.putExtra("isInviteContact", true);
                                                startActivity(contactSharingSettingIntent);
                                                ScanQRCodeActivity.this.finish();
                                            }
                                        });
                                        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //TODO
                                                dialog.dismiss();
                                                mCamera.setPreviewCallback(previewCb);
                                                mCamera.startPreview();
                                                previewing = true;
                                                mCamera.autoFocus(autoFocusCB);
                                            }
                                        });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                } else {
                                    MyApp.getInstance().showSimpleAlertDiloag(ScanQRCodeActivity.this, "Failed to get contact info.", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ScanQRCodeActivity.this.finish();
                                        }
                                    });
                                }
                            }
                        });
                     }
                    break;
            }
        }
    };

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getUIObjects();
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnPrev = (Button)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new QRScannerCameraPreview(this, mCamera, previewCb, autoFocusCB);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera == null)
        {
            mCamera = getCameraInstance();
            if(mPreview == null)
            {
                FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
                preview.removeAllViews();
                mPreview = new QRScannerCameraPreview(this, mCamera, previewCb, autoFocusCB);
                preview.addView(mPreview);
            }
            else
            {
                FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
                preview.removeAllViews();
                mPreview = new QRScannerCameraPreview(this, mCamera, previewCb, autoFocusCB);
                preview.addView(mPreview);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }

    private void releaseCamera() {
        try{
            if (mCamera != null) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mPreview.getHolder().removeCallback(mPreview);
                mCamera.release();
                mCamera = null;
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    //scanText.setText("barcode result " + sym.getData());
                    //sample "com.ginko.app://user/"
                    String strDetectedQrVal = sym.getData();
                    if(strDetectedQrVal.toLowerCase().startsWith("com.ginko.app://user/"))
                    {
                        barcodeScanned = true;
                        System.out.println("----QRCode = "+strDetectedQrVal+" ------");
                        String encodedUserId = strDetectedQrVal.substring(21 , strDetectedQrVal.length());

                        System.out.println("----QRCode = "+encodedUserId+" ------");

                        try {
                            byte[] base64Decoded = Base64.decode(encodedUserId.getBytes("UTF-8"), Base64.DEFAULT);
                            //   Decoded text will be the same as the original text.
                            String decodedUserId = new String(base64Decoded, "UTF-8");
                            System.out.println("----QRCode = (" + decodedUserId + ") ------");

                            Message msg = new Message();
                            msg.arg1 = Integer.valueOf(decodedUserId);
                            msg.what = GOTO_PERMISSION_WITH_SCANNED_USERID;
                            mHandler.sendMessage(msg);

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }


                    } else {
                        barcodeScanned = false;

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCamera.setPreviewCallback(previewCb);
                                mCamera.startPreview();
                                previewing = true;
                                mCamera.autoFocus(autoFocusCB);
                            }
                        }, 200);

                    }
                }
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };
}
