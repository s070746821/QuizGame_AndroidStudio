package com.gu11q.gu11qelementsquiz;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
 import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


import android.support.v4.app.Fragment;
 import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
import android.widget.Button;
 import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {



     private static final String TAG = "Elements Activity";

    private static final int ElEMENTS_IN_QUIZ = 20;

    private List<String> fileNameList; // flag file names
    private List<String> quizElementsList; // countries in current quiz
    private Set<String> ElementsSet; // world regions in current quiz
    private String correctAnswer; // correct country for the current flag
    private int totalGuesses; // number of guesses made
    private int correctAnswers; // number of correct guesses
    private int guessRows; // number of rows displaying guess Buttons
    private SecureRandom random; // used to randomize the quiz
    private Handler handler; // used to delay loading next flag
    private Animation shakeAnimation; // animation for incorrect guess

    private LinearLayout quizLinearLayout; // layout that contains the quiz
    private TextView questionNumberTextView; // shows current question #
    private ImageView elementImageView; // displays a flag
    private LinearLayout[] guessLinearLayouts; // rows of answer Buttons
    private TextView answerTextView; // displays correct answer


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view=inflater.inflate(R.layout.fragment_main, container, false);

        fileNameList= new ArrayList<>();
        quizElementsList= new ArrayList<>();
        random= new SecureRandom();
        handler= new Handler();

        shakeAnimation= AnimationUtils.loadAnimation(getActivity(), R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3);


        quizLinearLayout = (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView =(TextView) view.findViewById(R.id.questionNumberTextView);

        elementImageView= (ImageView) view.findViewById(R.id.ElementImageView);
        guessLinearLayouts= new LinearLayout[5];

        guessLinearLayouts[0] =(LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] =(LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] =(LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] =(LinearLayout) view.findViewById(R.id.row4LinearLayout);
        guessLinearLayouts[4] =(LinearLayout) view.findViewById(R.id.row5LinearLayout);


        for (LinearLayout row : guessLinearLayouts) {
                       for (int column = 0; column < row.getChildCount(); column++) {
                              Button button = (Button) row.getChildAt(column);
                             button.setOnClickListener(guessButtonListener);
                       }
        }

        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        questionNumberTextView.setText(
                       getString(R.string.question, 1, ElEMENTS_IN_QUIZ));

        return view; // return the fragment's view for display
    }


    public void updateGuessRows(SharedPreferences sharedPreferences) {
        // get the number of guess buttons that should be displayed
        String choices = sharedPreferences.getString(MainActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        // hide all quess button LinearLayouts
        for (LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);

        // display appropriate guess button LinearLayouts
        for (int row = 0; row < guessRows; row++)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);

    }

    public void updateElements(SharedPreferences sharedPreferences){
        ElementsSet=sharedPreferences.getStringSet(MainActivity.ELEMENTS, null);
    }

    public void resetQuiz() {

        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();

        try {

                        for (String element : ElementsSet) {
                              // get a list of all flag image files in this region
                              String[] paths = assets.list(element);

                               for (String path : paths)
                                      fileNameList.add(path.replace(".jpg", ""));
                        }
        }
        catch (IOException exception) {
                        Log.e(TAG, "Error loading image file names", exception);
        }

        correctAnswers = 0; // reset the number of correct answers made
        totalGuesses = 0; // reset the total number of guesses the user made
        quizElementsList.clear(); // clear prior list of quiz countries


        int flagCounter = 1;
        int numberOfEls = fileNameList.size();


        while (flagCounter <= ElEMENTS_IN_QUIZ) {
                        int randomIndex = random.nextInt(numberOfEls);

                       // get the random file name
                        String filename = fileNameList.get(randomIndex);

                       // if the region is enabled and it hasn't already been chosen
                       if (!quizElementsList.contains(filename)) {
                              quizElementsList.add(filename); // add the file to the list
                               ++flagCounter;
                            }
        }

        loadNext();




    }

    private void loadNext(){


        String nextImage = quizElementsList.remove(0);
         correctAnswer = nextImage; // update the correct answer
         answerTextView.setText(""); // clear answerTextView


        questionNumberTextView.setText(getString(R.string.question, (correctAnswers + 1), ElEMENTS_IN_QUIZ));

        String element= nextImage.substring(0, nextImage.indexOf('-'));

        AssetManager assets = getActivity().getAssets();

        try (InputStream stream = assets.open(element + "/" + nextImage + ".jpg")) {

                       Drawable flag = Drawable.createFromStream(stream, nextImage);
                      elementImageView.setImageDrawable(flag);

                       animate(false); // animate the flag onto the screen
        }
         catch (IOException exception) {
             Log.e(TAG, "Error loading " + nextImage, exception);
         }

        Collections.shuffle(fileNameList);
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));


        for (int row = 0; row < guessRows; row++) {

             for (int column = 0;
              column < guessLinearLayouts[row].getChildCount(); column++) {

                Button newGuessButton =
                (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);
                String filename = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getElementName(filename));
              }
         }


        int row = random.nextInt(guessRows); // pick random row
        int column = random.nextInt(2); // pick random column
        LinearLayout randomRow = guessLinearLayouts[row]; // get the row
        String countryName = getElementName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);


    }



    private String getElementName(String name) {
        return name.substring(name.indexOf('-') + 1).replace('_', ' ');
    }



    private void animate(boolean animateOut) {

        if (correctAnswers == 0) return;


        int centerX = (quizLinearLayout.getLeft() + quizLinearLayout.getRight()) / 2;
        int centerY = (quizLinearLayout.getTop() + quizLinearLayout.getBottom()) / 2;

        int radius = Math.max(quizLinearLayout.getWidth(), quizLinearLayout.getHeight());


        Animator animator;


        if (animateOut) {

            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, radius, 0);

            animator.addListener(
                    new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(Animator animation) {

                            loadNext();
                        }
                     }
            );
        }
        else {
                animator = ViewAnimationUtils.createCircularReveal(
                quizLinearLayout, centerX, centerY, 0, radius);
        }


        animator.setDuration(500);
        animator.start();
    }


    private OnClickListener guessButtonListener = new OnClickListener() {


        @Override
        public void onClick(View v) {

            ++totalGuesses;
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            String answer = getElementName(correctAnswer);


            if(guess.equals(answer)) {


                ++correctAnswers;




                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(getResources().getColor(R.color.correctAnswer, getContext().getTheme()));
                disableButtons();




                if(correctAnswers>=20){


                    AlertDialog.Builder Enddialog= new AlertDialog.Builder(getActivity());
                    double percent= 2000/totalGuesses;
                    String finalsring= percent+"% correct"+" 20"+ "/"+totalGuesses;
                    Enddialog.setMessage(finalsring);


                    Enddialog.setPositiveButton(R.string.reset,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {

                                    handler.postDelayed(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    animate(true); // animate the flag off the screen
                                                }
                                            }, 2000); // 2000 milliseconds for 2-second delay
                                    resetQuiz();
                                }
                            }
                    );

                    AlertDialog alert=Enddialog.create();
                    alert.show();

                }
                else { // answer is correct but quiz is not over
                    // load the next flag after a 2-second delay
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    animate(true); // animate the flag off the screen
                                }
                            }, 2000); // 2000 milliseconds for 2-second delay
                }







            }

            else { // answer was incorrect
                elementImageView.startAnimation(shakeAnimation); // play shake

                // display "Incorrect!" in red
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor( R.color.incorrectAnswer, getContext().getTheme()));
                guessButton.setEnabled(false); // disable incorrect answer
            }


        }


    };

    private void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int i = 0; i < guessRow.getChildCount(); i++)
                guessRow.getChildAt(i).setEnabled(false);
        }
    }




}
