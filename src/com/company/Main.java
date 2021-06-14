package com.company;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.net.*;
import java.io.*;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Main {
    static class Setting{
        public String method;
        public Integer countRequest;
        public JsonObject input;
        public List<Integer> responseCodes;
        public String checkOutBody;
        public JsonObject output;
        public Integer timeOut;
        public Integer thread;
        public Integer intervalRequest;
        public Integer intervalOnThread;
        public URL url;
    }
    static Setting setting;
    static long sumDelay = 0;
    static long maxDelay = 0;
    static long countError = 0;
    static double kb = 0.0d;

    public static void main(String[] args) throws IOException, InterruptedException {
        String address = "../Script.json";

        Gson gson = new Gson();
        setting = gson.fromJson(new FileReader(new File(address)), Setting.class);
        setting.countRequest = 10;
        setting.url = new URL("http://localhost:3000/favorites");

        List<MyThread> list = new ArrayList<>();
        Instant startPoint = Instant.now();
        for (int i = 0; i < setting.thread; i++){
            MyThread thread = new MyThread();
            thread.start();
            list.add(thread);
            TimeUnit.SECONDS.sleep(setting.intervalOnThread);
        }

        for (MyThread thread: list){
            thread.join();
        }

        Instant finishPoint = Instant.now();

        long countReq = (long) setting.countRequest * setting.thread;

        System.out.print(((double) countReq / (double) Duration.between(startPoint, finishPoint).toSeconds()) + " ");
        System.out.print(((double) sumDelay / (double)countReq) + " ");
        System.out.print(((double) countError / (double) countReq) * 100 + "% ");
        System.out.print(setting.thread + " ");
        System.out.print(maxDelay + " ");
        System.out.println(kb / (double)countReq);
    }

    public static void sendRequest() throws IOException, InterruptedException {
        HttpURLConnection con = (HttpURLConnection) setting.url.openConnection();

        con.setRequestMethod(setting.method);
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(setting.timeOut);
        con.setDoInput(true);
        con.setDoOutput(true);
        con.connect();

        long sizeReq = 0;
        if (!setting.method.equals("GET")) {
            byte[] out = setting.input.toString().getBytes();
            OutputStream output = con.getOutputStream();
            output.write(out);
            output.close();
            sizeReq = out.length;
        }

        Instant firstPoint = Instant.now();
        int code = con.getResponseCode();
        Instant secondPoint = Instant.now();
        long delay = Duration.between(firstPoint, secondPoint).toMillis();

        String status = "OK";
        StringBuilder urlString = new StringBuilder();
        boolean findCode = false;

        for (Integer integer: setting.responseCodes)
            if (integer == code) {
                findCode = true;
                break;
            }

        if (findCode){
            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while((line = input.readLine()) != null) {
                urlString.append(line);
            }
            if (setting.checkOutBody.equals("Yes")){
                if (!urlString.toString().equals(setting.output.toString())){
                    status = "ERROR";
                    countError = countError + 1;
                }
            }
            input.close();
        } else {
            status = "ERROR";
            countError = countError + 1;
        }
        long sizeRes = con.getContentLengthLong();
        if (sizeRes < 0){
            sizeRes = 0;
        }

        System.out.println(firstPoint + " " + secondPoint + " " + delay + " " + status + " " + sizeReq + " " + sizeRes);
        sumDelay = sumDelay + delay;
        if (delay > maxDelay)
            maxDelay = delay;
        kb = kb + ((double)sizeReq + (double)sizeRes) / (double)delay * 1000.0d / 1024.0d;
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
            for (int i = 0; i < setting.countRequest; i++){
                sendRequest();
                TimeUnit.SECONDS.sleep(setting.intervalRequest);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
