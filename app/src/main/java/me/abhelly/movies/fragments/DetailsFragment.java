package me.abhelly.movies.fragments;

import com.bumptech.glide.Glide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import me.abhelly.movies.R;
import me.abhelly.movies.api.MovieResponse.Movie;
import me.abhelly.movies.api.RetrofitAdapter;
import me.abhelly.movies.api.ReviewsResponse;
import me.abhelly.movies.api.ReviewsResponse.Review;
import me.abhelly.movies.api.TmdbService;
import me.abhelly.movies.api.TrailersResponse;
import me.abhelly.movies.api.TrailersResponse.Trailer;
import me.abhelly.movies.util.BackdropTransformation;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Fragment represents detailed movie info.
 * Created by abhelly on 07.06.15.
 */
public class DetailsFragment extends Fragment {

    public final static String MOVIE = "movie";

    public final static String FAVORITE = "favorite";

    private final static String ARG_TRAILERS = "trailers";

    private final static String ARG_REVIEWS = "reviews";

    private final static String[] RANDOM_COLORS = {"#EF9A9A", "#F48FB1", "#B39DDB", "#9FA8DA",
            "#90CAF9", "#81D4FA", "#80DEEA", "#80CBC4", "#A5D6A7", "#C5E1A5", "#E6EE9C", "#FFE082",
            "#FFCC80", "#FFAB91", "#BCAAA4", "#B0BEC5"};

    @Optional
    @InjectView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;

