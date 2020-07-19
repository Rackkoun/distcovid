package cm.rulan.distcovid;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setTransitionAnimation();
    }

    private void setTransitionAnimation(){
        if (Build.VERSION.SDK_INT > 20){
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.BOTTOM);
            slide.setDuration(350);
            slide.setInterpolator(new DecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
    }

    public void goBackToMainActivity(View view){
        Intent intent = new Intent(this, MainActivity.class);

        if (Build.VERSION.SDK_INT > 20){
            ActivityOptions activityOptions = ActivityOptions
                    .makeSceneTransitionAnimation(this);
            startActivity(intent, activityOptions.toBundle());
        }else {
            startActivity(intent);
        }
    }
}
