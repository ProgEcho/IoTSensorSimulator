package de.thkoeln.sensordaten;


import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class DynamicSensorResource {
    private final Random random = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final String datenTyp;
    private final double minValue;
    private final double maxValue;
    private final boolean hasTimestamp;

    public DynamicSensorResource(String datenTyp, double minValue, double maxValue, boolean hasTimestamp) {
        this.datenTyp = datenTyp;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.hasTimestamp = hasTimestamp;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorResponse getValue() {
        Object generatedValue = generateRandomValue();
        String timestamp = hasTimestamp ? LocalDateTime.now().format(formatter) : null;
        return new SensorResponse(timestamp, generatedValue);
    }

    private Object generateRandomValue() {
        switch (datenTyp.toLowerCase()) {
            case "string":
                return generateRandomString();
            case "integer":
                return (int) (minValue + random.nextDouble() * (maxValue - minValue));
            case "float":
                return (float) (minValue + random.nextDouble() * (maxValue - minValue));
            case "double":
                return minValue + random.nextDouble() * (maxValue - minValue);
            case "boolean":
                return random.nextBoolean();
            default:
                throw new IllegalArgumentException("Unsupported data type: " + datenTyp);
        }
    }

    private String generateRandomString() {
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 10;

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}