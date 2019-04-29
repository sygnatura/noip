package pl.noip;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Ustawienia extends PreferenceActivity {

	 @Override
     protected void onCreate(Bundle savedInstanceState)
	 {
             super.onCreate(savedInstanceState);
             addPreferencesFromResource(R.xml.ustawienia);
            
     }
}
