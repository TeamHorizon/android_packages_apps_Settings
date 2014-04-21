/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.android.settings.cyanogenmod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.DisplaySettings;
import com.android.settings.R;
import com.android.settings.Utils;

import java.util.Arrays;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    private static final String CHARGE_SETTINGS_PROP = "sys.charge.restored";
    private String fchargePath; 

    @Override
    public void onReceive(Context ctx, Intent intent) {
        fchargePath = ctx.getResources()
                  .getString(com.android.internal.R.string.config_fastChargePath);

        if (!fchargePath.isEmpty()) {
            if (SystemProperties.getBoolean(CHARGE_SETTINGS_PROP, false) == false
                    && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                SystemProperties.set(CHARGE_SETTINGS_PROP, "true");
                configureCharge(ctx);
            } else {
                SystemProperties.set(CHARGE_SETTINGS_PROP, "false");
            }
        }

    }

    private void configureCharge(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        boolean charge = prefs.getBoolean(Settings.System.FCHARGE_ENABLED, false);
        fchargePath = ctx.getResources()
                  .getString(com.android.internal.R.string.config_fastChargePath);

        Utils.fileWriteOneLine(fchargePath, charge ? "1" : "0");
        Log.d(TAG, "Fast Charge settings restored.");
    }
}
