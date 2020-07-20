package cm.rulan.distcovid.model;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import cm.rulan.distcovid.model.comparators.ModelDistanceComparator;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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

    public DistcovidModelManager() {Log.i(TAG, "--- constructor Manager called ---");}

    //get push up object grouped by days
    public List<DistcovidModelObject> groupDailyDistance(List<DistcovidModelObject> warnings){
        //final Comparator<DistcovidModelObject> warningComparator =
        //        Comparator.comparingDouble(DistcovidModelObject::getDistance); //get the warning object that have the greatest value "max" (only one value per day)

        if(warnings.size() > 0){
            for (DistcovidModelObject warning : warnings) {
                date.setTime(warning.getDatetime());
                warning.setFormattedDate(sdf_days.format(date));
                warning.setFormattedTime(sdf_time.format(date));
            }

            /*List<DistcovidModelObject> newList = // Collect elements to a new list
                    warnings.stream()
                            .collect(Collectors.groupingBy(DistcovidModelObject::getFormattedDate,
                                    Collectors.maxBy(warningComparator)))
                            .values().stream().map(Optional::get)
                            .collect(Collectors.toList());*/

            Log.i(TAG, "Sorting started...");
            Collections.sort(warnings);
            Log.i(TAG, "Sorting ended");
            Log.i(TAG,"Size: "+ warnings.size());
            for (DistcovidModelObject p: warnings) {
                Log.i(TAG, "ID: " +p.get_id() +"   Dist:" + p.getDistance() + "Date: "+ p.getFormattedDate()+ "   Time: "+p.getFormattedTime());
            }
            return warnings;
        }
        return null;
    }

    public List<DistcovidModelObject> reorderedDailyDistance(List<DistcovidModelObject> list){
        Log.i(TAG, "--- reordered daily start ---");
        List<DistcovidModelObject> reorderedList  = groupDailyDistance(list);
        /*final Comparator<DistcovidModelObject> datetimeComparator = //get Min of Object
                Comparator.comparingLong(DistcovidModelObject::getDatetime);*/

        Log.i(TAG,"size reordered List: " + reorderedList.size());
       /* List<DistcovidModelObject> l = reorderedList.stream()
                .sorted(datetimeComparator)
                .collect(Collectors.toList());*/
        Collections.sort(reorderedList, new ModelDistanceComparator());

        for (DistcovidModelObject p: reorderedList){
            Log.d(TAG,"ID : " +p.get_id() + "   Dist: "+p.getDistance()
                    +"   Time: "+p.getFormattedTime()+"   Date: "+p.getFormattedDate());
        }

        Log.i(TAG, "--- reordered daily end ---");
        return reorderedList;
    }

    // get the object with the highest value
    public DistcovidModelObject getClosestDistance(List<DistcovidModelObject> list){
        Log.i(TAG, "--- get closet distance method start ---");
        if (list.size() > 0){
            Collections.sort(list, new ModelDistanceComparator());
            DistcovidModelObject war = list.get(0);
                    /*.stream()
                    .min(Comparator.comparing(DistcovidModelObject::getDistance))
                    .get();*/
            war.setFormattedTime(sdf_time.format(war.getDatetime()));
            war.setFormattedDate(sdf_days.format(war.getDatetime()));
            Log.d(TAG, "Highest use::\n" +
                    "ID: "+ war.get_id()+"\nmin Dist: " + war.getDistance()+
                    "\nDate: " + war.getFormattedDate()+
                    "\nTime: "+ war.getFormattedTime());

            return war;
        }
        return null;
    }
}
