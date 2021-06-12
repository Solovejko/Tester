package com.company;

import java.net.*;
import java.io.*;
import java.time.*;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // https://api.openweathermap.org/data/2.5/weather?q=Moscow&appid=0b5edc7455a336d544760ce639198bc9&units=metric&lang=ru

        URL url = new URL("http://localhost:3000/weather/city?q=Moscow");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");

        con.connect();

        Instant firstPoint = Instant.now();
        int Code = con.getResponseCode();
        Instant secondPoint = Instant.now();

        long delay = Duration.between(firstPoint, secondPoint).toMillis();
        System.out.println(delay);

        if (Code == HttpURLConnection.HTTP_OK){
            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder urlString = new StringBuilder();
            String line;

            while((line = input.readLine()) != null) {
                urlString.append(line);
                urlString.append("\n");
            }

            input.close();

            System.out.println(urlString.toString());
        }

        con.disconnect();
    }
}
