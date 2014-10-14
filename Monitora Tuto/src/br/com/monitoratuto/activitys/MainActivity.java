package br.com.monitoratuto.activitys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import br.com.monitoratuto.util.CarregaParametros;

import com.example.notificationteste.R;


public class MainActivity extends ActionBarActivity{
	private CarregaParametros carregaParametros;
	//caminho para o arquivo de configurações
	private final File FILE = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/Parametros.properties");
	private Properties p;
	private boolean estado;
	private Button iniciar;
	private Button parar;
	private FileOutputStream outputStream;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		carregaParametros = new CarregaParametros();
		p = carregaParametros.getProperties(FILE);
		try {
			outputStream = new FileOutputStream(FILE);
			TelephonyManager mngr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
		    p.setProperty("imei",mngr.getDeviceId());
		    p.store(outputStream, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(R.drawable.spy_icon);
		
		
		
		
		estado = Boolean.valueOf(p.getProperty("enable"));
		
		iniciar = (Button) findViewById(R.id.button2);
		parar = (Button) findViewById(R.id.button1);
		iniciar.setEnabled(estado);
		if (iniciar.isEnabled()){
			parar.setEnabled(false);
		}
		
	}
	@Override
	protected void onResume() {
		super.onResume();
		carregaParametros = new CarregaParametros();
		p = carregaParametros.getProperties(FILE);
	}
	/**
	 * executa notificação do android
	 */
	public void notificar(){
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
		PendingIntent pi = PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class), 0);
		//texto rapido
		builder.setTicker("Monitorando");
		//titulo
		builder.setContentTitle("Monitoramento Ativado");
		//texto
		builder.setContentText("Enviando informações do seu GPS");
		//icone menor
		builder.setSmallIcon(R.drawable.spy_icon);
		//icone maior
		builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.spy_icon));
		builder.setContentIntent(pi);
		
		Notification n = builder.build();
		n.flags = Notification.FLAG_AUTO_CANCEL;
		n.vibrate = new long[]{150,300,150,600};
		nm.notify(R.drawable.spy_icon, n);
	}
	
	/**
	 * método usado no evento onClick do botão salvar.
	 * @param v
	 */
	public void iniciar(View v){
		String text = "";
		final boolean statusGps = verificaGPS();
	    final boolean statusRede = verificaConexao();
		if(!statusGps){
			text+="  * GPS";
		}
		
		if(!statusRede){
			text+="\n  * WIFI/GPRS";
		}
		if(!text.isEmpty()){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Para a aplicação funcionar é preciso que o(s) seguinte(s) recursos estejam ativos:\n"+text);
            builder.setPositiveButton("Ativar", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	   
                    	   if(!statusGps){
                    		   Intent iGps = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                   			   iGps.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   			   startActivity(iGps);
                    	   }
                    	   
               			   if(!statusRede){
               				Intent iWifi = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
             			    iWifi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             			    startActivity(iWifi);
               			   }
               			   
                       }
                   });
            builder.setNegativeButton("Cancelar", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					
				}
			});
		builder.show();
		}else{
			
        startService(new Intent("MONITORA"));
		notificar();
		iniciar.setEnabled(false);
		parar.setEnabled(true);
		finish();
		}
	}
	/**
	 * método usado no evento onClick do botão parar.
	 * @param v
	 */
	public void parar(View v){
		//para serviço
		stopService(new Intent("MONITORA"));
		iniciar.setEnabled(true);
		parar.setEnabled(false);
		Log.i("Services", "Parado");
	}
	
	public void Config(View v){
		Intent irConfig = new Intent(this, Config.class);
		startActivity(irConfig);
	}
	
	@Override
    
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
		case R.id.tutorial:
			Intent abrirTutorial = new Intent(Intent.ACTION_VIEW);
			Uri siteTutorial = Uri.parse("http://marcosilva.sytes.net/tutorial.html");
			abrirTutorial.setData(siteTutorial);
			startActivity(abrirTutorial);
			break;
		case R.id.notas:
			Intent abrirNotas = new Intent(Intent.ACTION_VIEW);
			Uri siteNotas = Uri.parse("http://marcosilva.sytes.net/notas.html");
			abrirNotas.setData(siteNotas);
			startActivity(abrirNotas);
			break;
		default:
			break;
		}

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Log.i("CONFIG","DESTROY");
		try {
			Log.i("CONFIG", p.getProperty("imei")+"\n"+p.getProperty("tempoAtu")+"\n"+p.getProperty("enable"));
			outputStream = new FileOutputStream(FILE);
			p.setProperty("enable", iniciar.isEnabled()+"");
	    	p.store(outputStream, null);
	    	outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private boolean verificaConexao(){
    	ConnectivityManager conexao = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    	if (conexao != null) 
        {
            NetworkInfo netInfo = conexao.getActiveNetworkInfo();
            // Se não existe nenhum tipo de conexão retorna false
            if (netInfo == null) {
              return false;
            }
        
            int netType = netInfo.getType();

            // Verifica se a conexão é do tipo WiFi ou Mobile e 
            // retorna true se estiver conectado ou false em
            // caso contrário
            if (netType == ConnectivityManager.TYPE_WIFI || 
                  netType == ConnectivityManager.TYPE_MOBILE) {
                return netInfo.isConnected();

            } else {
                return false;
            }
        }else{
          return false;
        }
    }

    private boolean verificaGPS(){	
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
    }
    
    
    
}
