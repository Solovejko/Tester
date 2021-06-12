package com.company;

import java.net.*;
import java.io.*;
import java.time.*;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException{
        for (int i = 0; i < 200; i++){
            MyThread thread = new MyThread();
            thread.start();
        }

        try {
            spam(10);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            try {
                spam(10);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void spam(int n) throws IOException {
        for (int i = 0; i < n; i++)
            sendRequest(new URL("http://localhost:3000/weather/city?q=Moscow"));
    }

}
