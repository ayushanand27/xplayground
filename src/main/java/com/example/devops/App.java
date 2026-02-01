package com.example.devops;

import static spark.Spark.get;
import static spark.Spark.port;

public class App {
    public static final String MESSAGE = "DevOps Pipeline Working";

    public static void main(String[] args) {
        port(8800);

        get("/health", (req, res) -> "OK");

        get("/", (req, res) -> {
            res.type("text/html");
            return "<html>" +
                   "  <head><title>DevOps Playground</title></head>" +
                   "  <body>" +
                   "    <h1>" + MESSAGE + "</h1>" +
                   "    <p>GitHub -> Jenkins -> Maven -> Selenium -> Docker</p>" +
                   "  </body>" +
                   "</html>";
        });

        System.out.println("Server started: http://localhost:8800");
        System.out.println(MESSAGE);
    }
}

