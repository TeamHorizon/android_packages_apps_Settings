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
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.util.AbstractAsyncSuCMDProcessor;
import com.android.settings.util.CMDProcessor;
import com.android.settings.util.Helpers;
import com.android.settings.Utils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SeekBarPreferenceCham;

public class LockscreenSettings extends SettingsPreferenceFragment 
	implements OnPreferenceChangeListener {

    private static final String TAG = "LockscreenSettings";

	private static final String CARRIERLABEL_ON_LOCKSCREEN="lock_screen_hide_carrier";
        private static final String KEY_LOCKSCREEN_BLUR_RADIUS = "lockscreen_blur_radius";

	private SwitchPreference mCarrierLabelOnLockScreen;
        private SeekBarPreferenceCham mBlurRadius;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen_settings);
		PreferenceScreen prefSet = getPreferenceScreen();

        mBlurRadius = (SeekBarPreferenceCham) findPreference(KEY_LOCKSCREEN_BLUR_RADIUS);
	mBlurRadius.setValue(Settings.System.getInt(
                getActivity().getContentResolver(),
		Settings.System.LOCKSCREEN_BLUR_RADIUS, 14));
	mBlurRadius.setOnPreferenceChangeListener(this);

		//CarrierLabel on LockScreen
        mCarrierLabelOnLockScreen = (SwitchPreference) findPreference(CARRIERLABEL_ON_LOCKSCREEN);
        if (!Utils.isWifiOnly(getActivity())) {
            mCarrierLabelOnLockScreen.setOnPreferenceChangeListener(this);

            boolean hideCarrierLabelOnLS = Settings.System.getInt(
                    getActivity().getContentResolver(),
                    Settings.System.LOCK_SCREEN_HIDE_CARRIER, 0) == 1;
            mCarrierLabelOnLockScreen.setChecked(hideCarrierLabelOnLS);
        } else {
            prefSet.removePreference(mCarrierLabelOnLockScreen);
        }
    }

	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        final String key = preference.getKey();
	if (preference == mBlurRadius) {
	    int width = ((Integer)objValue).intValue();
            Settings.System.putInt(resolver,
                Settings.System.LOCKSCREEN_BLUR_RADIUS, width);
            return true;
     	} else if (preference == mCarrierLabelOnLockScreen) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCK_SCREEN_HIDE_CARRIER,
                    (Boolean) objValue ? 1 : 0);
            Helpers.restartSystemUI();
            return true;
        }
        return false;
    }
}
