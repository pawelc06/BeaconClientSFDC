/*
 * Copyright (c) 2012-present, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.neoelectronics.pawel.sfdc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Main activity
 */
public class MainActivity extends SalesforceActivity {
	private RestClient client;
    private ArrayAdapter<String> listAdapter;
	private JSONArray records;
	private int pos = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup view
		setContentView(R.layout.main);
	}

	@Override
	public void onResume() {
		// Hide everything until we are logged in
		findViewById(R.id.root).setVisibility(View.INVISIBLE);

		// Create list adapter
		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
		((ListView) findViewById(R.id.contacts_list)).setAdapter(listAdapter);
		ListView lv = ((ListView) findViewById(R.id.contacts_list));

		lv.setOnItemLongClickListener(
				new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
						pos = position;
						Toast.makeText(getApplicationContext(),
								"Long press received", Toast.LENGTH_SHORT).show();
						RestRequest restRequest = null;
						try {

							restRequest = RestRequest.getRequestForDelete(
									getString(R.string.api_version), "Contact",
									records.getJSONObject(position).getString("Id"));




							client.sendAsync(restRequest, new AsyncRequestCallback() {
								@Override
								public void onSuccess(RestRequest request, final RestResponse result) {
									result.consumeQuietly();
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											// Network component doesn’t report app layer status.
											// Use Mobile SDK RestResponse.isSuccess() method to check
											// whether the REST request itself succeeded.
											if (result.isSuccess()) {
												//listAdapter.remove(listAdapter.getItem(pos));
												AlertDialog.Builder b =
														new AlertDialog.Builder(findViewById
																(R.id.contacts_list).getContext());
												b.setMessage("Contact deleted!");
												b.setCancelable(true);
												AlertDialog a = b.create();
												a.show();
											} else {
												Toast.makeText(MainActivity.this,
														MainActivity.this.getString(
																SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(),
																result.toString()),
														Toast.LENGTH_LONG).show();
											}
										}
									});
								}
								@Override
								public void onError(final Exception exception) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											Toast.makeText(MainActivity.this,
													MainActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
													Toast.LENGTH_LONG).show();
										}
									});
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
						return true;
					}
				});

		lv.setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						pos = position;

						Toast.makeText(getApplicationContext(),
								"Click received", Toast.LENGTH_SHORT).show();

						RestRequest restRequest = null;
						try {

							Map fMap = new HashMap();

							fMap.put("ContactId__c", records.getJSONObject(position).getString("Id"));




							restRequest = RestRequest.getRequestForCreate("v44.0","ClientAppearance__e",fMap);
							client.sendAsync(restRequest, new AsyncRequestCallback() {
								@Override
								public void onSuccess(RestRequest request, final RestResponse result) {
									result.consumeQuietly();
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											// Network component doesn’t report app layer status.
											// Use Mobile SDK RestResponse.isSuccess() method to check
											// whether the REST request itself succeeded.
											if (result.isSuccess()) {
												//listAdapter.remove(listAdapter.getItem(pos));
												AlertDialog.Builder b =
														new AlertDialog.Builder(findViewById
																(R.id.contacts_list).getContext());
												b.setMessage("Event has been sent!");
												b.setCancelable(true);
												AlertDialog a = b.create();
												a.show();
											} else {
												Toast.makeText(MainActivity.this,
														MainActivity.this.getString(
																SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(),
																result.toString()),
														Toast.LENGTH_LONG).show();
											}
										}
									});
								}
								@Override
								public void onError(final Exception exception) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											Toast.makeText(MainActivity.this,
													MainActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
													Toast.LENGTH_LONG).show();
										}
									});
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
						return;
					}
				});

		super.onResume();
	}
	
	@Override
	public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client; 

		// Show everything
		findViewById(R.id.root).setVisibility(View.VISIBLE);
	}

	/**
	 * Called when "Logout" button is clicked. 
	 * 
	 * @param v
	 */
	public void onLogoutClick(View v) {
		SalesforceSDKManager.getInstance().logout(this);
	}
	
	/**
	 * Called when "Clear" button is clicked. 
	 * 
	 * @param v
	 */
	public void onClearClick(View v) {
		listAdapter.clear();
	}

	public void onMonitorClick(View v) {

		Context context = getApplicationContext();
		CharSequence text = "iBeacon monitor!";
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();

		Intent intent = new Intent(this, MonitoringActivity.class);
		startActivity(intent);
	}

	/**
	 * Called when "Fetch Contacts" button is clicked
	 * 
	 * @param v
	 * @throws UnsupportedEncodingException 
	 */
	public void onFetchContactsClick(View v) throws UnsupportedEncodingException {
        sendRequest("SELECT Name FROM Contact");
	}

	/**
	 * Called when "Fetch Accounts" button is clicked
	 * 
	 * @param v
	 * @throws UnsupportedEncodingException 
	 */
	public void onFetchAccountsClick(View v) throws UnsupportedEncodingException {
		sendRequest("SELECT Name FROM Account");
	}	
	
	private void sendRequest(String soql) throws UnsupportedEncodingException {
		RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);

		client.sendAsync(restRequest, new AsyncRequestCallback() {
			@Override
			public void onSuccess(RestRequest request, final RestResponse result) {
				result.consumeQuietly(); // consume before going back to main thread
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							listAdapter.clear();
							JSONArray records = result.asJSONObject().getJSONArray("records");
							for (int i = 0; i < records.length(); i++) {
								listAdapter.add(records.getJSONObject(i).getString("Name"));
							}
						} catch (Exception e) {
							onError(e);
						}
					}
				});
			}
			
			@Override
			public void onError(final Exception exception) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this,
								MainActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
								Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}
}
