package bg.unisofia.fmi.ai.recommend.stat;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import bg.unisofia.fmi.ai.dao.MovieService;
import bg.unisofia.fmi.ai.dao.UserService;
import bg.unisofia.fmi.ai.data.Movie;
import bg.unisofia.fmi.ai.data.Rating;
import bg.unisofia.fmi.ai.data.User;
import bg.unisofia.fmi.ai.data.Watching;
import bg.unisofia.fmi.ai.db.util.DbUtil;

import com.j256.ormlite.support.ConnectionSource;

public class UserStatistics {

    static ConnectionSource conn = DbUtil.getConnectionSource();
    static MovieService movieService = new MovieService(conn);
    static UserService userService = new UserService(conn);

    public static double getMeanRating(User user) {
        return user.getRatings().stream().mapToDouble(Rating::getRating).average().orElse(0);
    }

    public static double getStandardDeviation(User user, Set<Movie> movies) {
        if (movies.isEmpty())
            return 0;

        double meanUserRating = getMeanRating(user);

        double sumSquares = 0;
        for (Rating rating : user.getRatings()) {
            if (movies.contains(rating.getMovie())) {
                sumSquares += Math.pow((rating.getRating() - meanUserRating), 2);
            }
        }

        return Math.sqrt(sumSquares / movies.size());
    }

    public static Set<User> getRelatedUsers(User user) {
        Set<User> relatedUsers = new TreeSet<User>();

        user.getRatings()
                .stream()
                .map(Rating::getMovie)
                .forEach(
                        movie -> {
                            movieService.refresh(movie);
                            final Set<User> usersForCurrentMovie = movie.getRatings().stream().map(Rating::getUser)
                                    .collect(Collectors.toSet());
                            relatedUsers.addAll(usersForCurrentMovie);
                        });
        relatedUsers.remove(user);


        return relatedUsers;
    }

    public static Set<Movie> getMoviesInCommon(final User user, final User otherUser) {
        final Set<Movie> movies = new TreeSet<>(user.getRatings().stream().map(Rating::getMovie)
                .collect(Collectors.toSet()));
        final Set<Movie> otherUserMovies = new TreeSet<>(otherUser.getRatings().stream().map(Rating::getMovie)
                .collect(Collectors.toSet()));

        movies.retainAll(otherUserMovies);

        return movies;
    }

    public static Set<Movie> getMoviesDifference(final User user, final User otherUser) {
        final Set<Movie> movies = new TreeSet<>(user.getRatings().stream().map(Rating::getMovie)
                .collect(Collectors.toSet()));
        final Set<Movie> otherUserMovies = new TreeSet<>(otherUser.getRatings().stream().map(Rating::getMovie)
                .collect(Collectors.toSet()));
        final Set<Movie> watchedMovies = new TreeSet<>(otherUser.getWatchings().stream().map(Watching::getMovie)
                .collect(Collectors.toSet()));

        movies.removeAll(otherUserMovies);
        movies.removeAll(watchedMovies);

        return movies;
    }
}
