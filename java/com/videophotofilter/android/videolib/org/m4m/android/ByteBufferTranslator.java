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

package com.videophotofilter.android.videolib.org.m4m.android;

import android.media.MediaCodec;
import com.videophotofilter.android.videolib.org.m4m.domain.IMediaCodec;

public class ByteBufferTranslator {
    public static  MediaCodec.BufferInfo from(IMediaCodec.BufferInfo bufferInfo) {
        MediaCodec.BufferInfo result = new MediaCodec.BufferInfo();
        result.flags = bufferInfo.flags;
        result.offset = bufferInfo.offset;
        result.size = bufferInfo.size;
        result.presentationTimeUs = bufferInfo.presentationTimeUs;
        return result;
    }
}
