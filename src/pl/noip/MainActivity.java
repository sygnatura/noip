package pl.noip;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.BadTokenException;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;


public class MainActivity extends Activity {
	
	public static String DEV_ID = "104683993";
	public static String APP_ID = "204467219";

	public static boolean isRunning;
	private static boolean activityVisible;
	public static TextView latest_ip;
	public static TextView latest_ip_time;
	public static TextView latest_response;
	public static ImageButton przycisk;
	private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
	//private Pattern DOMAIN_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	StartAppSDK.init(this, DEV_ID, APP_ID, true);
        new StartAppAd(this);
		StartAppAd.showSplash(this, savedInstanceState);
		setContentView(R.layout.activity_main);
		
		 
		//listener on changed sort order preference:
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		 
		prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		 
		    	// jezeli uzytkownik zmienil ktores z ustawien to zresetuj ustawienia
		    	if(key.equals("noip_host") || key.equals("noip_login") || key.equals("noip_haslo"))
		    	{
	        		Updater.setPref("pref_ok", false, MainActivity.this);
	        		Updater.setPref("pref_ip", null, MainActivity.this);
	        		Updater.setPref("pref_ip_time", null, MainActivity.this);
	        		Updater.setPref("pref_response", null, MainActivity.this);
	        		Updater.setPref("pref_start", false, MainActivity.this);
	        		
	        		latest_ip.setText(R.string.brak_danych);
	        		latest_ip_time.setText(R.string.brak_danych);
	        		latest_response.setText(R.string.brak_danych);

	        		isRunning = MainActivity.isServiceRunning(MainActivity.this, Updater.SERVICE_NAME);
		        	if(isRunning)
		        	{
		        		MainActivity.this.stopService(new Intent(MainActivity.this, Updater.class));
		        		przycisk.setImageResource(R.drawable.on);
		        	}
		    	}
		 
		    }
		};
		prefs.registerOnSharedPreferenceChangeListener(prefListener);
		
		latest_ip = (TextView) this.findViewById(R.id.latest_ip);
		latest_ip_time = (TextView) this.findViewById(R.id.latest_ip_time);
		latest_response = (TextView) this.findViewById(R.id.latest_response);
		przycisk = ((ImageButton) this.findViewById(R.id.toggleButton));
		przycisk.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		
		
		checkinternet();
		

	}
	
    public void procesSwitch(View V)
    {
    	isRunning = isServiceRunning(this, Updater.SERVICE_NAME);
    	if(isRunning)
    	{
    		stopService(new Intent(this, Updater.class));
    		Updater.setPref("pref_start", false, this);
    		przycisk.setImageResource(R.drawable.on);
    	}
    	else
    	{
    		boolean blad = false;
    		if(checkinternet() == false)
    		{
    			blad = true;
    		}
    		else if(Updater.pref_noip_host == null || Updater.pref_noip_host.length() == 0)
    		{
    			Toast.makeText(this, R.string.toast_host, Toast.LENGTH_LONG).show();
    			blad = true;
    		}
    		// jezeli podna nazwa domeny jest nieprawidlowa
    		/*else if(DOMAIN_NAME_PATTERN.matcher(Updater.pref_noip_host).find() == false)
    		{
    			Toast.makeText(this, "\"" + Updater.pref_noip_host + "\" " + this.getString(R.string.toast_host_wrong), Toast.LENGTH_LONG).show();
    			blad = true;
    		}*/
    		else if(Updater.pref_noip_login == null || Updater.pref_noip_login.length() == 0)
    		{
    			Toast.makeText(this, R.string.toast_login, Toast.LENGTH_LONG).show();
    			blad = true;
    		}
    		else if(Updater.pref_noip_haslo == null  || Updater.pref_noip_haslo.length() == 0)
    		{
    			Toast.makeText(this, R.string.toast_haslo, Toast.LENGTH_LONG).show();
    			blad = true;
    		}
    		// jezeli blad to wylacz przycisk
    		if(blad) przycisk.setImageResource(R.drawable.on);
    		else
    		{
    			startService(new Intent(this, Updater.class));
    			Updater.setPref("pref_start", true, this);
    			przycisk.setImageResource(R.drawable.off);
    			
    			if(Updater.pref_ip != null) latest_ip.setText(Updater.pref_ip);
    			if(Updater.pref_ip_time != null) latest_ip_time.setText(Updater.pref_ip_time);
    			if(Updater.pref_response != null) latest_response.setText(Updater.pref_response);
    		}
    	}
    }
    
    public static boolean isActivityVisible() {
        return activityVisible;
      }  

      public static void activityResumed() {
        activityVisible = true;
      }

      public static void activityPaused() {
        activityVisible = false;
      }
	
    public boolean checkinternet()
    {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.isConnected() == false)
        {
            try
            {
        		Builder builder = new Builder(this);
        		builder.setMessage(R.string.no_internet);
        		builder.setCancelable(true);  
        		builder.setIcon(android.R.drawable.ic_dialog_info);
        		
        		//builder.setInverseBackgroundForced(true);  
        		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {  
        			@Override 
        			public void onClick(DialogInterface dialogBox, int which)
        			{  
        				dialogBox.dismiss();  
        			}
        		}).create().show();
            }catch(BadTokenException e) {}
            
            return false;
        }else return true;
    }
    
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
	      super.onPause();
	      activityPaused();
	}
	
	

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		activityResumed();
		
	    Updater.getPrefs(this.getApplicationContext());
		
		isRunning = isServiceRunning(this, Updater.SERVICE_NAME);
		
		if(isRunning)
		{
			przycisk.setImageResource(R.drawable.off);
			
			if(Updater.pref_ip != null) latest_ip.setText(Updater.pref_ip);
			if(Updater.pref_ip_time != null) latest_ip_time.setText(Updater.pref_ip_time);
			if(Updater.pref_response != null) latest_response.setText(Updater.pref_response);
		}
		else przycisk.setImageResource(R.drawable.on);
		
	}
	
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Updater.getPrefs(this.getApplicationContext());
	}

	public static boolean isServiceRunning(Context ctx, String name)
    {
	    ActivityManager manager = ( ActivityManager )ctx.getSystemService( Context.ACTIVITY_SERVICE );
	     
	    for( RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE) )
	    {
	    	if( name.equals( service.service.getClassName() ) ) return true;	
	    }
	    return false;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
       /* case R.id.action_refresh:
        	isRunning = isServiceRunning(this, Updater.SERVICE_NAME);
        	if(isRunning)
        	{
        		stopService(new Intent(this, Updater.class));
        		Updater.setPref("pref_start", false, this);
        		przycisk.setChecked(false);
        	}
        	
        	if(isRunning)
        	{
        		stopService(new Intent(this, Updater.class));
        		Updater.setPref("pref_start", false, this);
        		przycisk.setChecked(false);
        	}	
        	procesSwitch(null);

            return true;*/

        case R.id.action_settings:
        	// preferencje        	
        	Intent ustawienia = new Intent(this, Ustawienia.class);
        	startActivity(ustawienia);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}