package com.vagoscorp.vccompost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * The Class Set_Server.
 */
public class Set_Server extends Activity {

	/** The Server_ ip. */
	EditText Server_IP;
	
	/** The Server_ port. */
	EditText Server_Port;
	
	/** The ip. */
	String IP;
	
	/** The Port. */
	int Port;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent Data = getIntent();
		IP = Data.getStringExtra(MainService.SI);
		Port = Data.getIntExtra(MainService.SP, MainService.defPort);
		setContentView(R.layout.set_server);
		Server_IP = (EditText) findViewById(R.id.Server_IP);
		Server_Port = (EditText) findViewById(R.id.Server_Port);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Server_IP.setText(IP);
		Server_Port.setText(Port + "");
	}

	/**
	 * Cambiar.
	 *
	 * @param view the view
	 */
	public void Cambiar(View view) {
		final String SIP = Server_IP.getText().toString();
		final int SPort = Integer.parseInt(Server_Port.getText().toString());
		Intent result = new Intent("RESULT_ACTION");
		result.putExtra(MainService.SI, SIP);
		result.putExtra(MainService.SP, SPort);
		setResult(Activity.RESULT_OK, result);
		finish();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
