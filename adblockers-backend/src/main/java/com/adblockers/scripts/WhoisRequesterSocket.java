package com.adblockers.scripts;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
public class WhoisRequesterSocket implements WhoisRequester {

    private static final Logger LOGGER = Logger.getLogger(WhoisRequesterSocket.class);

    private static final String WHOIS_DEFAULT_HOST = "whois.internic.net";
    private static final Integer WHOIS_DEFAULT_PORT = 43;

    public List<String> getResponse(String domain) throws IOException {
        return getResponse(WHOIS_DEFAULT_HOST, domain);
    }

    public List<String> getResponse(String databaseHost, String domain) throws IOException {
        LOGGER.info("Requesting info from " + databaseHost + ":" + WHOIS_DEFAULT_PORT + " for domain " + domain);
        try (Socket socket = new Socket(databaseHost, WHOIS_DEFAULT_PORT)) {
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println("whois " + domain);
            List<String> whoisResponse = new LinkedList<>();
            for (String line = bufferedReader.readLine();
                 line != null;
                 line = bufferedReader.readLine()) {
                LOGGER.error("Socket exception occurred");
                whoisResponse.add(line);
            }
            return whoisResponse;
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
    }
}
