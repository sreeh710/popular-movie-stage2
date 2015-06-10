package me.abhelly.movies.fragments;

import com.bumptech.glide.Glide;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import me.abhelly.movies.R;
import me.abhelly.movies.api.MovieResponse;
import me.abhelly.movies.api.MovieResponse.Movie;
import me.abhelly.movies.api.RetrofitAdapter;
import me.abhelly.movies.api.TmdbService;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Movie list fragment, displays movie posters sorted according to settings.
 * Created by abhelly on 07.06.15.
 */
public class MoviesFragment extends Fragment {

    private final static String ARG_ITEMS = "items";

    private final static String ARG_SORT_ORDER = "sort_order";

    private final static String ARG_VIEW_STATE = "view_state";

    private final static int VIEW_STATE_LOADING = 0;

    private final static int VIEW_STATE_ERROR = 1;

    private final static int VIEW_STATE_RESULTS = 2;

    private TextView mErrorTextView;

    private Button mRetryButton;

    private ProgressBar mProgressBar;

    private RecyclerView mRecyclerView;

    private MoviesAdapter mAdapter;

    private String mSortOrder;

    private ListActionListener mActionListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActionListener = (ListActionListener) activity;
        } catch (ClassCastException e) {
            Log.e(this.getClass().getName(),
                    "Activity must implement " + ListActionListener.class.getName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean reloadData = (mSortOrder != null && !mSortOrder.equalsIgnoreCase(getSortParam()));
        if (reloadData) {
            loadData();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_list, container, false);
        mErrorTextView = (TextView) v.findViewById(R.id.error_text_view);
        mRetryButton = (Button) v.findViewById(R.id.retry_button);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retry();
            }
        });
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        int orientation = getResources().getConfiguration().orientation;
        int spanCount = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? 4 : 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        mAdapter = new MoviesAdapter(getActivity(), mActionListener);
        mRecyclerView.setAdapter(mAdapter);
        if (savedInstanceState == null) {
            loadData();
        } else {
            mSortOrder = savedInstanceState.getString(ARG_SORT_ORDER);
            if (!mSortOrder.equalsIgnoreCase(getSortParam())) {
                loadData();
            }
            int state = savedInstanceState.getInt(ARG_VIEW_STATE, VIEW_STATE_ERROR);
            switch (state) {
                case VIEW_STATE_ERROR:
                    showErrorViews();
                    break;
                case VIEW_STATE_RESULTS:
                    ArrayList<Movie> items = savedInstanceState.getParcelableArrayList(ARG_ITEMS);
                    mAdapter.setItems(items);
                    showResultViews();
                    break;
                default:
                    showLoadingViews();
                    break;
            }
        }
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        int state = VIEW_STATE_RESULTS;
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            state = VIEW_STATE_LOADING;
        } else if (mErrorTextView.getVisibility() == View.VISIBLE) {
            state = VIEW_STATE_ERROR;
        }
        outState.putInt(ARG_VIEW_STATE, state);
        outState.putParcelableArrayList(ARG_ITEMS, mAdapter.getItems());
        outState.putString(ARG_SORT_ORDER, mSortOrder);
        super.onSaveInstanceState(outState);
    }

    /**
     * Loads movie list.
     */
    private void loadData() {
        showLoadingViews();
        RestAdapter adapter = RetrofitAdapter.getRestAdapter();
        TmdbService service = adapter.create(TmdbService.class);
        mSortOrder = getSortParam();
        service.getMovieList(mSortOrder, new Callback<MovieResponse>() {
            @Override
            public void success(MovieResponse movieResponse, Response response) {
                showResultViews();
                mAdapter.setItems(movieResponse.results);
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorViews();
            }
        });
    }

    /**
     * Retries to reload movie list.
     */
    private void retry() {
        loadData();
    }

    /**
     * Returns default (popularity) or set by user sortBy param.
     *
     * @return sortBy param
     */
    private String getSortParam() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultValue = getString(R.string.sort_order_popularity);
        return prefs.getString(getString(R.string.prefs_sort_order), defaultValue);
    }

    /**
     * Helper method to hide all elements, except progress bar.
     */
    private void showLoadingViews() {
        mProgressBar.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
    }

    /**
     * Helper method to hide all elements, except error views.
     */
    private void showErrorViews() {
        mErrorTextView.setVisibility(View.VISIBLE);
        mRetryButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
    }

    /**
     * Helper method to hide all elements, except recycler view.
     */
    private void showResultViews() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
    }

    /**
     * Movies RecyclerView adapter class.
     */
    private static class MoviesAdapter extends RecyclerView.Adapter<MovieViewHolder> {

        final private Context mContext;

        private ArrayList<Movie> mItems;

        final private ListActionListener mActionListener;

        public MoviesAdapter(Context context, ListActionListener listener) {
            mContext = context;
            mActionListener = listener;
            mItems = new ArrayList<>();
        }

        @Override
        public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.movie_item, parent, false);
            return new MovieViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MovieViewHolder holder, final int position) {
            Glide.with(mContext)
                    .load(mItems.get(position).getPosterUrl())
                    .centerCrop()
                    .placeholder(R.drawable.movie_placeholder)
                    .crossFade()
                    .into(holder.mPosterView);
            holder.mPosterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActionListener.onMovieSelected(mItems.get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public void setItems(ArrayList<Movie> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        public ArrayList<Movie> getItems() {
            return mItems;
        }

    }

    /**
     * Movie view holder class.
     */
    private static class MovieViewHolder extends RecyclerView.ViewHolder {

        final public ImageView mPosterView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            this.mPosterView = (ImageView) itemView.findViewById(R.id.poster_image_view);
        }
    }

    /**
     * Movie list action listener
     */
    public interface ListActionListener {

        void onMovieSelected(Movie movie);
    }
}
