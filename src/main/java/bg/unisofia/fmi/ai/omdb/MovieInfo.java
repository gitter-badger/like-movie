package bg.unisofia.fmi.ai.omdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONObject;

public class MovieInfo {

    private String urlTitle;

    private String title;
    private String year;
    private String runtime;
    private String genre;
    private String director;
    private String plot;
    private String image;
    private String writer;
    private String actors;
    private String language;
    private String country;
    private String awards;
    private String imdbRating;
    private String id;

    public MovieInfo(String id, String title) {
        this.title = title;

        retrieveData();
    }

    public String getId() {
        return id;
    }

    public String getUrlTitle() {
        return urlTitle;
    }

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getGenre() {
        return genre;
    }

    public String getDirector() {
        return director;
    }

    public String getPlot() {
        return plot;
    }

    public String getImage() {
        return image;
    }

    public String getWriter() {
        return writer;
    }

    public String getActors() {
        return actors;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    public String getAwards() {
        return awards;
    }

    public String getImdbRating() {
        return imdbRating;
    }

    private void retrieveData() {

        try {
            this.urlTitle = URLEncoder.encode(this.title, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String urlString = "http://www.omdbapi.com/?t=" + this.urlTitle;

        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        URLConnection yc;
        try {
            yc = url.openConnection();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        String inputLine;
        String result = "";

        try {
            while ((inputLine = in.readLine()) != null) {
                result += inputLine;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JSONObject obj = new JSONObject(result);

        // TODO: create object in static method
        this.title = obj.getString("Title");
        this.year = obj.getString("Year");
        this.runtime = obj.getString("Runtime");
        this.genre = obj.getString("Genre");
        this.director = obj.getString("Director");
        this.plot = obj.getString("Plot");
        this.image = obj.getString("Poster");
        this.writer = obj.getString("Writer");
        this.actors = obj.getString("Actors");
        this.language = obj.getString("Language");
        this.country = obj.getString("Country");
        this.awards = obj.getString("Awards");
        this.imdbRating = obj.getString("imdbRating");
    }
}
