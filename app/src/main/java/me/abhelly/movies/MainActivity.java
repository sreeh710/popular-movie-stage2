package me.abhelly.movies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.abhelly.movies.api.MovieResponse;
import me.abhelly.movies.fragments.DetailsFragment;
import me.abhelly.movies.fragments.MoviesFragment;

/**
 * Movie list activity.
 * Created by abhelly on 07.06.15.
 */
public class MainActivity extends AppCompatActivity implements MoviesFragment.ListActionListener {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    MoviesFragment mMoviesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);

        mMoviesFragment = (MoviesFragment) getSupportFragmentManager()
                .findFragmentById(R.id.movies_fragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // check default or last selected option
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sortParam = prefs.getString(getString(R.string.prefs_sort_order),
                getString(R.string.sort_order_popularity));
        int selectedId;
        if (getString(R.string.sort_order_rating).equals(sortParam)) {
            selectedId = R.id.sort_order_rating;
        } else if (getString(R.string.sort_order_favorites).equals(sortParam)) {
            selectedId = R.id.sort_order_favorites;
        } else {
            selectedId = R.id.sort_order_popularity;
        }
        menu.findItem(selectedId).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String param = null;
        switch (item.getItemId()) {
            case R.id.sort_order_popularity:
                param = getString(R.string.sort_order_popularity);
                break;
            case R.id.sort_order_rating:
                param = getString(R.string.sort_order_rating);
                break;
            case R.id.sort_order_favorites:
                param = getString(R.string.sort_order_favorites);
                break;
        }
        if (param != null) {
            item.setChecked(true);
            mMoviesFragment.setSortOrder(param);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieSelected(MovieResponse.Movie movie, boolean isFavorite) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsFragment.MOVIE, movie);
        intent.putExtra(DetailsFragment.FAVORITE, isFavorite);
        startActivity(intent);
    }
}
