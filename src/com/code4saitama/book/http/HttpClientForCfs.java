package com.code4saitama.book.http;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

/**
 * @author makiuchi
 * 
 */
@SuppressLint("NewApi")
public class HttpClientForCfs extends AsyncTask<String, Void, HttpResponse> {
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String PARAM_SEPERATOR = "&";
    public static final String PARAM_KEY_VALUE_SEPERATOR= "=";
    
    private OnFinishListener callback;
    private OnErrorListener errorCallback;
    
    public HttpClientForCfs() {
        /* API Level 11 以上で NetworkOnMainThreadException が発生するための処置 */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    /**
     * 完了時のコールバック設定
     * 
     * @param listener
     */
    public void setOnFinish(final OnFinishListener listener) {
        this.callback = listener;
    }
    
    public void setOnError(final OnErrorListener listener) {
    	this.errorCallback = listener;
    }
    
    private void doCallback(JSONObject json) {
		//コールバックへ
		if (this.callback != null){
			this.callback.done(json);
		}
    }

    private void doCallback(JSONArray json) {
		//コールバックへ
		if (this.callback != null){
			this.callback.done(json);
		}
    }
    
    private void doErrorCallback(Exception exception) {
    	if (this.errorCallback != null) {
    		this.errorCallback.raise(exception);
    	}
    }

    @SuppressLint("NewApi")
    public void start(final String url, final String method) {
      // API Level 10 以下
      Log.d("DEV", "Level 10 under");
      this.execute(url, method);
    }

    @SuppressLint("NewApi")
    public void start(final String url, final String method, final String params) {
            // API Level 10 以下
            Log.d("DEV", "Level 10 under");
            this.execute(url, method, params);
    }

    @Override
    public HttpResponse doInBackground(String... params) {
        String url = params[0];
        String method = params[1];
        String urlParams = "";
        
        if ((params.length == 3) && (params[2] != null)) {
        	urlParams = params[2];
        }
        HttpClient httpClient = this.getNewHttpClient();

        HttpResponse response = null;
        try {
            if (METHOD_GET.equals(method)) {
            	StringBuilder sb = new StringBuilder(url);
            	if (urlParams.length() == 0) {
            		sb.append("?").append(urlParams);
            	}
                response = httpClient.execute(new HttpGet(sb.toString()));
            } else {
                HttpPost httpPost = new HttpPost(url);
                if (urlParams.length() > 0) {
                    List<NameValuePair> paramList = this.getParamList(urlParams);
                    httpPost.setEntity(new UrlEncodedFormEntity(paramList));
                }
                response = httpClient.execute(httpPost);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            doErrorCallback(ex);
        }
        
        return response;
    }

    private List<NameValuePair> getParamList(String paramString) {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
    	
        // TODO キーペアの分解
        // key=value&key=value&... という文字列になっている事
    	String[] paramArray = paramString.split(PARAM_SEPERATOR, 0);
    	for (String param : paramArray) {
    		Log.d(this.getClass().getName(), "Pamameter : " + param);
    		String[] keyValue = param.split(PARAM_KEY_VALUE_SEPERATOR, 0);
    		if (keyValue.length == 1) {
    	        paramList.add(new BasicNameValuePair(keyValue[0], ""));
    		} else if (keyValue.length == 2) {
    	        paramList.add(new BasicNameValuePair(keyValue[0], keyValue[1]));
    		} else {
    			//Exception
    			throw new InvalidParameterException();
    		}
    	}

        return paramList;
    }

    private HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    @Override
    protected void onPostExecute(HttpResponse result) {
        super.onPostExecute(result);
        
        if(result != null) {
    		HttpEntity entity = result.getEntity();
    		String returnValue;
    		try {
    			returnValue = EntityUtils.toString(entity);
    			Log.d(this.getClass().getName(), "Result String : " + returnValue);
    			// 開放
    			entity.consumeContent();
    			if ("[".equals(returnValue.substring(0, 1))) {
    				JSONArray json = new JSONArray(returnValue);
        			doCallback(json);
    			} else {
    				JSONObject json = new JSONObject(returnValue);
        			doCallback(json);
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
                doErrorCallback(e);
    		}
        }
    }

    public class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws java.security.cert.CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws java.security.cert.CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
                UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
       
    /**
     * コールバック用インターフェース
     * @author makiuchi
     *
     */
    public interface OnFinishListener {
        public void done(JSONObject obj);
        public void done(JSONArray array);
    }
}
