package com.code4saitama.book;

import com.code4saitama.book.util.Config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class BookPreference extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference);
		
		setSummary(PreferenceManager.getDefaultSharedPreferences(this), getString(R.string.pref_key_user_id));
		setSummary(PreferenceManager.getDefaultSharedPreferences(this), getString(R.string.pref_key_user_name));

		// 変更があったときのイベントを登録する
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		setSummary(sharedPreferences, key);
	}
	
	private void setSummary(SharedPreferences sharedPreferences, String key) {
		if (isMatch(R.string.pref_key_user_id, key)) {
			findPreference(key).setSummary(sharedPreferences.getString(key, Config.DEFAULT_USER_ID));
		} else if (isMatch(R.string.pref_key_user_name, key)) {
			findPreference(key).setSummary(sharedPreferences.getString(key, Config.DEFAULT_USER_NAME));
		}
	}
	
	private boolean isMatch(int resourceId, String preferenceKey) {
		return (getString(resourceId).equals(preferenceKey));
	}

}
