package com.flash.brainbreaker.flash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.StringReader;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.flash.brainbreaker.flash.MainActivity.convertStreamToString;

public class PaymentActivity extends AppCompatActivity {
    public static final String URL = "http://e6c614d0.ngrok.io/pay_bill";
    SweetAlertDialog pDialog;
    Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        TextView units = (TextView) findViewById(R.id.units);
        TextView rate = (TextView) findViewById(R.id.rate);
        TextView amount = (TextView) findViewById(R.id.amount);
        TextView address = (TextView) findViewById(R.id.meter_Add);
        Button pay = (Button) findViewById(R.id.Pay);

        Bundle bundle = getIntent().getExtras();
        String message = bundle.getString("response");

        gson = new Gson();
        final APIResult apiResult = gson.fromJson(message, APIResult.class);

        if(apiResult != null) {
            address.setText("Meter Address - " + apiResult.meter_add);
            units.setText(apiResult.units + " Units");
            rate.setText(apiResult.rate + " Per Unit");
            amount.setText("Rs " + apiResult.amount);
            pay.setText("Pay Rs " + apiResult.amount + " via UPI");
        } else {
            Log.e("Error in Payment", "error");
        }

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pDialog = new SweetAlertDialog(PaymentActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                pDialog.setTitleText("Paying Bill...");
                pDialog.setCancelable(false);
                pDialog.show();
                sendJson(apiResult.meter_add, apiResult.amount);
            }
        });

    }

    protected void sendJson(final String meter_no, final String billAmount) {
        Thread t = new Thread() {

            public void run() {
                Looper.prepare(); //For Preparing Message Pool for the child Thread
                HttpClient client = new DefaultHttpClient();
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
                HttpResponse response;
                JSONObject json = new JSONObject();

                try {
                    HttpPost post = new HttpPost(URL);
                    json.put("meter_address", meter_no);
                    json.put("bill_timestamp", "random");
                    json.put("meter_bill_amount", billAmount);
                    StringEntity se = new StringEntity(json.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    response = client.execute(post);

                    /*Checking response */
                    if(response!=null){
                        pDialog.dismiss();
                        final InputStream in = response.getEntity().getContent(); //Get the data in the entity
                        final String jsonResponse = convertStreamToString(in);
                        new SweetAlertDialog(PaymentActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Your Unique txn hash - " + gson.fromJson(jsonResponse, PayResponse.class).hash)
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        Toast.makeText(PaymentActivity.this, "Hash copied to Clipboard", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                })
                                .show();
                    }


                } catch(Exception e) {
                    e.printStackTrace();
                    new SweetAlertDialog(PaymentActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Something went wrong!")
                            .show();
                }

                Looper.loop(); //Loop in the message queue
            }
        };

        t.start();
    }

}
