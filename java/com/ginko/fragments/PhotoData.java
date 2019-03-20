package com.ginko.fragments;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class PhotoData {
	private GPUImage picture;
	private GPUImageView imageView;
	// private imageOrientation;
	private GPUImageBrightnessFilter brightness;
	private float transparency;

	public GPUImage getPicture() {
		return picture;
	}

	public void setPicture(GPUImage picture) {
		this.picture = picture;
	}

	public GPUImageView getImageView() {
		return imageView;
	}

	public void setImageView(GPUImageView imageView) {
		this.imageView = imageView;
	}

	public GPUImageBrightnessFilter getBrightness() {
		return brightness;
	}

	public void setBrightness(GPUImageBrightnessFilter brightness) {
		this.brightness = brightness;
	}

	public float getTransparency() {
		return transparency;
	}

	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

}
