package cm.rulan.distcovid.model;

import android.annotation.SuppressLint;
import android.util.Log;
import cm.rulan.distcovid.model.comparators.ModelDistanceComparator;

import java.text.SimpleDateFormat;
import java.util.*;

/*
* @author: L. Sangare and T. Ruphus
* */
public class DistcovidModelManager {

    private final String TAG = "Manager";

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat sdf_months = new SimpleDateFormat("yyyy-MM"); // format to plot values monthly
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat sdf_days = new SimpleDateFormat("yyyy-MM-dd"); // format to plot values daily
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat sdf_time = new SimpleDateFormat("hh:mm:ss");
    private Date date = new Date();

    public DistcovidModelManager() {}

    //get object grouped by day
    public List<DistcovidModelObject> groupDailyDistance(List<DistcovidModelObject> warnings){
        if(warnings.size() > 0){
            for (DistcovidModelObject warning : warnings) {
                date.setTime(warning.getDatetime());
                warning.setFormattedDate(sdf_days.format(date));
                warning.setFormattedTime(sdf_time.format(date));
            }
            Collections.sort(warnings);
            for (DistcovidModelObject p: warnings) {
                Log.i(TAG, "ID: " +p.get_id() +"   Dist:" + p.getDistance() + "Date: "+ p.getFormattedDate()+ "   Time: "+p.getFormattedTime());
            }
            return warnings;
        }
        return null;
    }

    // get the (Object) minimal distance
    public DistcovidModelObject getClosestDistance(List<DistcovidModelObject> list){

        if (list.size() > 0){
            Collections.sort(list, new ModelDistanceComparator());
            DistcovidModelObject war = list.get(0);
            war.setFormattedTime(sdf_time.format(war.getDatetime()));
            war.setFormattedDate(sdf_days.format(war.getDatetime()));

            return war;
        }
        return null;
    }
}
