/*Copyright (C) 2015 The ResurrectionRemix Project
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
*/
package com.android.settings.xenonhd;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Build;
import com.android.settings.util.AbstractAsyncSuCMDProcessor;
import com.android.settings.util.CMDProcessor;
import com.android.settings.util.Helpers;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;
import com.android.settings.util.Helpers;
import dalvik.system.VMRuntime;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.List;

import java.io.File;
import java.io.IOException;
import java.io.DataOutputStream;

public class MainSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "MainSettings";

    private static final String SELINUX = "selinux";

    private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
    private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";
    private static final String PREF_COLUMNS = "qs_columns";

    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";

    private SwitchPreference mConfig;

    private SwitchPreference mSelinux;

    private ListPreference mRowsPortrait;
    private ListPreference mRowsLandscape;
    private ListPreference mQsColumns;

    private SwitchPreference mRecentsClearAll;
    private ListPreference mRecentsClearAllLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.xenonhd_main_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
	  	final ContentResolver resolver = getActivity().getContentResolver();

        //SELinux
        mSelinux = (SwitchPreference) findPreference(SELINUX);
        mSelinux.setOnPreferenceChangeListener(this);

 	if (CMDProcessor.runShellCommand("getenforce").getStdout().contains("Enforcing")) {
            mSelinux.setChecked(true);
            mSelinux.setSummary(R.string.selinux_enforcing_title);
        } else {
            mSelinux.setChecked(false);
            mSelinux.setSummary(R.string.selinux_permissive_title);
        }

        // clear all location
        mRecentsClearAllLocation = (ListPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_LOCATION);
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
        mRecentsClearAllLocation.setValue(String.valueOf(location));
        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        int defaultValue;

        mRowsPortrait = (ListPreference) findPreference(PREF_ROWS_PORTRAIT);
        int rowsPortrait = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.QS_ROWS_PORTRAIT, 3);
        mRowsPortrait.setValue(String.valueOf(rowsPortrait));
        mRowsPortrait.setSummary(mRowsPortrait.getEntry());
        mRowsPortrait.setOnPreferenceChangeListener(this);

        defaultValue = getResources().getInteger(com.android.internal.R.integer.config_qs_num_rows_landscape_default);
        mRowsLandscape = (ListPreference) findPreference(PREF_ROWS_LANDSCAPE);
        int rowsLandscape = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.QS_ROWS_LANDSCAPE, defaultValue);
        mRowsLandscape.setValue(String.valueOf(rowsLandscape));
        mRowsLandscape.setSummary(mRowsLandscape.getEntry());
        mRowsLandscape.setOnPreferenceChangeListener(this);

        mQsColumns = (ListPreference) findPreference(PREF_COLUMNS);
        int columnsQs = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.QS_COLUMNS, 3);
        mQsColumns.setValue(String.valueOf(columnsQs));
        mQsColumns.setSummary(mQsColumns.getEntry());
        mQsColumns.setOnPreferenceChangeListener(this);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.APPLICATION;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

     private void setSelinuxEnabled(String status) {
         SharedPreferences.Editor editor = getContext().getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
         editor.putString("selinux", status);
         editor.apply();
     }

     @Override
     public boolean onPreferenceChange(Preference preference, Object objValue) {

        int intValue;
        int index;

     ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mSelinux) {
            if (objValue.toString().equals("true")) {
                CMDProcessor.runSuCommand("setenforce 1");
                mSelinux.setSummary(R.string.selinux_enforcing_title);
            } else if (objValue.toString().equals("false")) {
                CMDProcessor.runSuCommand("setenforce 0");
                mSelinux.setSummary(R.string.selinux_permissive_title);
            }
            return true;
        } else if (preference == mRowsPortrait) {
            intValue = Integer.valueOf((String) objValue);
            index = mRowsPortrait.findIndexOfValue((String) objValue);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.QS_ROWS_PORTRAIT, intValue);
            preference.setSummary(mRowsPortrait.getEntries()[index]);
            return true;
        } else if (preference == mRowsLandscape) {
            intValue = Integer.valueOf((String) objValue);
            index = mRowsLandscape.findIndexOfValue((String) objValue);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.QS_ROWS_LANDSCAPE, intValue);
            preference.setSummary(mRowsLandscape.getEntries()[index]);
            return true;
        } else if (preference == mQsColumns) {
            intValue = Integer.valueOf((String) objValue);
            index = mQsColumns.findIndexOfValue((String) objValue);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.QS_COLUMNS, intValue);
            preference.setSummary(mQsColumns.getEntries()[index]);
            return true;
        } else if (preference == mRecentsClearAllLocation) {
            int location = Integer.valueOf((String) objValue);
            index = mRecentsClearAllLocation.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
            return true;
        }
        return false;
     } 
}
