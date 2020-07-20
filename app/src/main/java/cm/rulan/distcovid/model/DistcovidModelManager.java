package cm.rulan.distcovid.model;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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

    public DistcovidModelManager() {}

    //get push up object grouped by days
    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<DistcovidModelObject> groupDailyDistance(List<DistcovidModelObject> warnings){
        final Comparator<DistcovidModelObject> warningComparator =
                Comparator.comparingDouble(DistcovidModelObject::getDistance); //get the warning object that have the greatest value "max" (only one value per day)

        if(warnings.size() > 0){
            for (DistcovidModelObject warning : warnings) {
                date.setTime(warning.getDatetime());
                warning.setFormattedDate(sdf_days.format(date));
                warning.setFormattedTime(sdf_time.format(date));
            }

            List<DistcovidModelObject> newList = // Collect elements to a new list
                    warnings.stream()
                            .collect(Collectors.groupingBy(DistcovidModelObject::getFormattedDate,
                                    Collectors.maxBy(warningComparator)))
                            .values().stream().map(Optional::get)
                            .collect(Collectors.toList());

            Log.d(getClass().getSimpleName(),"Size: "+ newList.size());
            for (DistcovidModelObject p: newList) {
                Log.i(TAG, "ID: " +p.get_id() +"   Dist:" + p.getDistance() + "Date: "+ p.getFormattedDate()+ "   Time: "+p.getFormattedTime());
            }
            return newList;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<DistcovidModelObject> reorderedDailyDistance(List<DistcovidModelObject> list){

        List<DistcovidModelObject> reorderedList  = groupDailyDistance(list);
        final Comparator<DistcovidModelObject> datetimeComparator = //get Min of Object
                Comparator.comparingLong(DistcovidModelObject::getDatetime);

        Log.d(getClass().getSimpleName(),"size reordered List: " + reorderedList.size());
        List<DistcovidModelObject> l = reorderedList.stream()
                .sorted(datetimeComparator)
                .collect(Collectors.toList());
        for (DistcovidModelObject p: l){
            Log.d(getClass().getSimpleName(),"ID : " +p.get_id() + "   Dist: "+p.getDistance()
                    +"   Time: "+p.getFormattedTime()+"   Date: "+p.getFormattedDate());
        }
        return l;
    }

    // get the object with the highest value
    @RequiresApi(api = Build.VERSION_CODES.N)
    public DistcovidModelObject getClosestDistance(List<DistcovidModelObject> list){
        if (list.size() > 0){
            DistcovidModelObject war = list
                    .stream()
                    .min(Comparator.comparing(DistcovidModelObject::getDistance))
                    .get();
            war.setFormattedTime(sdf_time.format(war.getDatetime()));
            war.setFormattedDate(sdf_days.format(war.getDatetime()));
            Log.d(getClass().getSimpleName(), "Highest use::\n" +
                    "ID: "+ war.get_id()+"\nmin Dist: " + war.getDistance()+
                    "\nDate: " + war.getFormattedDate()+
                    "\nTime: "+ war.getFormattedTime());

            return war;
        }
        return null;
    }
}
