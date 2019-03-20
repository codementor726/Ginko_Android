package com.ginko.common;

import java.util.ArrayList;
import java.util.List;

import android.widget.ImageButton;

public class ImageButtonTab {
	private List<_ImageButton> buttons = new ArrayList<_ImageButton>();

	public void selectButton(ImageButton button) {
		for (_ImageButton _imgBtn : buttons) {
			if (button.getId() == _imgBtn.button.getId()) {
				_imgBtn.button.setImageResource(_imgBtn.selectedImageId);
			} else {
				_imgBtn.button.setImageResource(_imgBtn.unselectedImageId);
			}
		}
	}

	public void addButton(ImageButton button, int selectedImageId,
			int unselectedImageId) {
		_ImageButton _img = new _ImageButton();
		_img.button = button;
		_img.selectedImageId = selectedImageId;
		_img.unselectedImageId = unselectedImageId;
		this.buttons.add(_img);

	}

	public void clear() {
		this.buttons.clear();
	}

	private class _ImageButton {
		private ImageButton button;
		private int selectedImageId;
		private int unselectedImageId;
	}
}
