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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.cardemulation.OffHostApduService;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View.OnLayoutChangeListener;
import br.com.monitoratuto.connectWS.ConnectWebService;
import br.com.monitoratuto.entidade.DadosGPS;
import br.com.monitoratuto.util.CarregaParametros;

/**
 * 
 * @author MARCOANTONIO
 * 
 */
public class Servicos extends Service implements LocationListener {

	private LocationManager lm;
	private ArrayList<DadosGPS> coordenadas = new ArrayList<DadosGPS>();
	private ArrayList<DadosGPS> coordenadasOffLine = new ArrayList<DadosGPS>();
	private DadosGPS dadosGPSON;
	private DadosGPS dadosGPSOFF;
	private CarregaParametros carregaParametros;
	private final File FILE = new File(Environment
			.getExternalStorageDirectory().getAbsoluteFile()
			+ "/Parametros.properties");
	private Properties p;
	private int tempoAtu;
	private String imei;
	private static final String NOW = new Timestamp(System.currentTimeMillis()).toString();
	private static final String PANICO = "N";


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
		float vel_m_s = Float.parseFloat(local.getSpeed()+"");
		int vel = (int) Math.round(vel_m_s * 3.6);
		ConnectWebService connectWebService = new ConnectWebService();
		
		if(verificaConexao()){
			//Se existir coodernadas offline, envia para WS
			if(!coordenadasOffLine.isEmpty()){
				connectWebService(coordenadasOffLine);
			}
			coordenadas.add(local);
			Log.i("Local",
					coordenadas.size() + " - " + local.getLatitude() + "/"
							+ local.getLongitude() + "  "
							+ new Timestamp(System.currentTimeMillis()));
			if (coordenadas.size() == tempoAtu) {
				enviaWS(coordenadas,(coordenadas.size() - 1),ONLINE);
				
				Log.i("Local", "Enviou para WS");
				coordenadas.clear();
			}
		}else{
			dadosGPSOFF = new DadosGPS(imei,local.getLatitude()+"", local.getLongitude()+"","S","W",NOW,PANICO,vel);
			coordenadasOffLine.add(dadosGPSOFF);
		}
	}

	private boolean verificaConexao() {
		ConnectivityManager conexao = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conexao != null) {
			NetworkInfo netInfo = conexao.getActiveNetworkInfo();
			// Se não existe nenhum tipo de conexão retorna false
			if (netInfo == null) {
				return false;
			}

			int netType = netInfo.getType();

			// Verifica se a conexão é do tipo WiFi ou Mobile e
			// retorna true se estiver conectado ou false em
			// caso contrário
			if (netType == ConnectivityManager.TYPE_WIFI
					|| netType == ConnectivityManager.TYPE_MOBILE) {
				return netInfo.isConnected();

			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	/**
	 * 
	 * @param local
	 * @param tipo define se a carga é online ou offline
	 */
	private void enviaWS(ArrayList<Location> local,int index, int tipo){
		
		switch(tipo){
		case ONLINE:
			connectWebService.execute(local.get(index).getLatitude() + "",local.get(index).getLongitude() + "",
					local.get(index).getSpeed() + "",imei);
			break;
		case OFFLINE:
			connectWebService.execute(local);
			
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
