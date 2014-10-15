package br.com.monitoratuto.service;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import br.com.monitoratuto.connectWS.ConnectWebService;
import br.com.monitoratuto.util.CarregaParametros;

/**
 * 
 * @author MARCOANTONIO
 * 
 */
public class Servicos extends Service implements LocationListener {

	private LocationManager lm;
	private ArrayList<Location> coordenadas = new ArrayList<Location>();
	private CarregaParametros carregaParametros;
	private final File FILE = new File(Environment
			.getExternalStorageDirectory().getAbsoluteFile()
			+ "/Parametros.properties");
	private Properties p;
	private int tempoAtu;
	private String imei;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * executa quando o servido é criado
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		// obtém serviço de localização.
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// inicia monitoramento
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		Log.i("Local", "Iniciado");
	}

	/**
	 * executa quando inicia o serviço
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("Services", "Ativo");
		carregaParametros = new CarregaParametros();
		p = carregaParametros.getProperties(FILE);
		tempoAtu = Integer.valueOf(p.getProperty("tempoAtu"));
		imei = p.getProperty("imei").toString();
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * executa quando o servido é finalizado;
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		// encerra o serviço
		stopSelf();
		// encerra o monitoramento GPS
		lm.removeUpdates(this);
	}

	/**
	 * envia localizaçao no tempo/metros determinado
	 */
	@Override
	public void onLocationChanged(Location local) {
		coordenadas.add(local);
		Log.i("Local",
				coordenadas.size() + " - " + local.getLatitude() + "/"
						+ local.getLongitude() + "  "
						+ new Timestamp(System.currentTimeMillis()));
		if (coordenadas.size() == tempoAtu) {
			ConnectWebService connectWebService = new ConnectWebService();
			connectWebService.execute(coordenadas.get(coordenadas.size() - 1)
					.getLatitude() + "", coordenadas
					.get(coordenadas.size() - 1).getLongitude() + "",
					coordenadas.get(coordenadas.size() - 1).getSpeed() + "",
					imei);
			Log.i("Local", "Enviou para WS");
			coordenadas.clear();
		}

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

}