    @Optional
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.favorite_action_button)
    FloatingActionButton mActionButton;

    @InjectView(R.id.backdrop_image_view)
    ImageView mBackdropImageView;

    @InjectView(R.id.poster_image_view)
    ImageView mPosterImageView;

    @InjectView(R.id.rating_text_view)
    TextView mRatingTextView;

    @InjectView(R.id.date_text_view)
    TextView mDateTextView;

    @InjectView(R.id.overview_text_view)
    TextView mOverviewTextView;

    @InjectView(R.id.empty_overview_text_view)
    TextView mEmptyOverviewTextView;

    @InjectView(R.id.trailers_parent)
    LinearLayout mTrailersParent;

    @InjectView(R.id.empty_trailers_text_view)
    TextView mEmptyTrailersTextView;

    @InjectView(R.id.reviews_parent)
    LinearLayout mReviewsParent;

    @InjectView(R.id.empty_reviews_text_view)
    TextView mEmptyReviewsTextView;

    MenuItem mShareMenuItem;

    private boolean mTabletLayout;

    private Movie mMovie;

    private boolean isFavorite;

    private ArrayList<Trailer> mTrailers;

    private ArrayList<Review> mReviews;

    private TmdbService mTmdbService;

    public static DetailsFragment getInstance(Movie movie, boolean isFavorite) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(MOVIE, movie);
        args.putBoolean(FAVORITE, isFavorite);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMovie = getArguments().getParcelable(MOVIE);
        isFavorite = getArguments().getBoolean(FAVORITE);
        if (mMovie == null) {
            throw new NullPointerException("Movie object should be put into fragment arguments.");
        }
        RestAdapter restAdapter = RetrofitAdapter.getRestAdapter();
        mTmdbService = restAdapter.create(TmdbService.class);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_details, menu);
        mShareMenuItem = menu.findItem(R.id.action_share);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            }
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text,
                    mTrailers.get(0).getYoutubeLink()));
            startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie, container, false);
        ButterKnife.inject(this, v);

        mTabletLayout = (mToolbar == null);
        if (!mTabletLayout) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            mCollapsingToolbar.setTitle(mMovie.title);
        }

        // tint & set compound drawables
        setCompoundDrawable(mRatingTextView, R.drawable.ic_rating);
        setCompoundDrawable(mDateTextView, R.drawable.ic_release);

        // setup data
        if (!mTabletLayout) {
            BackdropTransformation transformation = new BackdropTransformation(getActivity(),
                    Glide.get(getActivity()).getBitmapPool());
            Glide.with(this)
                    .load(mMovie.getBackdropUrl())
                    .centerCrop()
                    .bitmapTransform(transformation)
                    .into(mBackdropImageView);
        } else {
            // TODO: revisit it
            Glide.with(this)
                    .load(mMovie.getBackdropUrl())
                    .centerCrop()
                    .into(mBackdropImageView);
        }
        Glide.with(this)
                .load(mMovie.getPosterUrl())
                .crossFade()
                .into(mPosterImageView);
        mRatingTextView.setText(Float.toString(mMovie.rating));
        mDateTextView.setText(mMovie.releaseDate);
        if (mMovie.overview != null) {
            mOverviewTextView.setText(mMovie.overview);
            mOverviewTextView.setVisibility(View.VISIBLE);
            mEmptyOverviewTextView.setVisibility(View.GONE);
        }
        setFavorite(isFavorite);
        if (savedInstanceState == null) {
            loadData();
        } else {
            mTrailers = savedInstanceState.getParcelableArrayList(ARG_TRAILERS);
            populateTrailers();
            mReviews = savedInstanceState.getParcelableArrayList(ARG_REVIEWS);
            populateReviews();
        }
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ARG_TRAILERS, mTrailers);
        outState.putParcelableArrayList(ARG_REVIEWS, mReviews);
        super.onSaveInstanceState(outState);
    }

    /**
     * Action handlers.
     */
    @OnClick(R.id.favorite_action_button)
    void onFavoriteAction() {
        favoriteMovie();
        String message = (isFavorite)
                ? getString(R.string.favorite_added)
                : getString(R.string.favorite_removed);
        Snackbar
                .make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction(R.string.favorite_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        favoriteMovie();
                    }
                })
                .show();
    }

    /** Marks current movie as favorite/not favorite, updates favorites list. */
    private void favoriteMovie() {
        setFavorite(!isFavorite);
        // TODO: notify movies fragment if tablet
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String favoritesString = prefs.getString(getString(R.string.prefs_favorites), "");
        ArrayList<Long> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(favoritesString, ",");
        while (st.hasMoreTokens()) {
            list.add(Long.parseLong(st.nextToken()));
        }
        if (isFavorite) {
            list.add(mMovie.id);
        } else {
            list.remove(mMovie.id);
        }
        StringBuilder sb = new StringBuilder();
        for (Long id : list) {
            sb.append(id).append(",");
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.prefs_favorites), sb.toString());
        editor.apply();
    }

    private void openTrailer(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    /** Loads data first from local db, then updates from server. */
    private void loadData() {
        // TODO: first, load from db
        refreshData();
    }

    /** Loads data from servers, stores response to local db. */
    private void refreshData() {
        // TODO: store both responses to db
        mTmdbService.getTrailers(mMovie.id, new Callback<TrailersResponse>() {
            @Override
            public void success(TrailersResponse trailers, Response response) {
                mTrailers = trailers.results;
                populateTrailers();
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });

        mTmdbService.getReviews(mMovie.id, new Callback<ReviewsResponse>() {
            @Override
            public void success(ReviewsResponse reviews, Response response) {
                mReviews = reviews.results;
                populateReviews();
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    /**
     * UI setup.
     */
    private void populateTrailers() {
        if (!isAdded()) {
            return;
        }
        if (mTrailers != null && mTrailers.size() > 0) {
            mEmptyTrailersTextView.setVisibility(View.GONE);
            Context context = getActivity();
            int paddingSmall = Math.round(getResources().getDimension(R.dimen.margin_small));
            int paddingTiny = Math.round(getResources().getDimension(R.dimen.margin_tiny));
            for (final Trailer item : mTrailers) {
                TextView view = new TextView(context);
                view.setText(item.name);
                view.setCompoundDrawablePadding(paddingSmall);
                setCompoundDrawable(view, R.drawable.ic_video);
                view.setPadding(0, paddingTiny, 0, paddingTiny);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openTrailer(item.getYoutubeLink());
                    }
                });
                mTrailersParent.addView(view);
            }
        } else {
            mShareMenuItem.setVisible(false);
        }
    }

    private void populateReviews() {
        if (!isAdded()) {
            return;
        }
        if (mReviews != null && mReviews.size() > 0) {
            mEmptyReviewsTextView.setVisibility(View.GONE);
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            Random random = new Random();
            for (Review item : mReviews) {
                View view = inflater.inflate(R.layout.review_item, mReviewsParent, false);
                ReviewViewHolder viewHolder = new ReviewViewHolder(view);
                viewHolder.nameTextView.setText(item.author);
                viewHolder.contentTextView.setText(item.content);
                int tint = Color.parseColor(RANDOM_COLORS[random.nextInt(RANDOM_COLORS.length)]);
                Drawable drawable = ContextCompat
                        .getDrawable(getActivity(), R.drawable.circle_background);
                drawable.setColorFilter(tint, PorterDuff.Mode.MULTIPLY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.avatarImageView.setBackground(drawable);
                } else {
                    viewHolder.avatarImageView.setBackgroundDrawable(drawable);
                }
                mReviewsParent.addView(view);
            }
        }
    }

    private void setFavorite(boolean favorite) {
        isFavorite = favorite;
        int resId;
        if (isFavorite) {
            resId = R.drawable.ic_action_favorite_selected;
        } else {
            resId = R.drawable.ic_action_favorite;
        }
        mActionButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), resId));
    }

    private void setCompoundDrawable(TextView view, int resId) {
        Drawable ratingIcon = ContextCompat.getDrawable(getActivity(), resId);
        ratingIcon = DrawableCompat.wrap(ratingIcon);
        DrawableCompat.setTint(ratingIcon, getResources().getColor(R.color.icon_tint));
        view.setCompoundDrawablesWithIntrinsicBounds(ratingIcon, null, null, null);
    }

    class ReviewViewHolder {

        @InjectView(R.id.avatar_view)
        ImageView avatarImageView;

        @InjectView(R.id.name_text_view)
        TextView nameTextView;

        @InjectView(R.id.content_text_view)
        TextView contentTextView;

        public ReviewViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}
