/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jumptracker;

import java.awt.*;
import java.io.*;
import java.net.*;

/**
 *
 * @author Infaera
 */
public class URLIO {

    public static boolean openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean openWebpage(URL url) {
        try {
            return openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final String browser = "Mozilla";

    public static String sendGET(String link) throws IOException {
        URL obj = new URL(link);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", browser);

        int responseCode = con.getResponseCode();
        //System.out.println("GET Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder response;
            try ( // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream()))) {
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\n");//.append("\n");
                }
            }

            return response.toString();
        }

        return null;

    }

    public static String sendPOST(String link) throws IOException {
        URL obj = new URL(link);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", browser);
        //con.addRequestProperty("username", "named");

        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            os.flush();
        }

        int responseCode = con.getResponseCode();
        //System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder response;
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream()))) {
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);//.append("\n");
                }
            }

            //System.out.println(response.toString());
            return response.toString();
        }

        return null;
    }

//    public void printCodes() {
//        System.out.println("Accepted: " + HttpURLConnection.HTTP_ACCEPTED);
//        System.out.println("Forbidden: " + HttpURLConnection.HTTP_FORBIDDEN);
//        System.out.println("Bad Gateway: " + HttpURLConnection.HTTP_BAD_GATEWAY);
//        System.out.println("Unauthorized: " + HttpURLConnection.HTTP_UNAUTHORIZED);
//        System.out.println("Not Found: " + HttpURLConnection.HTTP_NOT_FOUND);
//        System.out.println("OK: " + HttpURLConnection.HTTP_OK);
//        System.out.println("Moved Permanently: " + HttpURLConnection.HTTP_MOVED_PERM);
//    }
}
