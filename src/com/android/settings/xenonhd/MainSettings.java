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

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Build;
import com.android.settings.dashboard.DashboardSummary;
import com.android.settings.util.AbstractAsyncSuCMDProcessor;
import com.android.settings.util.CMDProcessor;
import com.android.settings.util.Helpers;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;
import com.android.settings.util.Helpers;
import dalvik.system.VMRuntime;
import android.preference.PreferenceActivity;
import android.view.View;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.widget.LockPatternUtils;

import com.android.settings.preference.CustomSeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.List;

import java.io.File;
import java.io.IOException;
import java.io.DataOutputStream;

public class MainSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final int MY_USER_ID = UserHandle.myUserId();

    private static final String TAG = "MainSettings";

    private static final String SELINUX = "selinux";

    private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
    private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";
    private static final String PREF_COLUMNS = "qs_columns";
    private static final String PREF_QS_EASY_TOGGLE = "qs_easy_toggle";

    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";

    private static final String KEY_DASHBOARD_PORTRAIT_COLUMNS = "dashboard_portrait_columns";
    private static final String KEY_DASHBOARD_LANDSCAPE_COLUMNS = "dashboard_landscape_columns";

    private static final String PREF_SHOW_EMERGENCY_BUTTON = "show_emergency_button";
    private static final String PREF_LOCKSCREEN_BATTERY_INFO = "lockscreen_battery_info";
    private static final String CURRENT_NOW = "/sys/class/power_supply/battery/current_now";

    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String FP_UNLOCK_KEYSTORE = "fp_unlock_keystore";
    private static final String FP_MAX_FAILED_ATTEMPTS = "fp_max_failed_attempts";

    private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
    private static final String CLOCK_FONT_SIZE  = "lockclock_font_size";
    private static final String DATE_FONT_SIZE  = "lockdate_font_size";
    private static final String LOCK_DATE_FONTS = "lock_date_fonts";

    private static final String STATUS_BAR_BATTERY_SAVER_COLOR = "status_bar_battery_saver_color";

    private SwitchPreference mConfig;

    private SwitchPreference mSelinux;

    private CustomSeekBarPreference mRowsPortrait;
    private CustomSeekBarPreference mRowsLandscape;
    private CustomSeekBarPreference mQsColumns;
    private SwitchPreference mEasyToggle;

    private SwitchPreference mRecentsClearAll;
    private ListPreference mRecentsClearAllLocation;

    private CustomSeekBarPreference mDashboardPortraitColumns;
    private CustomSeekBarPreference mDashboardLandscapeColumns;

    private SwitchPreference mEmergencyButton;
    private SwitchPreference mLockscreenBatteryInfo;

    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
    private SwitchPreference mFpKeystore;
    private ListPreference maxFailedAttempts;

    ListPreference mDateFonts;
    ListPreference mLockClockFonts;
    private CustomSeekBarPreference mClockFontSize;
    private CustomSeekBarPreference mDateFontSize;

    private ColorPickerPreference mBatterySaverColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.xenonhd_main_settings);

        final PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();
        final LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());

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

        mDashboardPortraitColumns = (CustomSeekBarPreference) findPreference(KEY_DASHBOARD_PORTRAIT_COLUMNS);
        int columnsPortrait = Settings.System.getInt(resolver,
                Settings.System.DASHBOARD_PORTRAIT_COLUMNS, DashboardSummary.mNumColumns);
        mDashboardPortraitColumns.setValue(columnsPortrait / 1);
        mDashboardPortraitColumns.setOnPreferenceChangeListener(this);

        mDashboardLandscapeColumns = (CustomSeekBarPreference) findPreference(KEY_DASHBOARD_LANDSCAPE_COLUMNS);
        int columnsLandscape = Settings.System.getInt(resolver,
                Settings.System.DASHBOARD_LANDSCAPE_COLUMNS, 2);
        mDashboardLandscapeColumns.setValue(columnsLandscape / 1);
        mDashboardLandscapeColumns.setOnPreferenceChangeListener(this);

        mEasyToggle = (SwitchPreference) findPreference(PREF_QS_EASY_TOGGLE);
        mEasyToggle.setOnPreferenceChangeListener(this);
        mEasyToggle.setChecked((Settings.Secure.getInt(resolver,
                Settings.Secure.QS_EASY_TOGGLE, 0) == 1));

        mEmergencyButton = (SwitchPreference) findPreference(PREF_SHOW_EMERGENCY_BUTTON);
        if (lockPatternUtils.isSecure(MY_USER_ID)) {
            mEmergencyButton.setChecked((Settings.System.getInt(resolver,
                Settings.System.SHOW_EMERGENCY_BUTTON, 1) == 1));
            mEmergencyButton.setOnPreferenceChangeListener(this);
        }
        
        // We need to remove the lockscreen battery info if the device is not a Qualcomm device
        mLockscreenBatteryInfo = (SwitchPreference) findPreference(PREF_LOCKSCREEN_BATTERY_INFO);
        File current_now = new File(CURRENT_NOW);
        if (!current_now.exists()) {
            prefSet.removePreference(mLockscreenBatteryInfo);
        }

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        mFpKeystore = (SwitchPreference) findPreference(FP_UNLOCK_KEYSTORE);

        // fp max failed attempts
        maxFailedAttempts = (ListPreference) findPreference(FP_MAX_FAILED_ATTEMPTS);
        int set = Settings.System.getIntForUser(resolver,
                Settings.System.FP_MAX_FAILED_ATTEMPTS, 5, UserHandle.USER_CURRENT);
        maxFailedAttempts.setValue(String.valueOf(set));
        maxFailedAttempts.setSummary(maxFailedAttempts.getEntry());
        maxFailedAttempts.setOnPreferenceChangeListener(this);

        mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_CLOCK_FONTS, 0)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);

        mClockFontSize = (CustomSeekBarPreference) findPreference(CLOCK_FONT_SIZE);
        mClockFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKCLOCK_FONT_SIZE, 88));
        mClockFontSize.setOnPreferenceChangeListener(this);

        mDateFontSize = (CustomSeekBarPreference) findPreference(DATE_FONT_SIZE);
        mDateFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKDATE_FONT_SIZE,14));
        mDateFontSize.setOnPreferenceChangeListener(this);

        mDateFonts = (ListPreference) findPreference(LOCK_DATE_FONTS);
        mDateFonts.setValue(String.valueOf(Settings.System.getInt(
                resolver, Settings.System.LOCK_DATE_FONTS, 4)));
        mDateFonts.setSummary(mDateFonts.getEntry());
        mDateFonts.setOnPreferenceChangeListener(this);

        int batterySaverColor = Settings.Secure.getInt(resolver,
                Settings.Secure.STATUS_BAR_BATTERY_SAVER_COLOR, 0xfff4511e);
        mBatterySaverColor = (ColorPickerPreference) findPreference("status_bar_battery_saver_color");
        mBatterySaverColor.setNewPreviewColor(batterySaverColor);
        mBatterySaverColor.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        int defaultValue;

        mRowsPortrait = (CustomSeekBarPreference) findPreference(PREF_ROWS_PORTRAIT);
        int rowsPortrait = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.QS_ROWS_PORTRAIT, 3);
        mRowsPortrait.setValue(rowsPortrait / 1);
        mRowsPortrait.setOnPreferenceChangeListener(this);

        defaultValue = getResources().getInteger(com.android.internal.R.integer.config_qs_num_rows_landscape_default);
        mRowsLandscape = (CustomSeekBarPreference) findPreference(PREF_ROWS_LANDSCAPE);
        int rowsLandscape = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.QS_ROWS_LANDSCAPE, defaultValue);
        mRowsLandscape.setValue(rowsLandscape / 1);
        mRowsLandscape.setOnPreferenceChangeListener(this);

        mQsColumns = (CustomSeekBarPreference) findPreference(PREF_COLUMNS);
        int columnsQs = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.QS_COLUMNS, 3);
        mQsColumns.setValue(columnsQs / 1);
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
                setSelinuxEnabled("true");
                mSelinux.setSummary(R.string.selinux_enforcing_title);
            } else if (objValue.toString().equals("false")) {
                CMDProcessor.runSuCommand("setenforce 0");
                setSelinuxEnabled("false");
                mSelinux.setSummary(R.string.selinux_permissive_title);
            }
            return true;
        } else if (preference == mRowsPortrait) {
            int rowsPortrait = (Integer) objValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.QS_ROWS_PORTRAIT, rowsPortrait * 1);
            return true;
        } else if (preference == mRowsLandscape) {
            int rowsLandscape = (Integer) objValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.QS_ROWS_LANDSCAPE, rowsLandscape * 1);
            return true;
        } else if (preference == mQsColumns) {
            int columnsQs = (Integer) objValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.QS_COLUMNS, columnsQs * 1);
            return true;
        } else if (preference == mRecentsClearAllLocation) {
            int location = Integer.valueOf((String) objValue);
            index = mRecentsClearAllLocation.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
            return true;
        } else if (preference == mDashboardPortraitColumns) {
            int columnsPortrait = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.DASHBOARD_PORTRAIT_COLUMNS, columnsPortrait * 1);
            return true;
        } else if (preference == mDashboardLandscapeColumns) {
            int columnsLandscape = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.DASHBOARD_LANDSCAPE_COLUMNS, columnsLandscape * 1);
            return true;
        } else if  (preference == mEasyToggle) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.QS_EASY_TOGGLE, checked ? 1:0);
            return true;
        } else if  (preference == mEmergencyButton) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_EMERGENCY_BUTTON, checked ? 1:0);
            return true;
        } else if (preference == mLockClockFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_CLOCK_FONTS,
                    Integer.valueOf((String) objValue));
            mLockClockFonts.setValue(String.valueOf(objValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            return true;
        } else if (preference == mClockFontSize) {
            int top = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKCLOCK_FONT_SIZE, top*1);
            return true;
        } else if (preference == mDateFontSize) {
            int top = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKDATE_FONT_SIZE, top*1);
            return true;
        } else if (preference == mDateFonts) {
            Settings.System.putInt(resolver, Settings.System.LOCK_DATE_FONTS,
                    Integer.valueOf((String) objValue));
            mDateFonts.setValue(String.valueOf(objValue));
            mDateFonts.setSummary(mDateFonts.getEntry());
            return true;
        } else if (preference == maxFailedAttempts) {
            int set = Integer.valueOf((String) objValue);
            index = maxFailedAttempts.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.FP_MAX_FAILED_ATTEMPTS, set, UserHandle.USER_CURRENT);
            maxFailedAttempts.setSummary(maxFailedAttempts.getEntries()[index]);
            return true;
        } else if (preference.equals(mBatterySaverColor)) {
            int color = ((Integer) objValue).intValue();
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.STATUS_BAR_BATTERY_SAVER_COLOR, color);
            return true;
        } 
        return false;
    }
}
