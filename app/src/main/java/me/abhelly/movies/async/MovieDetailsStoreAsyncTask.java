package me.abhelly.movies.async;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import me.abhelly.movies.provider.MovieProvider;

/**
 * AsyncTask to store movie trailers or reviews to db.
 * TODO: use {@link android.content.AsyncQueryHandler} if database grows.
 * Created by abhelly on 24.06.15.
 */
public class MovieDetailsStoreAsyncTask extends AsyncTask<ContentValues[], Void, Void> {

    private final Context mContext;

    private final Uri mContentUri;

    private final long mMovieId;

    public MovieDetailsStoreAsyncTask(Context context, Uri contentUri, long movieId) {
        mContext = context;
        mContentUri = contentUri;
        mMovieId = movieId;
    }

    @Override
    final protected Void doInBackground(ContentValues[]... params) {
        if (mContext != null) {
            // remove all trailers/reviews for given movie id
            ContentResolver cr = mContext.getContentResolver();
            cr.delete(mContentUri,
                    MovieProvider.COL_MOVIE_ID + "=?",
                    new String[]{Long.toString(mMovieId)});

            // insert all trailers/reviews
            ContentValues[] values = params[0];
            cr.bulkInsert(mContentUri, values);
        }
        return null;
    }
}
