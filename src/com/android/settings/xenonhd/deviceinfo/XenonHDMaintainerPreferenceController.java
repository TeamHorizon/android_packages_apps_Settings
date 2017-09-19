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
import android.os.Build;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;

import com.android.settings.core.PreferenceController;
import com.android.settings.core.lifecycle.Lifecycle;
import com.android.settings.core.lifecycle.LifecycleObserver;
import com.android.settings.core.lifecycle.events.OnResume;
import com.android.settingslib.RestrictedLockUtils;


public class XenonHDMaintainerPreferenceController extends PreferenceController
        implements LifecycleObserver, OnResume {

    private static final String TAG = "XenonHDMaintainerPref";
    private static final String KEY_XENONHD_MAINTAINER = "xenonhd_maintainer";

    private final UserManager mUserManager;

    private RestrictedLockUtils.EnforcedAdmin mFunDisallowedAdmin;
    private boolean mFunDisallowedBySystem;

    public XenonHDMaintainerPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        String buildtype = SystemProperties.get("ro.xenonhd.type","Unofficial");
        final Preference pref = screen.findPreference(KEY_XENONHD_MAINTAINER);
        if (buildtype.equalsIgnoreCase("Unofficial")) {
            removePreference(screen, KEY_XENONHD_MAINTAINER);
        } else {
            pref.setSummary(Build.VERSION.XENONHD_MAINTAINER);
        }
    }

    @Override
    public String getPreferenceKey() {
        return KEY_XENONHD_MAINTAINER;
    }

    @Override
    public void onResume() {
        mFunDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(
                mContext, UserManager.DISALLOW_FUN, UserHandle.myUserId());
        mFunDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(
                mContext, UserManager.DISALLOW_FUN, UserHandle.myUserId());
    }
}
