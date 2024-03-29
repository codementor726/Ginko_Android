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

public enum SampleRate {
    SAMPLE_RATE_8000(8000),
    SAMPLE_RATE_16000(16000),
    SAMPLE_RATE_24000(24000),
    SAMPLE_RATE_22050(22050),
    SAMPLE_RATE_32000(32000),
    SAMPLE_RATE_44100(44100),
    SAMPLE_RATE_48000(48000);

    private final int sampleRate;

    private SampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getValue() {
        return sampleRate;
    }
}
