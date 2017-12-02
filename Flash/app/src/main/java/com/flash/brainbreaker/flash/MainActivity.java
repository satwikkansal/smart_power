package com.flash.brainbreaker.flash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.heinrichreimersoftware.singleinputform.SingleInputFormActivity;
import com.heinrichreimersoftware.singleinputform.steps.Step;
import com.heinrichreimersoftware.singleinputform.steps.TextStep;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends SingleInputFormActivity {

    private static final String DATA_KEY_PHONE = "phone";
    private static final String DATA_KEY_PASSWORD = "password";
    private static final String DATA_KEY_METER = "meter";
    private static final String URL = "http://e6c614d0.ngrok.io/get_bill";
    SweetAlertDialog pDialog;
    @Override
    protected List<Step> onCreateSteps(){
        List<Step> steps = new ArrayList<>();

        setInputGravity(Gravity.CENTER);

        steps.add(new TextStep.Builder(this, DATA_KEY_PHONE)
                .titleResId(R.string.phone)
                .errorResId(R.string.phone_error)
                .detailsResId(R.string.phone_details)
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PHONETIC)
                .validator(new TextStep.Validator() {
                    @Override
                    public boolean validate(String input) {
                        return Patterns.PHONE.matcher(input).matches();
                    }
                })
                .build());

        steps.add(new TextStep.Builder(this, DATA_KEY_PASSWORD)
                .titleResId(R.string.password)
                .errorResId(R.string.password_error)
                .detailsResId(R.string.password_details)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .validator(new TextStep.Validator() {
                    @Override
                    public boolean validate(String input) {
                        return input.length() >= 5;
                    }
                })
                .build());

        steps.add(new TextStep.Builder(this, DATA_KEY_METER)
                .titleResId(R.string.meter)
                .errorResId(R.string.meter_error)
                .detailsResId(R.string.meter_details)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PHONETIC)
                .build());

        return steps;
    }

    @Override
    protected View onCreateFinishedView(LayoutInflater inflater, ViewGroup parent) {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Fetching Bill...");
        pDialog.setCancelable(false);
        pDialog.show();
        return inflater.inflate(R.layout.payment_done, parent, false);
    }

    @Override
    protected void onFormFinished(Bundle data) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmss");
        String timeFormat = sdf.format(new Date());

        sendJson(TextStep.text(data, DATA_KEY_METER), timeFormat);

//        //Wait 4 seconds and finish
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }, 2000);

    }

    protected void sendJson(final String meter_no, final String time) {
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
                    json.put("timestamp", time);
                    StringEntity se = new StringEntity(json.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    response = client.execute(post);

                    /*Checking response */
                    if(response!=null){
                        pDialog.dismiss();
                        final InputStream in = response.getEntity().getContent(); //Get the data in the entity
                        final String jsonResponse = convertStreamToString(in);
                        new SweetAlertDialog(MainActivity.this)
                                .setTitleText("Electricity Bill")
                                .setContentText(jsonResponse)
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                                        intent.putExtra("response", jsonResponse);
                                        MainActivity.this.startActivity(intent);
                                        finish();
                                    }
                                })
                                .show();
                    }


                } catch(Exception e) {
                    e.printStackTrace();
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Something went wrong!")
                            .show();
                }

                Looper.loop(); //Loop in the message queue
            }
        };

        t.start();
    }

    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
