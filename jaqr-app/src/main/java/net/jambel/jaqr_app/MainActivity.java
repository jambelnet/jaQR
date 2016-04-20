package net.jambel.jaqr_app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.jambel.jaqr.SSLConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpsTransportSE;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.btnStartScanner);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanner();
            }
        });
    }

    public void startScanner() {
        Intent intent = new Intent("net.jambel.jaqr.libjaqr.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                // Handle successful scan and send scan result contents to web service
                AsyncCallWS task = new AsyncCallWS();
                task.execute(contents);
                TextView result = (TextView) findViewById(R.id.textView);
                result.setText(contents);
                // Toast.makeText(this, contents, Toast.LENGTH_LONG).show();
            }
            else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                Toast toast = Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();
            }
        }
    }

    private class AsyncCallWS extends AsyncTask<String, Void, String> {

        //@Override
        protected String doInBackground(String... params) {
            //
            return getWsResult(params[0]);
        }

        private String getWsResult(String _Value)
        {
            final String WSDL_TARGET_NAMESPACE = "http://tempuri.com/";
            // for non SSL
            // final String SOAP_ADDRESS = "http://tempuri.com/ws/wsMywebService.asmx";
            // for SSL
            final String SOAP_ADDRESS = "www.tempuri.com";
            final String SOAP_FILE = "/ws/wsMywebService.asmx";
            final String SOAP_ACTION = "http://tempuri.com/myWebMethod";
            final String METHOD_NAME = "myWebMethod";

            SSLConnection.allowAllSSL();

            SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, METHOD_NAME);

            // request.addProperty(_Name, _Value);
            // Web Method parameters
            request.addProperty("myMethod", "Some Value");
            // Add more methods, same as above

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            // Choose Method
            // Clear text
            // HttpTransportSE transport = new HttpTransportSE(SOAP_ADDRESS);
            // SSL
            HttpsTransportSE transport = new HttpsTransportSE(SOAP_ADDRESS, 443, SOAP_FILE, 5000);

            Object response;

            try {
                transport.call(SOAP_ACTION, envelope);
                response = envelope.getResponse();
            }
            catch (Exception exception) {
                response = exception;
            }

            return response.toString();
        }

        //@Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }
}
