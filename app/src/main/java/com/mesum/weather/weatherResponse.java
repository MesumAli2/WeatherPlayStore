package com.mesum.weather;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class weatherResponse {
    public void getWeatherInfo(String cityname){
        String url = "www.apple.,com";

        JsonObjectRequest jsonObjectREquest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject hourArray = response.getJSONObject("forecast");
                    JSONObject forecasto = hourArray.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray harray  = forecasto.getJSONArray("hour");
                    for (int i =0; i <harray.length(); i++){
                        JSONObject hobject = harray.getJSONObject(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


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
