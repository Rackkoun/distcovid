package cm.rulan.distcovid;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private final String TAG = "Stats";

    private LineChart graph;
    private List<Entry> entries;
    private LineDataSet lineDataSet;
    private ArrayList<ILineDataSet> iLineDataSets;
    private List<String> xaxis;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf_time = new SimpleDateFormat("hh:mm:ss");

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
