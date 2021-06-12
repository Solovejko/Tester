package com.company;

import java.net.*;
import java.io.*;


public class Main {
    public static void main(String[] args) throws IOException {
        URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=Moscow&appid=0b5edc7455a336d544760ce639198bc9&units=metric&lang=ru");
        URLConnection urlConnect = url.openConnection();

        if (urlConnect.getContentLength() > 0) {
            BufferedReader input = new BufferedReader(new InputStreamReader(urlConnect.getInputStream()));
            String urlString = "";
            String current;

            while((current = input.readLine()) != null) {
                urlString += current;
            }

            input.close();

            System.out.println(urlString);
        }
    }
}
