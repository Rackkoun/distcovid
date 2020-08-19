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
    private final SimpleDateFormat sdfDays = new SimpleDateFormat("yyyy-MM-dd"); // format to plot values daily
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss");
    private Date date = new Date();

    public DistcovidModelManager() {}

    //get object grouped by day
    public List<DistcovidModelObject> groupDailyDistance(List<DistcovidModelObject> warnings){
        if(warnings.size() > 0){
            for (DistcovidModelObject warning : warnings) {
                date.setTime(warning.getDatetime());
                warning.setFormattedDate(sdfDays.format(date));
                warning.setFormattedTime(sdfTime.format(date));
            }
            Collections.sort(warnings);
            for (DistcovidModelObject p: warnings) {
                Log.i(TAG, "ID: " +p.getiD() +"   Dist:" + p.getDistance() + "Date: "+ p.getFormattedDate()+ "   Time: "+p.getFormattedTime());
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
            war.setFormattedTime(sdfTime.format(war.getDatetime()));
            war.setFormattedDate(sdfDays.format(war.getDatetime()));

            return war;
        }
        return null;
    }
}
