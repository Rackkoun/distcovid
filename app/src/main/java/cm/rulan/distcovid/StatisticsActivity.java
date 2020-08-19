package cm.rulan.distcovid;

import android.app.ActivityOptions;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import cm.rulan.distcovid.database.StatsDataDB;
import cm.rulan.distcovid.model.DistcovidModelManager;
import cm.rulan.distcovid.model.DistcovidModelObject;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private LineChart graph;
    private List<Entry> entries;
    private LineDataSet lineDataSet;
    private ArrayList<ILineDataSet> iLineDataSets;
    private List<String> xaxis;

    private StatsDataDB dbHelper;
    private SQLiteDatabase database;
    private final DistcovidModelManager manager = new DistcovidModelManager();

    TextView valueTv;
    TextView dateTv;
    TextView timeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setTransitionAnimation();
        // init views
        initViews();
    }

    private void initViews(){
        graph = findViewById(R.id.chart_id);

        valueTv = findViewById(R.id.first_value_id);
        dateTv = findViewById(R.id.first_date_id);
        timeTv = findViewById(R.id.first_time_id);

        dbHelper = new StatsDataDB(this);
        dbHelper.getReadableDatabase();
        // database = dbHelper.getReadableDatabase();
        onCreateGraph();
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

    // Graph
    private void onDraw(List<DistcovidModelObject> modelObjects){

        // add the value to draw the origin axis
        entries.add(new Entry(0.0f, 0.0f));
        xaxis.add("0");

        // check if the list not null to avoid app crash
        if(modelObjects != null && modelObjects.size() > 0) {
            for (int i = 0; i < modelObjects.size(); i++) {
                entries.add(new Entry((float) (i + 1), Double.valueOf(modelObjects.get(i).getDistance()).floatValue()));
                xaxis.add(modelObjects.get(i).getFormattedDate());
            }
        }
        else{
            String TAG = "Stats";
            Log.i(TAG, "--- onDraw object is null STATS ----");
        }
            lineDataSet = new LineDataSet(entries, "Closest Distance (in meter)"); // Legend
            onConfigLineDataSet(lineDataSet);

            iLineDataSets.add(lineDataSet);
            LineData lineData = new LineData(iLineDataSets);
            graph.setData(lineData);

            // others config for graph
            graph.setTouchEnabled(true);
            graph.setPinchZoom(true);
            // then modified viewport
            graph.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
            graph.getAxisRight().setEnabled(false); // don't show right YAxix
            graph.getXAxis().setDrawAxisLine(false);
            graph.getXAxis().setDrawGridLines(false);
            graph.getXAxis().setCenterAxisLabels(false);

            graph.getXAxis().setTextColor(Color.BLACK);
            graph.getXAxis().setTextSize(11f);
            graph.setDrawBorders(false);

            graph.getDescription().setText("Daily statistics of closest devices scanned");

            setGraphViewPort();
            graph.animateXY(1200, 2200);
    }

    private List<DistcovidModelObject> onRead(){

        List<DistcovidModelObject> warningList = manager.groupDailyDistance(dbHelper.getWarnings());

        if (warningList != null){
            DistcovidModelObject w = manager.getClosestDistance(warningList);
            double currentClosestDist = w.getDistance();

            String distance = currentClosestDist +" meter";
            valueTv.setText(distance);
            dateTv.setText(w.getFormattedDate());
            timeTv.setText(w.getFormattedTime());

        }else{
            warningList = new ArrayList<>();
            final String distance = getResources().getString(R.string.dist_init_val);
            final String date = getResources().getString(R.string.date_init_value);
            final String time = getResources().getString(R.string.time_init_value);

            valueTv.setText(distance);
            dateTv.setText(date);
            timeTv.setText(time);
        }
        return warningList;
    }

    private void onCreateGraph(){
        reset();
        onDraw(onRead());
    }

    private void onConfigLineDataSet(LineDataSet set){

        set.setDrawIcons(false);
        set.enableDashedLine(10f, 8f, 1f);
        set.enableDashedHighlightLine(10f, 8f, 1f);
        set.setColor(Color.DKGRAY);
        set.setCircleColor(Color.DKGRAY);
        set.setLineWidth(1f);
        set.setCircleRadius(4f);
        set.setValueTextSize(10f);
        set.setDrawFilled(true);
        set.setFormLineWidth(1f);
        set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 8f}, 1f));
        set.setFormSize(10f);

        if (Utils.getSDKInt() >= 21){
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.graph_background);
            lineDataSet.setFillDrawable(drawable);
        }
        else {
            lineDataSet.setFillColor(Color.DKGRAY);
        }
    }

    private void reset(){
        entries = null;
        lineDataSet = null;
        xaxis = null;
        iLineDataSets = null;

        entries = new ArrayList<>();
        xaxis = new ArrayList<>();
        iLineDataSets = new ArrayList<>();
    }

    private void setGraphViewPort(){

        graph.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xaxis));

        graph.getXAxis().setGranularity(1f);
        graph.getXAxis().setGranularityEnabled(true);

        graph.getXAxis().setAvoidFirstLastClipping(true); // to avoid that the last value always appear clipped
        graph.setVisibleXRangeMaximum(2);
    }
}
