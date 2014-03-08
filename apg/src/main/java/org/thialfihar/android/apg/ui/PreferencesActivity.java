/*
 * Copyright (C) 2010 Thialfihar <thi@thialfihar.org>
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

package org.thialfihar.android.apg.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.openpgp.PGPEncryptedData;

import org.thialfihar.android.apg.Constants;
import org.thialfihar.android.apg.Id;
import org.thialfihar.android.apg.R;
import org.thialfihar.android.apg.helper.Preferences;
import org.thialfihar.android.apg.ui.widget.IntegerListPreference;

import java.util.List;

@SuppressLint("NewApi")
public class PreferencesActivity extends PreferenceActivity {

    public final static String ACTION_PREFS_GEN = "org.thialfihar.android.apg.ui.PREFS_GEN";
    public final static String ACTION_PREFS_ADV = "org.thialfihar.android.apg.ui.PREFS_ADV";

    private PreferenceScreen mKeyServerPreference = null;
    private static Preferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreferences = Preferences.getPreferences(this);
        super.onCreate(savedInstanceState);

//        final ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(false);
//        actionBar.setHomeButtonEnabled(false);

        //addPreferencesFromResource(R.xml.preferences);
        String action = getIntent().getAction();

        if (action != null && action.equals(ACTION_PREFS_GEN)) {
            addPreferencesFromResource(R.xml.gen_preferences);

            initializePassPassPhraceCacheTtl(
                    (IntegerListPreference) findPreference(Constants.pref.PASSPHRASE_CACHE_TTL));

            mKeyServerPreference = (PreferenceScreen) findPreference(Constants.pref.KEY_SERVERS);
            String servers[] = mPreferences.getKeyServers();
            mKeyServerPreference.setSummary(getResources().getQuantityString(R.plurals.n_key_servers,
                    servers.length, servers.length));
            mKeyServerPreference
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            Intent intent = new Intent(PreferencesActivity.this,
                                    PreferencesKeyServerActivity.class);
                            intent.putExtra(PreferencesKeyServerActivity.EXTRA_KEY_SERVERS,
                                    mPreferences.getKeyServers());
                            startActivityForResult(intent, Id.request.key_server_preference);
                            return false;
                        }
                    });

        } else if (action != null && action.equals(ACTION_PREFS_ADV)) {
            addPreferencesFromResource(R.xml.adv_preferences);

            initializeEncryptionAlgorithm(
                    (IntegerListPreference) findPreference(Constants.pref.DEFAULT_ENCRYPTION_ALGORITHM));

            int[] valueIds = new int[] { Id.choice.compression.none, Id.choice.compression.zip,
                    Id.choice.compression.zlib, Id.choice.compression.bzip2, };
            String[] entries = new String[] {
                    getString(R.string.choice_none) + " (" + getString(R.string.compression_fast) + ")",
                    "ZIP (" + getString(R.string.compression_fast) + ")",
                    "ZLIB (" + getString(R.string.compression_fast) + ")",
                    "BZIP2 (" + getString(R.string.compression_very_slow) + ")", };
            String[] values = new String[valueIds.length];
            for (int i = 0; i < values.length; ++i) {
                values[i] = "" + valueIds[i];
            }

            initializeHashAlgorithm(
                    (IntegerListPreference) findPreference(Constants.pref.DEFAULT_HASH_ALGORITHM),
                                            valueIds, entries, values);

            initializeMessageCompression(
                    (IntegerListPreference) findPreference(Constants.pref.DEFAULT_MESSAGE_COMPRESSION),
                                            valueIds, entries, values);

            initializeFileCompression(
                    (IntegerListPreference) findPreference(Constants.pref.DEFAULT_FILE_COMPRESSION),
                                            entries, values);

            initializeAsciiArmor((CheckBoxPreference) findPreference(Constants.pref.DEFAULT_ASCII_ARMOR));

            initializeForceV3Signatures((CheckBoxPreference) findPreference(Constants.pref.FORCE_V3_SIGNATURES));

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Load the legacy preferences headers
            addPreferencesFromResource(R.xml.preference_headers_legacy);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Id.request.key_server_preference: {
                if (resultCode == RESULT_CANCELED || data == null) {
                    return;
                }
                String servers[] = data
                        .getStringArrayExtra(PreferencesKeyServerActivity.EXTRA_KEY_SERVERS);
                mPreferences.setKeyServers(servers);
                mKeyServerPreference.setSummary(getResources().getQuantityString(
                        R.plurals.n_key_servers, servers.length, servers.length));
                break;
            }

            default: {
                super.onActivityResult(requestCode, resultCode, data);
                break;
            }
        }
    }

    /* Called only on Honeycomb and later */
    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    /** This fragment shows the general preferences in android 3.0+ */
    public static class GeneralPrefsFragment extends PreferenceFragment {

        private PreferenceScreen mKeyServerPreference = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.gen_preferences);

            initializePassPassPhraceCacheTtl(
                    (IntegerListPreference) findPreference(Constants.pref.PASSPHRASE_CACHE_TTL));

            mKeyServerPreference = (PreferenceScreen) findPreference(Constants.pref.KEY_SERVERS);
            String servers[] = mPreferences.getKeyServers();
            mKeyServerPreference.setSummary(getResources().getQuantityString(R.plurals.n_key_servers,
                    servers.length, servers.length));
            mKeyServerPreference
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            Intent intent = new Intent(getActivity(),
                                    PreferencesKeyServerActivity.class);
                            intent.putExtra(PreferencesKeyServerActivity.EXTRA_KEY_SERVERS,
                                    mPreferences.getKeyServers());
                            startActivityForResult(intent, Id.request.key_server_preference);
                            return false;
                        }
                    });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case Id.request.key_server_preference: {
                    if (resultCode == RESULT_CANCELED || data == null) {
                        return;
                    }
                    String servers[] = data
                            .getStringArrayExtra(PreferencesKeyServerActivity.EXTRA_KEY_SERVERS);
                    mPreferences.setKeyServers(servers);
                    mKeyServerPreference.setSummary(getResources().getQuantityString(
                            R.plurals.n_key_servers, servers.length, servers.length));
                    break;
                }

                default: {
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
                }
            }
        }
    }

    /** This fragment shows the advanced preferences in android 3.0+ */
    public static class AdvancedPrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.adv_preferences);

            initializeEncryptionAlgorithm(
                    (IntegerListPreference) findPreference(Constants.pref.DEFAULT_ENCRYPTION_ALGORITHM));

            int[] valueIds = new int[] { Id.choice.compression.none, Id.choice.compression.zip,
                    Id.choice.compression.zlib, Id.choice.compression.bzip2, };
            String[] entries = new String[] {
                    getString(R.string.choice_none) + " (" + getString(R.string.compression_fast) + ")",
                    "ZIP (" + getString(R.string.compression_fast) + ")",
                    "ZLIB (" + getString(R.string.compression_fast) + ")",
                    "BZIP2 (" + getString(R.string.compression_very_slow) + ")", };
            String[] values = new String[valueIds.length];
            for (int i = 0; i < values.length; ++i) {
                values[i] = "" + valueIds[i];
            }

            initializeHashAlgorithm(
                    (IntegerListPreference) findPreference(Constants.pref.DEFAULT_HASH_ALGORITHM),
                                                                    valueIds, entries, values);

            initializeMessageCompression(
                    (IntegerListPreference) findPreference(Constants.pref.DEFAULT_MESSAGE_COMPRESSION),
                                                                valueIds, entries, values);

            initializeFileCompression(
                    (IntegerListPreference) findPreference(Constants.pref.DEFAULT_FILE_COMPRESSION),
                    entries, values);

            initializeAsciiArmor((CheckBoxPreference) findPreference(Constants.pref.DEFAULT_ASCII_ARMOR));

            initializeForceV3Signatures((CheckBoxPreference) findPreference(Constants.pref.FORCE_V3_SIGNATURES));
        }
    }

    protected boolean isValidFragment (String fragmentName) {
        return AdvancedPrefsFragment.class.getName().equals(fragmentName)
                || GeneralPrefsFragment.class.getName().equals(fragmentName)
                || super.isValidFragment(fragmentName);
    }

    private static void initializePassPassPhraceCacheTtl(final IntegerListPreference mPassphraseCacheTtl) {
        mPassphraseCacheTtl.setValue("" + mPreferences.getPassphraseCacheTtl());
        mPassphraseCacheTtl.setSummary(mPassphraseCacheTtl.getEntry());
        mPassphraseCacheTtl
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        mPassphraseCacheTtl.setValue(newValue.toString());
                        mPassphraseCacheTtl.setSummary(mPassphraseCacheTtl.getEntry());
                        mPreferences.setPassphraseCacheTtl(Integer.parseInt(newValue.toString()));
                        return false;
                    }
                });
    }

    private static void initializeEncryptionAlgorithm(final IntegerListPreference mEncryptionAlgorithm) {
        int valueIds[] = { PGPEncryptedData.AES_128, PGPEncryptedData.AES_192,
                PGPEncryptedData.AES_256, PGPEncryptedData.BLOWFISH, PGPEncryptedData.TWOFISH,
                PGPEncryptedData.CAST5, PGPEncryptedData.DES, PGPEncryptedData.TRIPLE_DES,
                PGPEncryptedData.IDEA, };
        String entries[] = { "AES-128", "AES-192", "AES-256", "Blowfish", "Twofish", "CAST5",
                "DES", "Triple DES", "IDEA", };
        String values[] = new String[valueIds.length];
        for (int i = 0; i < values.length; ++i) {
            values[i] = "" + valueIds[i];
        }
        mEncryptionAlgorithm.setEntries(entries);
        mEncryptionAlgorithm.setEntryValues(values);
        mEncryptionAlgorithm.setValue("" + mPreferences.getDefaultEncryptionAlgorithm());
        mEncryptionAlgorithm.setSummary(mEncryptionAlgorithm.getEntry());
        mEncryptionAlgorithm
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        mEncryptionAlgorithm.setValue(newValue.toString());
                        mEncryptionAlgorithm.setSummary(mEncryptionAlgorithm.getEntry());
                        mPreferences.setDefaultEncryptionAlgorithm(Integer.parseInt(newValue
                                .toString()));
                        return false;
                    }
                });
    }

    private static void initializeHashAlgorithm
            (final IntegerListPreference mHashAlgorithm, int[] valueIds, String[] entries, String[] values) {
        valueIds = new int[] { HashAlgorithmTags.MD5, HashAlgorithmTags.RIPEMD160,
                HashAlgorithmTags.SHA1, HashAlgorithmTags.SHA224, HashAlgorithmTags.SHA256,
                HashAlgorithmTags.SHA384, HashAlgorithmTags.SHA512, };
        entries = new String[] { "MD5", "RIPEMD-160", "SHA-1", "SHA-224", "SHA-256", "SHA-384",
                "SHA-512", };
        values = new String[valueIds.length];
        for (int i = 0; i < values.length; ++i) {
            values[i] = "" + valueIds[i];
        }
        mHashAlgorithm.setEntries(entries);
        mHashAlgorithm.setEntryValues(values);
        mHashAlgorithm.setValue("" + mPreferences.getDefaultHashAlgorithm());
        mHashAlgorithm.setSummary(mHashAlgorithm.getEntry());
        mHashAlgorithm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mHashAlgorithm.setValue(newValue.toString());
                mHashAlgorithm.setSummary(mHashAlgorithm.getEntry());
                mPreferences.setDefaultHashAlgorithm(Integer.parseInt(newValue.toString()));
                return false;
            }
        });
    }

    private static void initializeMessageCompression
            (final IntegerListPreference mMessageCompression, int[] valueIds, String[] entries, String[] values) {
        mMessageCompression.setEntries(entries);
        mMessageCompression.setEntryValues(values);
        mMessageCompression.setValue("" + mPreferences.getDefaultMessageCompression());
        mMessageCompression.setSummary(mMessageCompression.getEntry());
        mMessageCompression
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        mMessageCompression.setValue(newValue.toString());
                        mMessageCompression.setSummary(mMessageCompression.getEntry());
                        mPreferences.setDefaultMessageCompression(Integer.parseInt(newValue
                                .toString()));
                        return false;
                    }
                });
    }

    private static void initializeFileCompression
            (final IntegerListPreference mFileCompression, String[] entries, String[] values) {
        mFileCompression.setEntries(entries);
        mFileCompression.setEntryValues(values);
        mFileCompression.setValue("" + mPreferences.getDefaultFileCompression());
        mFileCompression.setSummary(mFileCompression.getEntry());
        mFileCompression.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mFileCompression.setValue(newValue.toString());
                mFileCompression.setSummary(mFileCompression.getEntry());
                mPreferences.setDefaultFileCompression(Integer.parseInt(newValue.toString()));
                return false;
            }
        });
    }

    private static void initializeAsciiArmor(final CheckBoxPreference mAsciiArmor) {
        mAsciiArmor.setChecked(mPreferences.getDefaultAsciiArmor());
        mAsciiArmor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mAsciiArmor.setChecked((Boolean) newValue);
                mPreferences.setDefaultAsciiArmor((Boolean) newValue);
                return false;
            }
        });
    }

    private static void initializeForceV3Signatures(final CheckBoxPreference mForceV3Signatures) {
        mForceV3Signatures.setChecked(mPreferences.getForceV3Signatures());
        mForceV3Signatures
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        mForceV3Signatures.setChecked((Boolean) newValue);
                        mPreferences.setForceV3Signatures((Boolean) newValue);
                        return false;
                    }
                });
    }
}