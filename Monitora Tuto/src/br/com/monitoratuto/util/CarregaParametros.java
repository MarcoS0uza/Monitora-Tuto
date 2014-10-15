package br.com.monitoratuto.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.util.Log;

public class CarregaParametros {

	private Properties properties;

	public CarregaParametros() {
		properties = new Properties();
	}

	public Properties getProperties(File FileName) {

		try {
			InputStream inputStream = new FileInputStream(FileName);
			properties.load(inputStream);
		} catch (IOException e) {
			Log.e("AssetsPropertyReader", e.toString());
		}
		return properties;
	}

}
