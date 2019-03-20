/*
 * Copyright 2014-2016 Media for Mobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.videophotofilter.android.videolib.org.m4m.domain;

import com.videophotofilter.android.videolib.org.m4m.IVideoEffect;
import com.videophotofilter.android.videolib.org.m4m.domain.graphics.TextureRenderer;

public interface IPreview {
    void setActiveEffect(IVideoEffect effectApplied);

    PreviewContext getSharedContext();

    void updateCameraParameters();

    void start();

    void stop();

    void setListener(IOnFrameAvailableListener listener);

    void renderSurfaceFromFrameBuffer(int id);

    void requestRendering();

    void setOrientation(int screenOrientation);

    int getOrientation();

    public void setFillMode(TextureRenderer.FillMode fillMode);

    public TextureRenderer.FillMode getFillMode();
}