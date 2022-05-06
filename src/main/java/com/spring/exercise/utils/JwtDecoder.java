package com.spring.exercise.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.tomcat.util.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

public class JwtDecoder {

    public String sub;
    public String jti;

    public static JwtDecoder decodeToken(String token) throws UnsupportedEncodingException {
        String[] pieces = token.split("\\.");
        String payload = pieces[1];
        String jsonString = new String(Base64.decodeBase64(payload), "UTF-8");
        return new Gson().fromJson(jsonString, JwtDecoder.class);
    }

    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

}
