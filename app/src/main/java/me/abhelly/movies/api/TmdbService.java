package me.abhelly.movies.api;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * TheMovieDB api service.
 * Created by abhelly on 07.06.15.
 */
public interface TmdbService {

    // Example: /discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]
    @GET("/discover/movie")
    void getMovieList(@Query("sort_by") String sortBy, Callback<MovieResponse> callback);

}
