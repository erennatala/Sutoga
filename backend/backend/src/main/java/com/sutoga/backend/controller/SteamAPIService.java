package com.sutoga.backend.controller;

import com.sutoga.backend.entity.Category;
import com.sutoga.backend.entity.Game;
import com.sutoga.backend.entity.Genre;
import com.sutoga.backend.repository.CategoryRepository;
import com.sutoga.backend.repository.GameRepository;
import com.sutoga.backend.repository.GenreRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@Transactional
public class SteamAPIService {
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final CategoryRepository categoryRepository;
    private final WebClient steamSpyWebClient;
    private final WebClient steamWebClient;

    @Autowired
    public SteamAPIService(GameRepository gameRepository, CategoryRepository categoryRepository, GenreRepository genreRepository) {
        this.gameRepository = gameRepository;
        this.genreRepository = genreRepository;
        this.categoryRepository = categoryRepository;

        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(10));
        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10485760))
                .build();

        this.steamSpyWebClient = WebClient.builder()
                .baseUrl("http://steamspy.com")
                .clientConnector(connector)
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        this.steamWebClient = WebClient.builder()
                .baseUrl("http://store.steampowered.com")
                .clientConnector(connector)
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void saveGame(Game game) {
        for (Genre genre : game.getGenres()) {
            Genre existingGenre = genreRepository.findByName(genre.getName());
            if (existingGenre != null) {
                genre.setId(existingGenre.getId());
            }
        }

        for (Category category : game.getCategories()) {
            Category existingCategory = categoryRepository.findByName(category.getName());
            if (existingCategory != null) {
                category.setId(existingCategory.getId());
            }
        }

        gameRepository.save(game);
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game getGameById(Long id) {
        return gameRepository.findById(id).orElse(null);
    }

    public void fetchAllSteamGames() {
        String steamApiUrl = "/ISteamApps/GetAppList/v0002/?format=json";

        Mono<String> responseMono = steamWebClient.get()
                .uri(steamApiUrl)
                .retrieve()
                .bodyToMono(String.class);

        responseMono.subscribe(response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                JSONObject appList = jsonResponse.getJSONObject("applist");
                JSONArray apps = appList.getJSONArray("apps");

                for (int i = 0; i < apps.length(); i++) {
                    JSONObject app = apps.getJSONObject(i);
                    long appId = app.getLong("appid");
                    String appName = app.getString("name");
                }
            } catch (JSONException ex) {
                System.out.println("Invalid JSON response: " + ex.getMessage());
            }
        });
    }

    private String truncateDescription(String description, int maxLength) {
        if (description.length() <= maxLength) {
            return description;
        } else {
            return description.substring(0, maxLength);
        }
    }

    public void getGameDetails(long appId) {
        String steamApiUrl = "/api/appdetails?appids=" + appId;
        Duration delayDuration = Duration.ofSeconds(5);

        Mono<String> responseMono = steamWebClient.get()
                .uri(steamApiUrl)
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().is3xxRedirection()) {
                        String redirectedUrl = clientResponse.headers().header(HttpHeaders.LOCATION).get(0);
                        return steamWebClient.get()
                                .uri(redirectedUrl)
                                .retrieve()
                                .bodyToMono(String.class);
                    } else {
                        return clientResponse.bodyToMono(String.class);
                    }
                })
                .single() // Convert Flux to Mono
                .delayElement(delayDuration)
                .retryWhen(Retry.fixedDelay(3, delayDuration)
                        .doBeforeRetry(retrySignal -> {
                            System.out.println("Retrying request...");
                        }));

        responseMono.subscribe(response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONObject gameData = jsonResponse.getJSONObject(String.valueOf(appId));

                if (gameData.getBoolean("success")) {
                    JSONObject gameInfo = gameData.getJSONObject("data");

                    Game game = new Game();
                    game.setAppid(appId);
                    game.setTitle(gameInfo.getString("name"));
                    //game.setDescription(gameInfo.getString("short_description"));

//                    String shortDescription = gameInfo.getString("short_description");
//
//                    // Kısaltılmış bir açıklama oluştur
//                    String truncatedDescription = truncateDescription(shortDescription, 1000); // 500 karakter sınırlaması örneği
//
//                    game.setDescription(truncatedDescription);


                    JSONObject releaseDateObject = gameInfo.optJSONObject("release_date");
                    if (releaseDateObject != null && !releaseDateObject.isNull("date")) {
                        String releaseDate = releaseDateObject.getString("date");
                        LocalDate parsedDate = null;
                        String[] dateFormats = {
                                "d MMM, yyyy",
                                "MMM d, yyyy"
                                // Diğer tarih formatlarını buraya ekleyebilirsiniz
                        };
                        for (String dateFormat : dateFormats) {
                            try {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
                                parsedDate = LocalDate.parse(releaseDate, formatter);
                                break;
                            } catch (DateTimeParseException e) {
                                // Tarih formatı uymuyorsa bir sonraki formata geçmek için devam et
                            }
                        }
                        if (parsedDate != null) {
                            game.setReleaseDate(parsedDate);
                        } else {
                            System.out.println("Invalid date format: " + releaseDate);
                        }
                    }

                    System.out.println(game.getTitle());

                    JSONArray developersArray = gameInfo.getJSONArray("developers");
                    JSONArray publishersArray = gameInfo.getJSONArray("publishers");

                    game.setDeveloper(getStringFromArray(developersArray));
                    game.setPublisher(getStringFromArray(publishersArray));

                    if (gameInfo.has("genres")) {
                        JSONArray genresArray = gameInfo.getJSONArray("genres");
                        List<Genre> genres = new ArrayList<>();
                        for (int i = 0; i < genresArray.length(); i++) {
                            String genreName = genresArray.getJSONObject(i).getString("description");
                            Genre genre = genreRepository.findByName(genreName);
                            if (genre == null) {
                                genre = new Genre();
                                genre.setName(genreName);
                                genre = genreRepository.save(genre);
                            }
                            genres.add(genre);
                        }
                        game.setGenres(genres);
                    }

                    // Get categories
                    if (gameInfo.has("categories")) {
                        JSONArray categoriesArray = gameInfo.getJSONArray("categories");
                        List<Category> categories = new ArrayList<>();
                        for (int i = 0; i < categoriesArray.length(); i++) {
                            String categoryName = categoriesArray.getJSONObject(i).getString("description");
                            Category category = categoryRepository.findByName(categoryName);
                            if (category == null) {
                                category = new Category();
                                category.setName(categoryName);
                                category = categoryRepository.save(category);
                            }
                            categories.add(category);
                        }
                        game.setCategories(categories);
                    }

                    saveGame(game);
                } else {
                    System.out.println("Oyun bulunamadı: " + appId);
                }
            } catch (JSONException ex) {
                System.out.println("Invalid JSON response: " + ex.getMessage());
            }
        });
    }

    public void fetchTop100Games() {
        String steamSpyApiUrl = "/api.php?request=top100forever";

        Mono<String> responseMono = steamSpyWebClient.get()
                .uri(steamSpyApiUrl)
                .headers(headers -> headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"))
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().is3xxRedirection()) {
                        String redirectedUrl = clientResponse.headers().header(HttpHeaders.LOCATION).get(0);
                        return steamSpyWebClient.get()
                                .uri(redirectedUrl)
                                .headers(headers -> headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"))
                                .retrieve()
                                .bodyToMono(String.class);
                    } else {
                        return clientResponse.bodyToMono(String.class);
                    }
                });

        responseMono.subscribe(response -> {
            try {
                JSONObject gamesObject = new JSONObject(response);

                // Get keys (appids) of the games
                Iterator<String> keys = gamesObject.keys();

                // Loop through each game using its appid
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject gameObject = gamesObject.getJSONObject(key);

                    long appId = Long.parseLong(key);

                    getGameDetails(appId);
                }
            } catch (JSONException ex) {
                System.out.println("Invalid JSON response: " + ex.getMessage());
            }
        });
    }

    public void fetchApps1000To1999() {
        String steamSpyApiUrl = "/api.php?request=all&page=1";

        Mono<String> responseMono = steamSpyWebClient.get()
                .uri(steamSpyApiUrl)
                .headers(headers -> headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"))
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().is3xxRedirection()) {
                        String redirectedUrl = clientResponse.headers().header(HttpHeaders.LOCATION).get(0);
                        return steamSpyWebClient.get()
                                .uri(redirectedUrl)
                                .headers(headers -> headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"))
                                .retrieve()
                                .bodyToMono(String.class);
                    } else {
                        return clientResponse.bodyToMono(String.class);
                    }
                });

        responseMono.subscribe(response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                JSONArray gameArray = jsonResponse.names();

                for (int i = 0; i < gameArray.length(); i++) {
                    String appId = gameArray.getString(i);
                    JSONObject gameObject = jsonResponse.getJSONObject(appId);

                    String appName = gameObject.getString("name");
                    String developer = gameObject.getString("developer");
                    String publisher = gameObject.getString("publisher");
                    int positive = gameObject.getInt("positive");
                    int negative = gameObject.getInt("negative");
                    int userScore = gameObject.getInt("userscore");
                    String owners = gameObject.getString("owners");
                    int averageForever = gameObject.getInt("average_forever");
                    int average2Weeks = gameObject.getInt("average_2weeks");
                    int medianForever = gameObject.getInt("median_forever");
                    int median2Weeks = gameObject.getInt("median_2weeks");
                    String price = gameObject.getString("price");
                    String initialPrice = gameObject.getString("initialprice");
                    String discount = gameObject.getString("discount");
                    int ccu = gameObject.getInt("ccu");

                    Game game = new Game();
                    game.setAppid(Long.parseLong(appId));
                    game.setTitle(appName);
                    game.setDeveloper(developer);
                    game.setPublisher(publisher);
//                    game.setPositiveReviews(positive);
//                    game.setNegativeReviews(negative);
//                    game.setUserScore(userScore);
//                    game.setOwners(owners);
//                    game.setAverageForever(averageForever);
//                    game.setAverage2Weeks(average2Weeks);
//                    game.setMedianForever(medianForever);
//                    game.setMedian2Weeks(median2Weeks);
//                    game.setPrice(price);
//                    game.setInitialPrice(initialPrice);
//                    game.setDiscount(discount);
//                    game.setCCU(ccu);

                    System.out.println(appName + "\n");

                    saveGame(game);
                }
            } catch (JSONException ex) {
                System.out.println("Invalid JSON response: " + ex.getMessage());
            }
        });
    }

    private String getStringFromArray(JSONArray jsonArray) throws JSONException {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < jsonArray.length(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(jsonArray.getString(i));
        }

        return builder.toString();
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (Exception ex) {
            return null;
        }
    }
}