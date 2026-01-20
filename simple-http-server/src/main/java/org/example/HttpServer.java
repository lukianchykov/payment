package org.example;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class HttpServer {

    private static final Path BASE_DIR =
            Paths.get("simple-http-server/static").toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Server started at http://localhost:8080");
        System.out.println("Serving files from: " + BASE_DIR);

        while (true) {
            Socket clientSocket = serverSocket.accept();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            BufferedOutputStream out = new BufferedOutputStream(
                    clientSocket.getOutputStream());

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                clientSocket.close();
                continue;
            }

            System.out.println(requestLine);

            String[] parts = requestLine.split(" ");
            String rawPath = parts[1];

            if (rawPath.equals("/")) {
                rawPath = "/index.html";
            }

            try {
                Path resolved = BASE_DIR
                        .resolve(rawPath.substring(1))
                        .normalize();

                if (!resolved.startsWith(BASE_DIR)) {
                    send404(out);
                    clientSocket.close();
                    continue;
                }
                
                Path realPath = resolved.toRealPath();

                if (!realPath.startsWith(BASE_DIR)) {
                    send404(out);
                    clientSocket.close();
                    continue;
                }

                if (Files.exists(realPath) && !Files.isDirectory(realPath)) {

                    byte[] fileBytes = Files.readAllBytes(realPath);
                    String contentType = getContentType(realPath.toString());

                    out.write(("HTTP/1.1 200 OK\r\n").getBytes());
                    out.write(("Content-Type: " + contentType + "\r\n").getBytes());
                    out.write(("Content-Length: " + fileBytes.length + "\r\n").getBytes());
                    out.write(("\r\n").getBytes());
                    out.write(fileBytes);

                } else {
                    send404(out);
                }

            } catch (IOException e) {
                send404(out);
            }

            out.flush();
            clientSocket.close();
        }
    }

    private static void send404(BufferedOutputStream out) throws IOException {
        String response = "<h1>404 Not Found</h1>";

        out.write(("HTTP/1.1 404 Not Found\r\n").getBytes());
        out.write(("Content-Type: text/html; charset=UTF-8\r\n").getBytes());
        out.write(("Content-Length: " + response.length() + "\r\n").getBytes());
        out.write(("\r\n").getBytes());
        out.write(response.getBytes());
    }

    private static String getContentType(String fileName) {

        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";

        return "application/octet-stream";
    }
}
