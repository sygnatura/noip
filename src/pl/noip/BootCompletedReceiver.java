package pl.noip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		Updater.getPrefs(context);
		if(Updater.pref_start)
		{
			boolean blad = false;
			if(Updater.pref_noip_host == null || Updater.pref_noip_login == null || Updater.pref_noip_haslo == null) blad = true;		
			if(blad == false) context.startService(new Intent(context, Updater.class));
		}
	}
}
