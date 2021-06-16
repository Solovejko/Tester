import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.net.*;
import java.io.*;
import java.sql.DataTruncation;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

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

        public Setting(Setting set) {
            this.method = set.method;
            this.countRequest = set.countRequest;
            this.input = set.input;
            this.responseCodes = new ArrayList<>(set.responseCodes);
            this.checkOutBody = set.checkOutBody;
            this.output = set.output;
            this.timeOut = set.timeOut;
            this.thread = set.thread;
            this.intervalRequest = set.intervalRequest;
            this.intervalOnThread = set.intervalOnThread;
            this.url = set.url;
        }
    }
    static class Response{
        public Instant firstPoint;
        public Instant secondPoint;
        public long delay;
        public String status;
        public long sizeReq;
        public long sizeRes;

        public Response(){
            sizeReq = 0;
            sizeRes = 0;
        }
    }

    static Setting setting;
    static long sumDelay = 0;
    static long maxDelay = 0;
    static long countError = 0;
    static double kb = 0.0d;
    static long countReq = 0;
    static Logger logger = Logger.getLogger(Main.class);
    static String address = null;
    static String log;

    public static void main(String[] args){
        logger.info("Start");

        try {
            address = args[1];
            log = args[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error(e);
            logger.info("Finish");
            System.exit(1);
        }

        Gson gson = new Gson();
        try {
            setting = gson.fromJson(new FileReader(address), Setting.class);
            setting.url = new URL(args[0]);
        } catch (MalformedURLException | FileNotFoundException | ArrayIndexOutOfBoundsException e) {
            logger.error(e);
            logger.info("Finish");
            System.exit(1);
        }

        if (log.equals("INFO")){
            logger.info(address + " " + setting.url);
        }

        if (log.equals("DEBUG")){
            logger.info("\n      * " + address + " " + setting.url + '\n' +
                        "      * method: " + setting.method + '\n' +
                        "      * countRequest: " + setting.countRequest + '\n' +
                        "      * input: " + setting.input + '\n' +
                        "      * responseCodes: " + setting.responseCodes + '\n' +
                        "      * checkOutBody: " + setting.checkOutBody + '\n' +
                        "      * output: " + setting.output + '\n' +
                        "      * timeOut: " + setting.timeOut + '\n' +
                        "      * thread: " + setting.thread + '\n' +
                        "      * intervalRequest: " + setting.intervalRequest + '\n' +
                        "      * intervalOnThread: " + setting.intervalOnThread);
        }

        Instant startPoint = Instant.now(); // start time

        try {
            List<MyThread> list = new ArrayList<>();
            for (int i = 0; i < setting.thread; i++){ // start threads
                MyThread thread = new MyThread();
                thread.start();
                list.add(thread);
                TimeUnit.SECONDS.sleep(setting.intervalOnThread);
            }
            for (MyThread thread: list){   // wait all threads
                    thread.join();
            }
        } catch (InterruptedException e) {
            logger.error(e);
        }


        Instant finishPoint = Instant.now(); //finish time

        System.out.print(((double) countReq / (double) Duration.between(startPoint, finishPoint).toMillis() * 1000) + " ");
        System.out.print(((double) sumDelay / (double)countReq) + " ");
        System.out.print(((double) countError / (double) countReq) * 100 + "% ");
        System.out.print(setting.thread + " ");
        System.out.print(maxDelay + " ");
        System.out.println(kb / (double)countReq);

        logger.info("Finish");
    }

    public static Response sendRequest(Setting setting) throws IOException{
        if (log.equals("DEBUG")){
            logger.info("Thread: " + Thread.currentThread().getName() + " Starting function sendRequest()");
        }
        countReq = countReq + 1;
        Response response = new Response();
        HttpURLConnection con;
        con = (HttpURLConnection) setting.url.openConnection();
        con.setRequestMethod(setting.method);
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(setting.timeOut);
        con.setDoInput(true);
        con.setDoOutput(true);
        con.connect();

        if (!setting.method.equals("GET")) { // if we must send body
            byte[] out = setting.input.toString().getBytes();
            OutputStream output = con.getOutputStream();
            output.write(out);
            output.close();
            response.sizeReq = out.length;
        }

        response.firstPoint = Instant.now();
        int code = con.getResponseCode();
        response.secondPoint = Instant.now();
        response.delay = Duration.between(response.firstPoint, response.secondPoint).toMillis();

        response.status = "OK";
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
                    response.status = "ERROR";
                }
            }
            input.close();
        } else {
            response.status = "ERROR";
        }
        response.sizeRes = con.getContentLengthLong();
        if (response.sizeRes < 0){
            response.sizeRes = 0;
        }

        con.disconnect();

        if (log.equals("DEBUG")){
            logger.info("Thread: " + Thread.currentThread().getName() + " Closing function sendRequest()");
        }
        return response;
    }

    static class MyThread extends Thread {
        @Override
        public void run(){
            spam();
        }
    }

    public static void spam() {
        if (log.equals("DEBUG")){
            logger.info("Thread: " + Thread.currentThread().getName() + " Starting function spam()");
        }
        for (int i = 0; i < setting.countRequest; i++){
            Response response = null;
            try {
                response = sendRequest(setting);
            } catch (IOException e) {
                logger.error("Thread: " + Thread.currentThread().getName() + " " + e);
                countError = countError + 1;
                continue;
            }
            if (response.status.equals("ERROR")){
                countError = countError + 1;
            }
            sumDelay = sumDelay + response.delay;
            if (response.delay > maxDelay)
                maxDelay = response.delay;
            kb = kb + ((double)response.sizeReq + (double)response.sizeRes) / (double)response.delay * 1000.0d / 1024.0d;

            System.out.println(response.firstPoint + " " + response.secondPoint + " " + response.delay + " " +
                    response.status + " " + response.sizeReq + " " + response.sizeRes);

            try {
                TimeUnit.SECONDS.sleep(setting.intervalRequest);
            } catch (InterruptedException e) {
                logger.error("Thread: " + Thread.currentThread().getName() + " " + e);
            }
        }
        if (log.equals("DEBUG")){
            logger.info("Thread: " + Thread.currentThread().getName() + " Closing function spam()");
        }
    }
}
