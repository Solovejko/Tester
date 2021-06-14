package com.company;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.*;
import java.io.*;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Main {
    static int n = 50;
    static int thr = 0;
    static int time_thr = 0;
    static String url = "http://localhost:3000/favorites";
    static String address = "../Script.json";

    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < thr; i++){
            MyThread thread = new MyThread();
            thread.start();
            TimeUnit.SECONDS.sleep(time_thr);
        }

        Gson gson = new Gson();
        Script script = gson.fromJson(new FileReader(new File(address)), Script.class);

        System.out.println(script.method);
        System.out.println(script.input);
        System.out.println(script.timeOut);
        System.out.println(script.thread);
        System.out.println(script.intervalRequest);
        System.out.println(script.intervalOnThread);


    }

    public static void sendRequestPost(URL url) throws IOException {
        Map<String, String> myArgs = new HashMap<>();
        myArgs.put("User", "Bob");
        myArgs.put("Password", "123");

        Gson gson = new Gson();
        String json = gson.toJson(myArgs);
        byte[] out = json.getBytes();

        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        con.connect();

        OutputStream output = con.getOutputStream();
        output.write(out);
        output.close();

        StringBuilder urlString = new StringBuilder();
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK){
            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while((line = input.readLine()) != null) {
                urlString.append(line);
            }
            input.close();
        }

        System.out.println(urlString.toString());
        con.disconnect();
    }

    public static void sendRequestGet(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.connect();

        Instant firstPoint = Instant.now();
        int Code = con.getResponseCode();
        Instant secondPoint = Instant.now();

        long delay = Duration.between(firstPoint, secondPoint).toMillis();

        StringBuilder urlString = new StringBuilder();
        if (Code == HttpURLConnection.HTTP_OK){
            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while((line = input.readLine()) != null) {
                urlString.append(line);
            }
            input.close();
        }

        System.out.println(delay + " " + urlString.toString());
        con.disconnect();
    }

    static class MyThread extends Thread {
        @Override
        public void run(){
            spam();
        }
    }

    public static void spam() {
        try{
            for (int i = 0; i < n; i++)
                sendRequestGet(new URL("http://localhost:3000/weather/city?q=Moscow"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class Script {
        public String method;
    //    public Map<String, String> myArgs = new HashMap<>();
        public String input;
        public String timeOut;
        public String thread;
        public String intervalRequest;
        public String intervalOnThread;
    }

}
