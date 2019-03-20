package com.ginko.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.ginko.R;
import com.stony.imagescan.ImageGroupActivity;
import com.stony.imagescan.ShowImageActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PhotoSelectorFragment extends Fragment implements OnClickListener {
	private static final int CAMERA_PROFILE_PIC_REQUEST = 1336;
	private static final int ALLOW_ACCESS_ALBUM = 100;
	
	private Intent nextActivity;

	public PhotoSelectorFragment(){}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_selector, container, false);
		view.findViewById(R.id.btnCamera).setOnClickListener(this);
		view.findViewById(R.id.btnCameraRoll).setOnClickListener(this);
		return view;
	}

	public void onBtnCamera(View v) {
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		Uri imageUri = Uri.fromFile(new File(RuntimeContext.getTempFolder(), "workupload.jpg"));
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		this.startActivityForResult(cameraIntent, CAMERA_PROFILE_PIC_REQUEST);
	}

	public void onBtnCameraRoll(View v) {
//		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Intent intent  = new Intent(this.getActivity(), ImageGroupActivity.class);
        startActivityForResult(intent, ALLOW_ACCESS_ALBUM);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) { // RESULT_OK ��ϵͳ�Զ����һ������
			Logger.error( "ActivityResult resultCode error");
			return;
		}
		String photoPath = "";
		// take photo just now.
		if (requestCode == CAMERA_PROFILE_PIC_REQUEST) {
			// photoPath= savePhoto(data);
			photoPath = RuntimeContext.getTempFolder() + "/" + "workupload.jpg";
		} else if (requestCode == ALLOW_ACCESS_ALBUM) {
            Bundle bundle = data.getExtras();
            ArrayList<String> selectedImages = bundle.getStringArrayList(ShowImageActivity.SELECTED_IMAGES);
            Logger.debug("select image:" + selectedImages.get(0));
            photoPath = selectedImages.get(0);
		}

		if (photoPath.isEmpty()) {
			Logger.error( "Can't get photo to edit");
			return;
		}

		Intent intent = this.getNextActivity();
		intent.putExtra("photo_path", photoPath);
		startActivity(intent);
	}

	private void savePhoto(Intent data) {
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // ���sd�Ƿ����
			Logger.info( "SD card is not avaiable/writeable right now.");
			return;
		}
		String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.US)) + ".jpg";
		// Toast.makeText(this, name, Toast.LENGTH_LONG).show();
		Bundle bundle = data.getExtras();
		Bitmap bitmap = (Bitmap) bundle.get("data");// ��ȡ���ص���ݣ���ת��ΪBitmapͼƬ��ʽ

		FileOutputStream b = null;
		// ???????????????????????????????Ϊʲô����ֱ�ӱ�����ϵͳ���λ���أ�����������������������
		File file = new File("/sdcard/myImage/");
		file.mkdirs();// �����ļ���
		String fileName = "/sdcard/myImage/" + name;

		try {
			b = new FileOutputStream(fileName);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// �����д���ļ�
		} catch (FileNotFoundException e) {
			Logger.error(e);
		} finally {
			try {
				b.flush();
				b.close();
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnCamera) {
			this.onBtnCamera(v);
		} else if (id == R.id.btnCameraRoll) {
			this.onBtnCameraRoll(v);
		}
	}

	public Intent getNextActivity() {
		return nextActivity;
	}

	public void setNextActivity(Intent nextActivity) {
		this.nextActivity = nextActivity;
	}
}
