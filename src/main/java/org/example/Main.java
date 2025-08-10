package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import org.json.JSONObject;

public class Main {

    // The access token provided in the problem description
    private static final String ACCESS_TOKEN = "bf6ce7a3b868d760";
    private static final String BASE_URL = "https://hackattic.com/challenges/help_me_unpack/";

    public static void main(String[] args) {
        try {
            // Step 1: Fetch the problem data from the endpoint
            JSONObject problemJson = fetchProblem();
            System.out.println(problemJson);
            String base64Bytes = problemJson.getString("bytes");

            // Step 2: Decode the Base64 string into a byte array
            byte[] bytes = Base64.getDecoder().decode(base64Bytes);
            System.out.println("Decoded byte array has a length of: " + bytes.length);

            // Step 3: Unpack the values from the byte buffer
            JSONObject solutionJson = unpackBytes(bytes);

            // Step 4: Submit the solution to the endpoint
            submitSolution(solutionJson);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches the problem's Base64-encoded string from the API endpoint.
     * @return A JSONObject containing the problem data.
     * @throws IOException if there's an issue with the HTTP request.
     * @throws InterruptedException if the thread is interrupted.
     */
    private static JSONObject fetchProblem() throws IOException, InterruptedException {
        System.out.println("Fetching problem data...");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "problem?access_token=" + ACCESS_TOKEN))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new JSONObject(response.body());
    }

    /**
     * Unpacks the required values from the byte array based on the challenge rules.
     * @param bytes The decoded byte array.
     * @return A JSONObject with the unpacked values.
     */
    private static JSONObject unpackBytes(byte[] bytes) {
        // Wrap the byte array in a ByteBuffer for easy unpacking
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // Assume little-endian for the first few values as per typical platform defaults
        // The problem explicitly mentions big-endian for the second double, implying the rest are little-endian.
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Read the values in the specified order
        int signedInt = buffer.getInt();
        // Unsigned int is read as a signed int and then converted to a long to prevent overflow
        long unsignedInt = Integer.toUnsignedLong(buffer.getInt());
        short signedShort = buffer.getShort();

        // Change the byte order specifically for the last double
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.order(ByteOrder.BIG_ENDIAN);
        float floatValue = buffer.getFloat();
        double firstDouble = buffer.getDouble();
        double bigEndianDouble = buffer.getDouble();

        System.out.println("Unpacked values:");
        System.out.println("  Signed Int: " + signedInt);
        System.out.println("  Unsigned Int: " + unsignedInt);
        System.out.println("  Signed Short: " + signedShort);
        System.out.println("  Float: " + floatValue);
        System.out.println("  Little-endian Double: " + firstDouble);
        System.out.println("  Big-endian Double: " + bigEndianDouble);

        // Create the JSON object for the solution
        JSONObject solution = new JSONObject();
        solution.put("int", signedInt);
        solution.put("uint", unsignedInt);
        solution.put("short", signedShort);
        solution.put("float", floatValue);
        solution.put("double", firstDouble);
        solution.put("big_endian_double", bigEndianDouble);
        System.out.println(solution);

        return solution;
    }

    /**
     * Submits the solution JSON to the API endpoint.
     * @param solution The JSON object containing the unpacked values.
     * @throws IOException if there's an issue with the HTTP request.
     * @throws InterruptedException if the thread is interrupted.
     */
    private static void submitSolution(JSONObject solution) throws IOException, InterruptedException {
        System.out.println("Submitting solution...");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "solve?access_token=" + ACCESS_TOKEN))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(solution.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
    }
}
