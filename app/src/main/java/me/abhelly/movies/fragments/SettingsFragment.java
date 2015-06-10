package me.abhelly.movies.fragments;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import me.abhelly.movies.R;

/**
 * Settings fragment.
 * Created by abhelly on 07.06.15.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
    }
}
