package com.mesum.weather;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class weatherResponse {
    public void getWeatherInfo(String cityname){
        String url = "www.apple.,com";

        JsonObjectRequest jsonObjectREquest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        } ,new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }
        )
                ;
    }
}
