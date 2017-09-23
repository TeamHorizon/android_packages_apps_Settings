/*
 * Copyright (C) 2017 The Android Open Source Project
 * Copyright (C) 2017 TeamHorizon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.xenonhd.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;

import com.android.settings.core.PreferenceController;

public class XenonOTAPreferenceController extends PreferenceController {

    private static final String TAG = "XenonOTAPref";
    private static final String KEY_XENONOTA = "xenonota";

    public XenonOTAPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public boolean isXenonOTA() {
        PackageManager pm = mContext.getPackageManager();
        try {
            pm.getApplicationInfo("com.xenonota", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (!isXenonOTA()) {
            removePreference(screen, KEY_XENONOTA);
        }
    }

    @Override
    public String getPreferenceKey() {
        return KEY_XENONOTA;
    }
}
