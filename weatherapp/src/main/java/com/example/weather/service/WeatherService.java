// src/main/java/com/example/weather/service/WeatherService.java

package com.example.weather.service;

import com.example.weather.model.WeatherData;
import com.example.weather.model.ForecastEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for fetching weather data from the OpenWeatherMap API.
 *
 * Provides methods to retrieve current weather, UV index, and forecast entries.
 */
public class WeatherService {

    private static final String BASE = "https://api.openweathermap.org/data/2.5/";
    private final String apiKey;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Cache<String, JsonNode> cache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();

    public WeatherService(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Low-level fetch helper that includes a 10s timeout and in-memory caching.
     *
     * @param pathAndQuery the path and query string (no leading slash)
     * @return the parsed JSON tree
     * @throws Exception if the HTTP request fails or JSON parsing fails
     */
    private JsonNode fetch(String pathAndQuery) throws Exception {
        JsonNode node = cache.getIfPresent(pathAndQuery);
        if (node != null) {
            return node;
        }
        URI uri = URI.create(BASE + pathAndQuery + "&appid=" + apiKey);
        HttpRequest req = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new RuntimeException("API error: " + res.statusCode());
        }
        node = mapper.readTree(res.body());
        cache.put(pathAndQuery, node);
        return node;
    }

    /**
     * Retrieves the current weather data for the specified city.
     *
     * @param city  the name of the city (e.g. "Prague")
     * @param units the unit system ("metric" or "imperial")
     * @return a {@link WeatherData} object populated from the API response
     * @throws Exception if the fetch or parsing fails
     */
    public WeatherData getCurrent(String city, String units) throws Exception {
        String q = "weather?q=" + URLEncoder.encode(city, StandardCharsets.UTF_8)
                 + "&units=" + units;
        JsonNode root = fetch(q);
        JsonNode main = root.get("main");
        JsonNode sys = root.get("sys");
        JsonNode coord = root.get("coord");
        JsonNode weather = root.get("weather").get(0);
        JsonNode windNode = root.get("wind");

        WeatherData wd = mapper.treeToValue(main, WeatherData.class);
        wd.setMain(weather.get("main").asText());
        wd.setDescription(weather.get("description").asText());
        wd.setIconCode(weather.get("icon").asText());
        wd.setWindSpeed(windNode.get("speed").asDouble());
        wd.setSunrise(sys.get("sunrise").asLong());
        wd.setSunset(sys.get("sunset").asLong());
        wd.setLat(coord.get("lat").asDouble());
        wd.setLon(coord.get("lon").asDouble());
        wd.setCity(root.get("name").asText());
        return wd;
    }

    /**
     * Retrieves the UV index for a given latitude/longitude.
     *
     * @param lat the latitude
     * @param lon the longitude
     * @return the UV index value
     * @throws Exception if the fetch or parsing fails
     */
    public double fetchUVIndex(double lat, double lon) throws Exception {
        String path = String.format("uvi?lat=%.6f&lon=%.6f", lat, lon);
        JsonNode j = fetch(path);
        return j.get("value").asDouble();
    }

    /**
     * Retrieves a list of forecast entries for the specified city.
     *
     * @param city  the name of the city
     * @param units the unit system ("metric" or "imperial")
     * @param cnt   the number of forecast data points to retrieve
     * @return a {@link List} of {@link ForecastEntry} objects
     * @throws Exception if the fetch or parsing fails
     */
    public List<ForecastEntry> getForecast(String city, String units, int cnt) throws Exception {
        String q = "forecast?q=" + URLEncoder.encode(city, StandardCharsets.UTF_8)
                 + "&units=" + units + "&cnt=" + cnt;
        JsonNode list = fetch(q).get("list");
        return StreamSupport.stream(list.spliterator(), false)
            .map(node -> {
                ForecastEntry fe = new ForecastEntry();
                fe.setTimestamp(node.get("dt").asLong());
                fe.setTemp(node.get("main").get("temp").asDouble());
                return fe;
            })
            .collect(Collectors.toList());
    }

    /**
     * Clears the internal cache of API responses.
     * Subsequent calls will fetch fresh data until the next cache expiration.
     */
    public void clearCache() {
        cache.invalidateAll();
    }
}
