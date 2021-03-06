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
package com.android.settings.rr;

import android.app.Activity;
import android.app.ThemeManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.accessibility.ToggleFontSizePreferenceFragment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.display.ThemePreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.android.settings.rr.SeekBarPreference;
import com.android.settings.PreviewSeekBarPreferenceFragment;

public class UISettings extends SettingsPreferenceFragment implements
    Preference.OnPreferenceChangeListener {
    private static final String TAG = "UI";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_DOZE_FRAGMENT = "doze_fragment";
    private static final String RR_INCALL = "rr_incall";
    private static final String KEY_THEME = "theme";

    private Preference mFontSizePref;
    private PreferenceScreen mDozeFragement;
    private FingerprintManager mFingerprintManager;
    private PreferenceScreen mFingerprint;
    private PreferenceScreen mIncall;
    private ThemePreference mThemePreference;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.RESURRECTED;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity(); 
		ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.rr_ui_settings);
 		mFontSizePref = findPreference(KEY_FONT_SIZE);

        mDozeFragement = (PreferenceScreen) findPreference(KEY_DOZE_FRAGMENT);
        if (!isDozeAvailable(activity)) {
            getPreferenceScreen().removePreference(mDozeFragement);
            mDozeFragement = null;
        }

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);        
        mFingerprint = (PreferenceScreen) findPreference("rr_fp");
        if (!mFingerprintManager.isHardwareDetected()){
             getPreferenceScreen().removePreference(mFingerprint);
        }

        PreferenceScreen mIncall = (PreferenceScreen) findPreference(RR_INCALL);
        if (!isVoiceCapable(getActivity())) {
            getPreferenceScreen().removePreference(mIncall);
        }

        mThemePreference = (ThemePreference) findPreference(KEY_THEME);
        if (mThemePreference != null) {
            final int accentColorValue = Settings.Secure.getInt(getContext().getContentResolver(),
                    Settings.Secure.THEME_ACCENT_COLOR, 0);
            final int primaryColorValue = Settings.Secure.getInt(getContext().getContentResolver(),
                    Settings.Secure.THEME_PRIMARY_COLOR, 0);
            mThemePreference.setSummary(PreviewSeekBarPreferenceFragment.getInfoText(getContext(),
                    false, accentColorValue, primaryColorValue) + ", " +
                    PreviewSeekBarPreferenceFragment.getInfoText(getContext(), true,
                    accentColorValue, primaryColorValue));
            if (ThemeManager.isOverlayEnabled()) {
                mThemePreference.setEnabled(false);
                mThemePreference.setSummary(R.string.oms_enabled);
            }
        }
    }

    private void updateFontSizeSummary() {
        final Context context = mFontSizePref.getContext();
        final float currentScale = Settings.System.getFloat(context.getContentResolver(),
                Settings.System.FONT_SCALE, 1.0f);
        final Resources res = context.getResources();
        final String[] entries = res.getStringArray(R.array.entries_font_size_percent);
        final String[] strEntryValues = res.getStringArray(R.array.entryvalues_font_size);
        final int index = ToggleFontSizePreferenceFragment.fontSizeValueToIndex(currentScale,
                strEntryValues);
        mFontSizePref.setSummary(entries[index]);
    }

    private static boolean isDozeAvailable(Context context) {
        String name = Build.IS_DEBUGGABLE ? SystemProperties.get("debug.doze.component") : null;
        if (TextUtils.isEmpty(name)) {
            name = context.getResources().getString(
                    com.android.internal.R.string.config_dozeComponent);
        }
        return !TextUtils.isEmpty(name);
     }

    @Override
    public void onResume() {
        super.onResume();
        updateFontSizeSummary();
        updateThemesPref();
	}

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (mThemePreference != null) {
            final int accentColorValue = Settings.Secure.getInt(getContext().getContentResolver(),
                    Settings.Secure.THEME_ACCENT_COLOR, 0);
            final int primaryColorValue = Settings.Secure.getInt(getContext().getContentResolver(),
                    Settings.Secure.THEME_PRIMARY_COLOR, 0);
            mThemePreference.setSummary(PreviewSeekBarPreferenceFragment.getInfoText(getContext(),
                    false, accentColorValue, primaryColorValue) + ", " +
                    PreviewSeekBarPreferenceFragment.getInfoText(getContext(), true,
                    accentColorValue, primaryColorValue));
            if (ThemeManager.isOverlayEnabled()) {
                mThemePreference.setEnabled(false);
                mThemePreference.setSummary(R.string.oms_enabled);
            }
        }

        return true;
    }

    public void updateThemesPref() {
        if (mThemePreference != null) {
            final int accentColorValue = Settings.Secure.getInt(getContext().getContentResolver(),
                    Settings.Secure.THEME_ACCENT_COLOR, 0);
            final int primaryColorValue = Settings.Secure.getInt(getContext().getContentResolver(),
                    Settings.Secure.THEME_PRIMARY_COLOR, 0);
            mThemePreference.setSummary(PreviewSeekBarPreferenceFragment.getInfoText(getContext(),
                    false, accentColorValue, primaryColorValue) + ", " +
                    PreviewSeekBarPreferenceFragment.getInfoText(getContext(), true,
                    accentColorValue, primaryColorValue));
            if (ThemeManager.isOverlayEnabled()) {
                mThemePreference.setEnabled(false);
                mThemePreference.setSummary(R.string.oms_enabled);
            }
       }
    }

    /**
     * Returns whether the device is voice-capable (meaning, it is also a phone).
     */
    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephony =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephony != null && telephony.isVoiceCapable();
    }
}
