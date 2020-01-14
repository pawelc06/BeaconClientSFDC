package com.neoelectronics.pawel.sfdc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.neoelectronics.pawel.sfdc.R;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.altbeacon.beacon.AltBeacon;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

public class RangingActivity extends SalesforceActivity implements BeaconConsumer  {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    boolean isInRange = false;
    long lastSentEvent;
    private RestClient client;
    long millis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);

        beaconManager.bind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (Beacon beacon: beacons) {
                    if(beacon.getBluetoothName() != null && beacon.getBluetoothName().equals("iBeaconP")){
                        if (beacon.getDistance() < 2.0) {
                            isInRange = true;
                            Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  " + beacons.size());

                            logToDisplay("Beacon " + beacon.toString() + " is about " + beacon.getDistance() + " meters away.");
                            //logToDisplay("Type code: " + Integer.toHexString(firstBeacon.getBeaconTypeCode()));
                            //logToDisplay("RSSI: " + beacon.getRssi());
                            logToDisplay(beacon.getBluetoothName() + " is " + String.format("%.2f", beacon.getDistance())   + " meters away.");
                            //logToDisplay("Name: " + beacon.getBluetoothName());
                            //logToDisplay("Tx Power: " + beacon.getTxPower());

                            millis = System.currentTimeMillis();
                            if((millis - lastSentEvent)>60000) {

                                handleEnteringRange("0030Y000009a01yQAA");
                            }
                        } else {
                            logToDisplay("Customer has left the range:"+beacon.getBluetoothName() + " is " + String.format("%.2f", beacon.getDistance())   + " meters away.");
                        }
                    } else {
                        logToDisplay("Other :"+beacon.getBluetoothAddress() + " is " + String.format("%.2f", beacon.getDistance())   + " meters away.");
                    }
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    public void handleEnteringRange (String contactId) {
        Context context = getApplicationContext();
        CharSequence text = "Sending event to Salesforce...";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        RestRequest restRequest = null;
        try {

            Map fMap = new HashMap();

            fMap.put("ContactId__c", contactId);




            restRequest = RestRequest.getRequestForCreate("v44.0","ClientAppearance__e",fMap);
            client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
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
                                lastSentEvent = millis;
                                Toast toast = Toast.makeText(RangingActivity.this, "Event succesfully sent!", Toast.LENGTH_SHORT);
                                toast.show();

                            } else {
                                Context context = getApplicationContext();
                                Toast.makeText(context,
                                        RangingActivity.this.getString(
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
                            Toast.makeText(RangingActivity.this,
                                    RangingActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;

        // Show everything
        //findViewById(R.id.root).setVisibility(View.VISIBLE);
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
                //editText.append(line+"\n");
                editText.setText(line+"\n");;
            }
        });
    }
}

