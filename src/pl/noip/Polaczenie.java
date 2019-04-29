package pl.noip;

//IO
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;


public class Polaczenie{
  protected String url;
  protected String method;
  protected static String kodowanie;
  protected List<NameValuePair> params;
  protected HttpGet getRequest;
  protected HttpPost postRequest;
  protected HttpResponse response;
  protected String result;
  protected DefaultHttpClient httpClient;
  protected ArrayList<Header> naglowki;
 
  /**
   * constructor
   */
  public Polaczenie(DefaultHttpClient client){
	  this.httpClient = client;
	 
	  this.params = new ArrayList<NameValuePair>(2);
	  this.naglowki = new ArrayList<Header>();
	  kodowanie = HTTP.UTF_8;
  }
  
  public static DefaultHttpClient nowyKlient()
  {
	  HttpParams httpParameters = new BasicHttpParams();
	  
	  // Set the timeout in milliseconds until a connection is established.
	  int timeoutConnection = 20 * 1000;
	  HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	  // Set the default socket timeout (SO_TIMEOUT) 
	  // in milliseconds which is the timeout for waiting for data.
	  int timeoutSocket = 20 * 1000;
	  HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	  HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);
	  
	  HttpProtocolParams.setUseExpectContinue(httpParameters, false);
	  httpParameters.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
	  httpParameters.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
	  httpParameters.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
	  //httpParameters.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2965); 
	  HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);
	   
	  SchemeRegistry schemeRegistry = new SchemeRegistry ();
	  schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	  schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

	  ClientConnectionManager cm = new SingleClientConnManager(httpParameters, schemeRegistry);


	  return new DefaultHttpClient (cm, httpParameters);
  }
  
  public void setUrl(String url){
    this.url = url;
  }
  public String getUrl(){
    return this.url;
  }
  public void setMethod(String method){
    this.method = method;
  }
  public void ustawKodowanie(String k)
  {
	  kodowanie = k;
  }
  
  public String getMethod(){
    return this.method;
  }
  /**
   * Adding parameter
   * @param key
   * @param value
   */
  public void addParam(String key, String value){
    this.params.add(new BasicNameValuePair(key, value));
    
  }
  
  public void dodajNaglowek(String klucz, String wartosc)
  {
	  naglowki.add(new BasicHeader(klucz, wartosc));
  }

  public void czyscNaglowki()
  {
	  naglowki.clear();
  }
  
  public Header[] pobierzNaglowki()
  {
	  int ilosc = naglowki.size();
	  if(ilosc > 0)
	  {
		  Header[] temp = new Header[ilosc];
		  
		  for(int i = 0; i <ilosc; i++)
		  {
			  temp[i] = naglowki.get(i);
		  }
		  return temp;
	  }
	  return null;
		  
  }
  /**
   * getting response text
   * @return String response
   */
  public String getResult(){
	  if(result == null) return "";
	  else return this.result;
  }
  
  public String getResult(String encoder)
  {
	  try {
		return URLDecoder.decode(result, encoder);
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return result;
  }
  /**
   * executing request
   */
  public void execute(String dane)
  {
    try {
		this.getRequest = new HttpGet(this.getUrl());
    	// zastepuje naglowki zdefiniowanymi
    	if(naglowki.size() > 0) this.getRequest.setHeaders(this.pobierzNaglowki());
    	this.httpClient.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
    	this.getRequest.setHeader("User-Agent", "Mellenium Inc. - Router 25001 - 1.1");
    	//this.getRequest.setHeader("User-Agent", "No-IP Client AndroidOS/v1.0 jaskiewicz.sebastian@gmail.com");
    	
    	this.response = this.httpClient.execute(this.getRequest);

        HttpEntity entity = response.getEntity();

		if(entity != null)
		{
		  InputStream inputStream = entity.getContent();
		  // sprawdzenie czy zawartosc jest skompresowana
		  this.result = convertStreamToString(inputStream);
		}
		
    } catch (ClientProtocolException e) {
    	//Log.v("timeout",e.getMessage());
        this.result = "timeout";
        //Log.v("timeout",e.getMessage());
    } catch (IOException e) {
        this.result = "timeout";
        //Log.v("timeout",e.getMessage());
    } catch (IllegalStateException e){
    	
    } catch (IllegalArgumentException e)
    {
    	this.result = "dns";
    }
    
  }
  
  public void execute()
  {
	  execute(null);
  }
  
  /**
   * converting stream reader to string
   * @return String
   */
  private static String convertStreamToString(InputStream is) {
	 
    BufferedReader reader = null;
	try {
		reader = new BufferedReader(new InputStreamReader(is, kodowanie));
	} catch (UnsupportedEncodingException e1) {
		reader = new BufferedReader(new InputStreamReader(is));
	}
    StringBuilder stringBuilder = new StringBuilder();
 
    String line = null;
    try
    {
      while ((line = reader.readLine()) != null)
      {
    	  stringBuilder.append(line + "\n");
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        is.close();
        reader.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
	
    return stringBuilder.toString();
    
  }

}