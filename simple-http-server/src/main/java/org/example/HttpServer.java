package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple HTTP server
 * for serving static files.
 */
public final class HttpServer {

    /** HTTP port for the server. */
    private static final int PORT = 8080;

    /** Base directory for static content. */
    private static final Path BASE_DIR =
            Paths.get("simple-http-server/static")
                    .toAbsolutePath()
                    .normalize();

    /**
     * Utility class constructor is hidden.
     */
    private HttpServer() {
        // utility class
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     * @throws IOException if server fails
     */
    public static void main(final String[] args) throws IOException {

        final ServerSocket serverSocket =
                new ServerSocket(PORT);

        System.out.println(
                "Server started at http://localhost:" + PORT
        );
        System.out.println(
                "Serving files from: " + BASE_DIR
        );

        while (true) {
            final Socket clientSocket =
                    serverSocket.accept();

            final BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(
                                    clientSocket.getInputStream()
                            )
                    );

            final BufferedOutputStream out =
                    new BufferedOutputStream(
                            clientSocket.getOutputStream()
                    );

            final String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                clientSocket.close();
                continue;
            }

            System.out.println(requestLine);

            final String[] parts =
                    requestLine.split(" ");
            String rawPath = parts[1];

            if ("/".equals(rawPath)) {
                rawPath = "/index.html";
            }

            try {
                final Path resolved =
                        BASE_DIR.resolve(
                                        rawPath.substring(1)
                                )
                                .normalize();

                if (!resolved.startsWith(BASE_DIR)) {
                    send404(out);
                    clientSocket.close();
                    continue;
                }

                final Path realPath =
                        resolved.toRealPath();

                if (!realPath.startsWith(BASE_DIR)) {
                    send404(out);
                    clientSocket.close();
                    continue;
                }

                if (Files.exists(realPath)
                        && !Files.isDirectory(realPath)) {

                    final byte[] fileBytes =
                            Files.readAllBytes(realPath);

                    final String contentType =
                            getContentType(
                                    realPath.toString()
                            );

                    out.write(
                            "HTTP/1.1 200 OK\r\n"
                                    .getBytes()
                    );

                    out.write(
                            ("Content-Type: "
                                    + contentType
                                    + "\r\n")
                                    .getBytes()
                    );

                    out.write(
                            ("Content-Length: "
                                    + fileBytes.length
                                    + "\r\n")
                                    .getBytes()
                    );

                    out.write("\r\n".getBytes());
                    out.write(fileBytes);

                } else {
                    send404(out);
                }

            } catch (final IOException e) {
                send404(out);
            }

            out.flush();
            clientSocket.close();
        }
    }

    /**
     * Sends a 404 Not Found response.
     *
     * @param out output stream
     * @throws IOException if write fails
     */
    private static void send404(
            final BufferedOutputStream out
    ) throws IOException {

        final String response =
                "<h1>404 Not Found</h1>";

        out.write(
                "HTTP/1.1 404 Not Found\r\n"
                        .getBytes()
        );

        out.write(
                ("Content-Type: text/html; "
                        + "charset=UTF-8\r\n")
                        .getBytes()
        );

        out.write(
                ("Content-Length: "
                        + response.length()
                        + "\r\n")
                        .getBytes()
        );

        out.write("\r\n".getBytes());
        out.write(response.getBytes());
    }

    /**
     * Resolves HTTP Content-Type
     * by file extension.
     *
     * @param fileName file name
     * @return content type
     */
    private static String getContentType(
            final String fileName
    ) {

        if (fileName.endsWith(".html")) {
            return "text/html";
        }
        if (fileName.endsWith(".css")) {
            return "text/css";
        }
        if (fileName.endsWith(".js")) {
            return "application/javascript";
        }
        if (fileName.endsWith(".png")) {
            return "image/png";
        }
        if (fileName.endsWith(".jpg")) {
            return "image/jpeg";
        }
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }

        return "application/octet-stream";
    }
}
