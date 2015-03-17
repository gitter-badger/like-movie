package bg.unisofia.fmi.ai.routes;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.staticFileLocation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spark.ModelAndView;
import bg.unisofia.fmi.ai.dao.GenreService;
import bg.unisofia.fmi.ai.dao.MovieService;
import bg.unisofia.fmi.ai.dao.UserService;
import bg.unisofia.fmi.ai.data.Genre;
import bg.unisofia.fmi.ai.data.User;
import bg.unisofia.fmi.ai.db.util.DbUtil;
import bg.unisofia.fmi.ai.movieinfo.MovieInfo;
import bg.unisofia.fmi.ai.movieinfo.MovieInfoFetcher;
import bg.unisofia.fmi.ai.template.FreeMarkerEngine;
import bg.unisofia.fmi.ai.transformers.JsonTransformer;

import com.j256.ormlite.support.ConnectionSource;

public class Main {
    public final static int SIMILAR_MOVIES_NUMBER = 4;
    public final static int FRONT_PAGE_MOVIES = 10;

    private static final String USERNAME_ATTR = "username";
    private static final String USERID_ATTR = "userId";

    public static void main(String[] args) throws IOException, SQLException {
        staticFileLocation("/web");

        // DataImporter.movielensImporter("src/main/resources/datasets/movielens/");
        // DataImporter.customWikiExtractedFilesImporter("src/main/resources/datasets/wiki/");

        get("/", (request, response) -> {
            final ConnectionSource connection = DbUtil.getConnectionSource();
            final GenreService genreService = new GenreService(connection);

            Map<String, Object> attributes = new HashMap<>();
            List<MovieInfo> movies = new MovieInfoFetcher().getFrontPageMovies(FRONT_PAGE_MOVIES);
            attributes.put("genres", genreService.list());
            attributes.put("selectedGenre", "all");
            attributes.put("movies", movies);
            attributes.put("username", request.session().attribute(USERNAME_ATTR));

            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());

        get("/genre/:genreId", (request, response) -> {
            final ConnectionSource connection = DbUtil.getConnectionSource();
            final GenreService genreService = new GenreService(connection);

            String chosenGenreId = request.params(":genreId");
            Genre genre = genreService.find(Integer.parseInt(chosenGenreId));

            Map<String, Object> attributes = new HashMap<>();
            List<MovieInfo> movies = new MovieInfoFetcher().getMoviesWithGenre(FRONT_PAGE_MOVIES, genre);
            attributes.put("message", "Hello World!");
            attributes.put("genres", genreService.list());
            attributes.put("selectedGenre", genre.getName());
            attributes.put("movies", movies);
            attributes.put("username", request.session().attribute(USERNAME_ATTR));

            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());

        get("/register", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Register");
            return new ModelAndView(attributes, "register.ftl");

        }, new FreeMarkerEngine());

        post("/register", (request, response) -> {
            final ConnectionSource connection = DbUtil.getConnectionSource();
            final UserService userService = new UserService(connection);

            String username = request.queryParams("username");
            String password = request.queryParams("password");
            String passwordRepeat = request.queryParams("repeat_password");

            try {
                userService.registerUser(username, password, passwordRepeat);
            } catch (Exception e) {
                response.redirect("/register");
                return null;
            }
            userService.login(username, password);
            request.session().attribute(USERNAME_ATTR, username);

            response.redirect("/");
            return request;
        });

        get("/login", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Login");

            return new ModelAndView(attributes, "login.ftl");

        }, new FreeMarkerEngine());

        post("/login", (request, response) -> {
            final ConnectionSource connection = DbUtil.getConnectionSource();
            final UserService userService = new UserService(connection);

            final String username = request.queryParams("username");
            final String password = request.queryParams("password");

            User loggedUser;
            try {
                loggedUser = userService.login(username, password);
            } catch (Exception e) {
                response.redirect("/login");
                return request;
            }

            request.session().attribute(USERID_ATTR, loggedUser.getId());
            request.session().attribute(USERNAME_ATTR, username);

            response.redirect("/");
            return null;
        });

        get("/logout", (request, response) -> {
            request.session().removeAttribute("username");
            response.redirect("/");

            return request;
        });

        get("/movies/:movieId", (request, response) -> {
            final ConnectionSource connection = DbUtil.getConnectionSource();
            final GenreService genreService = new GenreService(connection);
            final MovieInfoFetcher fetcher = new MovieInfoFetcher();

            int chosenMovieId = Integer.parseInt(request.params(":movieId"));
            MovieInfo movieInfo = fetcher.getMovie(chosenMovieId);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("genres", genreService.list());
            attributes.put("movie", movieInfo);
            attributes.put("username", request.session().attribute(USERNAME_ATTR));
            attributes.put("movies", fetcher.getSimilarMovies(SIMILAR_MOVIES_NUMBER, movieInfo));

            return new ModelAndView(attributes, "preview.ftl");
        }, new FreeMarkerEngine());

        get("/movies", (request, response) -> {
            final ConnectionSource connection = DbUtil.getConnectionSource();
            final MovieService movieService = new MovieService(connection);

            final String searchText = request.queryParams("name");

            return movieService.autocompleteSearch(searchText);
        }, new JsonTransformer());

    }
}
