package me.abhelly.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import me.abhelly.movies.api.MovieResponse;
import me.abhelly.movies.fragments.DetailsFragment;
import me.abhelly.movies.fragments.MoviesFragment;

/**
 * Movie list activity.
 * Created by abhelly on 07.06.15.
 */
public class MainActivity extends AppCompatActivity implements MoviesFragment.ListActionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieSelected(MovieResponse.Movie movie) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsFragment.ARG_MOVIE, movie);
        startActivity(intent);
    }
}
