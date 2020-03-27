package com.gu11q.gu11qelementsquiz;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import java.util.Set;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {


    public static final String CHOICES = "pref_number_of_choices";
    public static final String ELEMENTS = "pref_ElementsToInclude";


    private boolean phoneDevice = true;
    private boolean preferencesChanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferencesChangeListener);

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            phoneDevice = false;

        if (phoneDevice) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChanged) {

            MainActivityFragment quizFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);

            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));

            quizFragment.updateElements(PreferenceManager.getDefaultSharedPreferences(this));

            quizFragment.resetQuiz();
            preferencesChanged = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        } else return false;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private OnSharedPreferenceChangeListener preferencesChangeListener =
                     new OnSharedPreferenceChangeListener() {
                   // called when the user changes the app's preferences
                    @Override
                    public void onSharedPreferenceChanged(
                              SharedPreferences sharedPreferences, String key) {
                          preferencesChanged = true;

                           MainActivityFragment quizFragment = (MainActivityFragment)
                                      getSupportFragmentManager().findFragmentById(
                                        R.id.quizFragment);

                           if (key.equals(CHOICES)) {
                                  quizFragment.updateGuessRows(sharedPreferences);
                                  quizFragment.resetQuiz();
                               }
                          else if (key.equals(ELEMENTS)) { // regions to include changed
                                  Set<String> elements =
                                             sharedPreferences.getStringSet(ELEMENTS, null);

                                 if (elements != null && elements.size() > 0) {
                                         quizFragment.updateElements(sharedPreferences);
                                        quizFragment.resetQuiz();
                                      }
                                  else {
                                         // must select one region--set North America as default
                                         SharedPreferences.Editor editor =
                                                   sharedPreferences.edit();
                                         elements.add(getString(R.string.default_element));
                                        editor.putStringSet(ELEMENTS, elements);
                                        editor.apply();

                                        Toast.makeText(MainActivity.this,
                                                    R.string.default_message,
                                                    Toast.LENGTH_SHORT).show();
                                      }
                               }

                           Toast.makeText(MainActivity.this,
                                      R.string.restart,
                                      Toast.LENGTH_SHORT).show();
                        }
              };
   }



