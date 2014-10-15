package br.com.monitoratuto.connectWS;

import java.io.IOException;
import java.sql.Timestamp;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.util.Log;

public class ConnectWebService extends AsyncTask<String, Void, String> {
	private static final String SOAP_ACTION = "http://achafacilrastreamento.com.br/gravaDadosGps";
	private static final String METODO = "gravaDadosGps";
	private static final String NAMESPACE = "http://achafacilrastreamento.com.br";
	private static final String URL = "http://achafacilrastreamento.com.br/WS/Services.asmx";
	private static final String NOW = new Timestamp(System.currentTimeMillis())
			.toString();
	private static final String PANICO = "N";

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(String... s) {
		SoapObject soap = new SoapObject(NAMESPACE, METODO);

		float vel_m_s = Float.parseFloat(s[2].toString());
		int vel = (int) Math.round(vel_m_s * 3.6);

		soap.addProperty("imei", s[3].toString());
		soap.addProperty("latitude", s[0].toString());
		soap.addProperty("longitude", s[1].toString());
		soap.addProperty("hemisferio_ns", "S");
		soap.addProperty("hemisferio_lo", "W");
		soap.addProperty("velocidade", vel);
		soap.addProperty("datahora_gps", NOW);
		soap.addProperty("panico", PANICO);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(soap);
		HttpTransportSE httpTransport = new HttpTransportSE(URL);

		try {
			Log.e("ERRO", "Enviou");
			httpTransport.call(SOAP_ACTION, envelope);

			// Log.i("ResultWS",envelope.getResponse()+"");
		} catch (XmlPullParserException | IOException ex) {
			Log.e("ERRO", ex.getMessage());
		}
		return "";
	}

}
