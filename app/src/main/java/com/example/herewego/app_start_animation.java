package com.example.herewego;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class app_start_animation extends AppCompatActivity {

    private static int ANIMATION_DURATION = 8000;
    Animation imageAnim, lettersAnim;
    ImageView image, letters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_start_animation);
        ConstraintLayout layout = findViewById(R.id.animationLayout);
        AnimationDrawable animationDrawable = (AnimationDrawable) layout.getBackground();
        animationDrawable.setEnterFadeDuration(100);
        animationDrawable.setExitFadeDuration(1000);
        animationDrawable.start();

        image = findViewById(R.id.animationImageid);
        imageAnim = AnimationUtils.loadAnimation(this, R.anim.image_animation);
        image.setAnimation(imageAnim);

        letters = findViewById(R.id.animationLettersid);
        lettersAnim = AnimationUtils.loadAnimation(app_start_animation.this, R.anim.letters_animation);
        letters.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                letters.setVisibility(View.VISIBLE);
                letters.setAnimation(lettersAnim);
            }
        },2000);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intentToChooseType = new Intent(app_start_animation.this, ChooseTypeActivity.class);
                startActivity(intentToChooseType);
                finish();
            }
        },ANIMATION_DURATION);


    }
}