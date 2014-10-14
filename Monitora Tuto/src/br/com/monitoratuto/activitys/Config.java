package br.com.monitoratuto.activitys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.EditText;
import br.com.monitoratuto.util.CarregaParametros;

import com.example.notificationteste.R;

public class Config extends ActionBarActivity{
	private CarregaParametros carregaParametros;
    private Properties p;
	private EditText editImei;
	private EditText editTmpMin;
	private final File FILE = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/Parametros.properties");
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		Log.i("CONFIG", "onCreate");
		editImei = (EditText) findViewById(R.id.EditTextIMEI);
		editTmpMin = (EditText) findViewById(R.id.EditTextTmpMin);
		Log.i("CONFIG", "onCreate2");
		carregaParametros = new CarregaParametros();
		p = carregaParametros.getProperties(FILE);
		
		editImei.setText(p.getProperty("imei").toString());
		editTmpMin.setText(p.getProperty("tempoAtu").toString());
		Log.i("CONFIG", "onCreate3");
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.i("CONFIG", "onBackPressed");
		OutputStream outputStream;
		try {
			 outputStream = new FileOutputStream(FILE);
	         p.setProperty("imei", editImei.getText().toString());
	         p.setProperty("tempoAtu", editTmpMin.getText().toString());
	         p.store(outputStream, null);
	         outputStream.close();
		} catch (Exception e) {
			Log.e("AssetsPropertyReader",e.getMessage());
		}
        
		
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i("CONFIG", "onResume");
	}
	
	
}
