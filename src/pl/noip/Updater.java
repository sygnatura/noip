
package pl.noip;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.Toast;

public class Updater extends Service {

	/*public static boolean pref_dyndns;
	public static String pref_dyndns_host;
	public static String pref_dyndns_login;
	public static String pref_dyndns_haslo;
	private static boolean pref_dyndns_notify;
	public static boolean pref_noip;*/
	public static String pref_noip_host;
	public static String pref_noip_login;
	public static String pref_noip_haslo;
	public static boolean pref_noip_wifi;
	private static boolean pref_noip_notify;
	public static String pref_ip;
	public static String pref_ip_time;
	public static String pref_response;
	public static String pref_server;

	public static boolean pref_start;
	private static boolean pref_ok;
	
	private static Context context;
	public static String SERVICE_NAME = "pl.noip.Updater";
	private Pattern IP_REGEX = Pattern.compile("(\\d{2,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");
	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		context = getBaseContext();
		getPrefs(context);
		
	}

	@Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
 
    @Override
    public void onStart(Intent intent, int startId) {
    	if(MainActivity.isActivityVisible()) Toast.makeText(this, R.string.service_started, Toast.LENGTH_LONG).show();
        //new SprawdzIP().execute("http://www.smsoid.pl/myip.php");
        registerReceivers();
    }

    @Override
    public void onDestroy() {
    	if(MainActivity.isActivityVisible()) Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_LONG).show();
        unregisterReceivers();
    }
    
    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            /*String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
            */            
            // do application-specific task(s) based on the current network state, such
            // as enabling queuing of HTTP requests when currentNetworkInfo is connected etc.
            
            if(noConnectivity == false || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
            	if(pref_server.equals("1")) new SprawdzIP().execute("http://whatismyipaddress.com/");
            	else new SprawdzIP().execute("http://www.smsoid.pl/myip.php");
            }
        }
    };
    
	 /*
	* method to be invoked to register the receiver
	*/
	private void registerReceivers() {
		registerReceiver(mConnReceiver,	new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	private void unregisterReceivers() {
		this.unregisterReceiver(mConnReceiver);		
	}
    
    private class UpdateNOIP extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            try {
                Polaczenie polaczenie = new Polaczenie(Polaczenie.nowyKlient());
                polaczenie.setUrl(urls[0]);
    		    polaczenie.setMethod("GET");
    		    polaczenie.dodajNaglowek("Authorization", "Basic " + Base64.encodeToString((urls[1]+":"+urls[2]).getBytes(), Base64.NO_WRAP));
    		    polaczenie.execute();
    		    //Log.v("rezultat",polaczenie.getResult());
    		    return polaczenie.getResult();

            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(String rezultat) {
   			Calendar c = Calendar.getInstance(); 
   			String rok = String.valueOf(c.get(Calendar.YEAR));
   			String miesiac = String.valueOf(c.get(Calendar.MONTH)+1);
   			String dzien = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
   			String godzina = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
   			String minuta = String.valueOf(c.get(Calendar.MINUTE));
   			String sekunda = String.valueOf(c.get(Calendar.SECOND));
   			if(miesiac.length() == 1) miesiac = "0" + miesiac;
   			if(dzien.length() == 1) dzien = "0" + dzien;
   			if(godzina.length() == 1) godzina = "0" + godzina;
   			if(minuta.length() == 1) minuta = "0" + minuta;
   			if(sekunda.length() == 1) sekunda = "0" + sekunda;
   			
   			pref_ip_time = rok + "." + miesiac + "." + dzien + " " + godzina + ":" + minuta + "." + sekunda;
   			setPref("pref_ip_time", pref_ip_time, context);
   			if(MainActivity.latest_ip_time != null) MainActivity.latest_ip_time.setText(Updater.pref_ip_time);
        	pref_response = rezultat;
        	
        	//Log.v("rezultat", rezultat);
        	
        	if(pref_response != null)
        	{
        		setPref("pref_response", pref_response, context);
        		if(MainActivity.latest_response != null) MainActivity.latest_response.setText(pref_response);
        	}
        	else if(MainActivity.latest_response != null) MainActivity.latest_response.setText(R.string.brak_danych);
        	
        	if(rezultat.startsWith("good"))
        	{
        		if(pref_noip_notify) pokazDymek(R.string.good);
        		pref_ok = true;
        		setPref("pref_ok", pref_ok, context);
        	}
        	else if(rezultat.startsWith("nochg"))
        	{
        		//if(rezultat.matches("\\d+.\\d+.\\d+.\\d+"))
        		//{
            		if(pref_noip_notify) pokazDymek(R.string.nochg);
            		pref_ok = true;
            		setPref("pref_ok", pref_ok, context);
        		//}
        		/*else
        		{
            		pokazDymek(R.string.badauth);
            		pref_ok = false;
            		pref_noip_host = null;
            		setPref("pref_start", false, context);
            		setPref("pref_ok", pref_ok, context);
            		setPref("noip_host", pref_noip_host, context);
            		if(MainActivity.przycisk != null) MainActivity.przycisk.setImageResource(R.drawable.on);
            		Updater.this.stopSelf();
        		}*/
        	}
        	else if(rezultat.startsWith("nohost"))
        	{
        		pokazDymek(R.string.nohost);
        		setPref("pref_start", false, context);
        		pref_ok = false;
        		setPref("pref_ok", pref_ok, context);
        		if(MainActivity.przycisk != null) MainActivity.przycisk.setImageResource(R.drawable.on);
        		Updater.this.stopSelf();
        	}
        	else if(rezultat.startsWith("badauth"))
        	{
        		pokazDymek(R.string.badauth);
        		setPref("pref_start", false, context);
        		pref_ok = false;
        		setPref("pref_ok", pref_ok, context);
        		if(MainActivity.przycisk != null) MainActivity.przycisk.setImageResource(R.drawable.on);
        		Updater.this.stopSelf();
        	}
        	else if(rezultat.startsWith("badagent"))
        	{
        		pokazDymek(R.string.badagent);
        		setPref("pref_start", false, context);
        		pref_ok = false;
        		setPref("pref_ok", pref_ok, context);
        		if(MainActivity.przycisk != null) MainActivity.przycisk.setImageResource(R.drawable.on);
        		Updater.this.stopSelf();
        	}
        	else if(rezultat.startsWith("abuse"))
        	{
        		pokazDymek(R.string.abuse);
        		setPref("pref_start", false, context);
        		pref_ok = false;
        		setPref("pref_ok", pref_ok, context);
        		if(MainActivity.przycisk != null) MainActivity.przycisk.setImageResource(R.drawable.on);
        		Updater.this.stopSelf();
        	}
        	else if(rezultat.contains("timeout"))
        	{
        		if(pref_noip_notify) pokazDymek(R.string.timeout);
        		pref_ok = false;
        		setPref("pref_ok", pref_ok, context);
        	}
        	else if(rezultat.startsWith("911"))
        	{
        		if(pref_noip_notify) pokazDymek(R.string.err911);
        		pref_ok = false;
        		setPref("pref_ok", pref_ok, context);
        	}
        	// nieznany blad        	
        	else if(pref_noip_notify)
        	{
        		pokazDymek(rezultat);
        		pref_ok = false;
        		setPref("pref_ok", pref_ok, context);
        	}
        }

     }
    
 /*   private class UpdateDynDNS extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            try {
                Polaczenie polaczenie = new Polaczenie(Polaczenie.nowyKlient());
                polaczenie.setUrl(urls[0]);
    		    polaczenie.setMethod("GET");
    		    polaczenie.dodajNaglowek("Authorization", "Basic " + Base64.encodeToString((urls[1]+":"+urls[2]).getBytes(), Base64.NO_WRAP));
    		    polaczenie.execute();
    		    return polaczenie.getResult();

            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(String rezultat) {
        	if(rezultat.startsWith("good") && pref_dyndns_notify) pokazDymek(R.string.good);
        	else if(rezultat.startsWith("nochg") && pref_dyndns_notify) pokazDymek(R.string.nochg);
        	else if(rezultat.startsWith("nohost"))
        	{
        		pokazDymek(R.string.nohost);
        		setPref("pref_start", false, context);
        		if(MainActivity.przycisk != null) MainActivity.przycisk.setChecked(false);
        		Updater.this.stopSelf();
        	}
        	else if(rezultat.startsWith("badauth"))
        	{
        		pokazDymek(R.string.badauth);
        		setPref("pref_start", false, context);
        		if(MainActivity.przycisk != null) MainActivity.przycisk.setChecked(false);
        		Updater.this.stopSelf();
        	}
        	else if(rezultat.startsWith("badagent"))
        	{
        		pokazDymek(R.string.badagent);
        		setPref("pref_start", false, context);
        		if(MainActivity.przycisk != null) MainActivity.przycisk.setChecked(false);
        		Updater.this.stopSelf();
        	}
        	else if(rezultat.startsWith("abuse"))
        	{
        		pokazDymek(R.string.abuse);
        		setPref("pref_start", false, context);
        		if(MainActivity.przycisk != null) MainActivity.przycisk.setChecked(false);
        		Updater.this.stopSelf();
        	}
        	else if(rezultat.startsWith("notfqdn"))
        	{
        		pokazDymek(R.string.notfqdn);
        		setPref("pref_start", false, context);
        		if(MainActivity.przycisk != null) MainActivity.przycisk.setChecked(false);
        		Updater.this.stopSelf();
        	}
        	else if(rezultat.startsWith("911") && pref_dyndns_notify) pokazDymek(R.string.err911);
        	// nieznany blad        	
        	else if(pref_dyndns_notify) pokazDymek(rezultat);
        }

     }*/
    
    private class SprawdzIP extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
        	// sprawdzenie czy wifi jest wlaczone
        	ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        	NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        	
        	if(wifi.isConnected() == false || (wifi.isConnected() && pref_noip_wifi))
        	{
                try {
                    Polaczenie polaczenie = new Polaczenie(Polaczenie.nowyKlient());
                    polaczenie.setUrl(urls[0]);
        		    polaczenie.setMethod("GET");
        		    polaczenie.execute();
        		    return polaczenie.getResult().trim();

                } catch (Exception e) {
                    return null;
                }
        	}
        	else return null;

        }

        protected void onPostExecute(String rezultat) {
        	if(rezultat != null)
        	{
        		String IP = null;
        		getPrefs(context);
        		
/*        		if(pref_server.equals("1"))
        		{
        			// whastmyip
       				Matcher m = IP_REGEX.matcher(rezultat);
               		if(m.find())
               		{
               			IP = m.group(1);
                   		//Log.v("IP whastmyip", IP);
               		}
        		}
        		// smsoid.pl/myip.php
        		else
        		{
       				if(rezultat.matches("^\\d+.\\d+.\\d+.\\d+$")) IP = rezultat;
               		//Log.v("smsoid", IP);
        		}*/
        		
   				Matcher m = IP_REGEX.matcher(rezultat);
           		if(m.find())
           		{
           			IP = m.group(1);
               		//Log.v("IP", IP);
           		}

        		
           		// uaktualnij tylko jezeli adresy sie roznia lub poprzednia aktualizacja nie powiodla sie
           		if(IP != null && (pref_ip == null || !pref_ip.equals(IP) || pref_ok == false))
           		{
               		pref_ip = IP;
               		setPref("pref_ip", pref_ip, context);

                   	if(MainActivity.latest_ip != null) MainActivity.latest_ip.setText(Updater.pref_ip);		

                   	new UpdateNOIP().execute("https://dynupdate.no-ip.com/nic/update?hostname="+pref_noip_host+"&myip="+pref_ip, pref_noip_login, pref_noip_haslo);
                   	//if(pref_dyndns) new UpdateDynDNS().execute("https://members.dyndns.org/nic/update?hostname="+pref_noip_host+"&myip="+pref_ip, pref_noip_login, pref_noip_haslo);
           		}

        	}
        }
     }
    
    public static void getPrefs(Context c)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        pref_noip_host = prefs.getString("noip_host", null);
        pref_noip_login = prefs.getString("noip_login", null);
        pref_noip_haslo = prefs.getString("noip_haslo", null);
        pref_noip_notify = prefs.getBoolean("noip_notify", true);
        pref_noip_wifi = prefs.getBoolean("noip_wifi", true);
        pref_ip = prefs.getString("pref_ip", null);
        pref_ip_time = prefs.getString("pref_ip_time", null);
        pref_response = prefs.getString("pref_response", null);
        pref_server = prefs.getString("pref_server", "1");
        pref_start = prefs.getBoolean("pref_start", false);
        pref_ok = prefs.getBoolean("pref_ok", false);
    }
    
    public static void setPref(String key, String value, Context c)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor edytor = prefs.edit();
    	
        edytor.putString(key, value);
        edytor.commit();
    }

    public static void setPref(String key, boolean value, Context c)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor edytor = prefs.edit();
    	
        edytor.putBoolean(key, value);
        edytor.commit();
    }
    
    private void pokazDymek(int ResID)
    {
    	pokazDymek(this.getString(ResID));
    }

	private void pokazDymek(String tresc)
	{
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.ic_launcher;

		CharSequence tickerText = getString(R.string.app_name);
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		//Intent notificationIntent = new Intent(this, SMSoid.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(Intent.ACTION_MAIN, null, context, Updater.class)
		.addCategory(Intent.CATEGORY_LAUNCHER).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),0);

		notification.setLatestEventInfo(this, tickerText, tresc, contentIntent);
		//notification.defaults = Notification.FLAG_AUTO_CANCEL + Notification.FLAG_ONLY_ALERT_ONCE;
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		mNotificationManager.notify(0, notification);
	}
}
