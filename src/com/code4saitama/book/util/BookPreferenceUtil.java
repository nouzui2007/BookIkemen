package com.code4saitama.book.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.code4saitama.book.R;

/**
 * ユーティリティ
 * @author makiuchi
 *
 */
public class BookPreferenceUtil {

	public static String getUserId(Context context) {
		String	key	= context.getString(R.string.pref_key_user_id);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(key, Config.DEFAULT_USER_ID);
	}
	
	public static void setUserId(Context context, String userId) {
		String	key	= context.getString(R.string.pref_key_user_id);
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(key, userId);
		editor.commit();
	}

	public static String getUserName(Context context) {
		String	key	= context.getString(R.string.pref_key_user_name);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(key, Config.DEFAULT_USER_NAME);
	}
	
	public static void setUserName(Context context, String userId) {
		String	key	= context.getString(R.string.pref_key_user_name);
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(key, userId);
		editor.commit();
	}

	/**
	 * キーの値がなければデフォルト値を設定する
	 * @param context
	 * @param preferenceKey
	 * @param defaultValue
	 */
	public static void setDefaultValue(Context context, int preferenceKey, String defaultValue) {
		String key = context.getString(preferenceKey);
		setDefaultValue(context, key, defaultValue);
	}

	public static void setDefaultValue(Context context, int preferenceKey, float defaultValue) {
		String key = context.getString(preferenceKey);
		setDefaultValue(context, key, defaultValue);
	}

	public static void setDefaultValue(Context context, String key, String defaultValue) {
		if (!PreferenceManager.getDefaultSharedPreferences(context).contains(key)) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putString(key, defaultValue);
			editor.commit();
		}
	}

	public static void setDefaultValue(Context context, String key, float defaultValue) {
		if (!PreferenceManager.getDefaultSharedPreferences(context).contains(key)) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putFloat(key, defaultValue);
			editor.commit();
		}
	}

}
