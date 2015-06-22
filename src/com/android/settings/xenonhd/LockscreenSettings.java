/*
* Copyright (C) 2014 Team Horizon
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.settings.xenonhd;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerImpl;
import android.widget.Toast;

import com.android.settings.R;

public class LockscreenSettings extends SettingsPreferenceFragment implements
Preference.OnPreferenceChangeListener {

    private static final String TAG = "LockscreenSettings";

    private Context mContext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.lockscreen_settings);
        Activity activity = getActivity();

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mContext = getActivity().getApplicationContext();

    }
}
