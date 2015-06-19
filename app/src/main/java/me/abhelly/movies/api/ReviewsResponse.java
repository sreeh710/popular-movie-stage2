package me.abhelly.movies.api;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Reviews response data model.
 * Created by abhelly on 17.06.15.
 */
public class ReviewsResponse {

    public ArrayList<Review> results;

    public static class Review implements Parcelable {

        public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
            public Review createFromParcel(Parcel in) {
                return new Review(in);
            }

            public Review[] newArray(int size) {
                return new Review[size];
            }
        };

        public String author;

        public String content;

        private Review(Parcel in) {
            author = in.readString();
            content = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(author);
            out.writeString(content);
        }

    }
}