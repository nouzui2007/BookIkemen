package com.code4saitama.book;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.code4saitama.book.http.HttpClientForCfs;
import com.code4saitama.book.http.OnErrorListener;
import com.code4saitama.book.util.BookPreferenceUtil;
import com.code4saitama.book.util.PinItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class MainActivity extends MapActivity implements LocationListener, OnErrorListener, HttpClientForCfs.OnFinishListener {

	private static final int DEFAULT_ZOOM_LEVEL = 19;

	private LocationManager mLocationManager;
	private MapView mapView;
	private MapController mapController;
	private MyLocationOverlay mOverlay = null;
	private TextView pointText;
	PinItemizedOverlay pinOverlay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			    
		setContentView(R.layout.activity_main);

		pointText = (TextView)findViewById(R.id.text_point);
		mapView = (MapView)findViewById(R.id.map);
		
		//MapControllerの取得
		mapController = mapView.getController();
		mapController.setZoom(DEFAULT_ZOOM_LEVEL);
		
		// LocationManagerを取得
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
        // Criteriaオブジェクトを生成
        Criteria criteria = new Criteria();
 
        // Accuracyを指定(低精度)
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
         
        // PowerRequirementを指定(低消費電力)
        criteria.setPowerRequirement(Criteria.POWER_LOW);
         
        // ロケーションプロバイダの取得
        String provider = mLocationManager.getBestProvider(criteria, true);
  
        // LocationListenerを登録
        mLocationManager.requestLocationUpdates(provider, 0, 0, this);
		
        mOverlay = new MyLocationOverlay(this, mapView);
        // 現在地取得を有効に
        if (!mOverlay.isMyLocationEnabled()) {
        	mOverlay.enableMyLocation();
        }
        mapView.getOverlays().add(mOverlay);
        // 最初に位置情報が確定したときに実行するコード
        mOverlay.runOnFirstFix(new Runnable() {
			@Override
			public void run() {
				// 現在地へ移動
				mapController.animateTo(mOverlay.getMyLocation());
			}
        });
        
        Drawable pin = getResources().getDrawable( R.drawable.pin);
        pinOverlay = new PinItemizedOverlay( pin);
        mapView.getOverlays().add( pinOverlay);

	}
	
	@Override
	protected void onResume() {
		putStores();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, BookPreference.class);
			startActivity(intent);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		if(location != null){
			//現在地情報取得成功
			//緯度の取得
			int latitude = (int) (location.getLatitude() * 1e6);
			//経度の取得
			int longitude = (int) (location.getLongitude() * 1e6);
			//GeoPointに緯度・経度を指定
			GeoPoint GP = new GeoPoint(latitude, longitude);
			//現在地までアニメーションで移動
			mapController.animateTo(GP);
			//現在地までパッと移動
			//MapCtrl.setCenter(GP);
			
			upCurrentLocation(location);
			
			Log.d("cfs", String.format("Lat : %d, Lon : %d", latitude, longitude));
		}else{
			//現在地情報取得失敗時の処理
			Toast.makeText(this, "現在地取得できませーん！", Toast.LENGTH_SHORT).show();
		}	
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

	public void upCurrentLocation(Location location)
	{
		HttpClientForCfs client = new HttpClientForCfs();
		client.setOnFinish(this);
		client.setOnError(this);
		
		String userId = BookPreferenceUtil.getUserId(this);
		
    	StringBuilder sb = new StringBuilder();
    	sb.append("http://book.code4saitama.org/criterium/users/add_point/")
    		.append(userId).append("/")
    		.append(location.getLatitude()).append("/")
    		.append(location.getLongitude());    		

		client.start(sb.toString(), HttpClientForCfs.METHOD_POST, null);
	}

	@Override
	public void raise(Exception e) {
		// TODO Auto-generated method stub
		
	}
	
	private void setNewPointValue(int point) {
		Log.d("cfs", "get new point : " + point);
		if (pointText != null) {
			pointText.setText(Integer.toString(point));
		}
	}

	@Override
	public void done(JSONObject obj) {
		try {
			int point = 0;
			JSONObject user = obj.getJSONObject("User");
			point = user.getInt("point");
			this.setNewPointValue(point);
		} catch (JSONException e) {
			e.printStackTrace();
			MainActivity.this.raise(e);
		}				
	}

	private void putStores() {
		//取得
		HttpClientForCfs client = new HttpClientForCfs();
		client.setOnFinish(new HttpClientForCfs.OnFinishListener() {
			@Override
			public void done(JSONObject obj) {
				//使わない
			}

			@Override
			public void done(JSONArray array) {
				//配置
				for (int i = 0; i < array.length(); i++) {
					try {
						JSONObject obj = array.getJSONObject(i);
						JSONObject store = obj.getJSONObject("Store");
						Log.d("cfs", store.getString("name"));
						
				        GeoPoint p = new GeoPoint(store.getInt("latitude"), store.getInt("longitude"));
				        pinOverlay.addPoint(p);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		client.setOnError(this);
		
    	StringBuilder sb = new StringBuilder();
    	sb.append("http://book.code4saitama.org/criterium/stores/findAll");
		client.start(sb.toString(), HttpClientForCfs.METHOD_POST, null);
	}

	@Override
	public void done(JSONArray array) {
		//使わない
	}
}
