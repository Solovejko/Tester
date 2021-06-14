package com.company;

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

    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < thr; i++){
            MyThread thread = new MyThread();
            thread.start();
            TimeUnit.SECONDS.sleep(time_thr);
        }

        Map<String, String> myArgs = new HashMap<>();
        myArgs.put("User", "Bob");
        myArgs.put("Password", "123");

        System.out.println(myArgs.toString());

        byte[] out = myArgs.toString().getBytes();

        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("POST");
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

    public static void sendRequest(URL url) throws IOException {
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
                sendRequest(new URL("http://localhost:3000/weather/city?q=Moscow"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
