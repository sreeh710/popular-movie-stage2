package me.abhelly.movies.api;

import com.google.gson.annotations.SerializedName;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Movie response data model.
 * Created by abhelly on 07.06.15.
 */
public class MovieResponse {

    public ArrayList<Movie> results;

    public static class Movie implements Parcelable {

        @SerializedName("original_title")
        public String title;

        @SerializedName("poster_path")
        public String posterPath;

        public String overview;

        @SerializedName("vote_average")
        public float rating;

        @SerializedName("release_date")
        public String releaseDate;

        public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
            public Movie createFromParcel(Parcel in) {
                return new Movie(in);
            }

            public Movie[] newArray(int size) {
                return new Movie[size];
            }
        };

        private Movie(Parcel in) {
            title = in.readString();
            posterPath = in.readString();
            overview = in.readString();
            rating = in.readFloat();
            releaseDate = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(title);
            out.writeString(posterPath);
            out.writeString(overview);
            out.writeFloat(rating);
            out.writeString(releaseDate);
        }

        /**
         * Helper method to build poster image url.
         */
        public String getPosterUrl() {
            return IMAGE_BASE_URL + "w185" + this.posterPath;
        }

        private final static String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";

    }
}
