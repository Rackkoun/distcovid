package cm.rulan.distcovid.model.comparators;

import cm.rulan.distcovid.model.DistcovidModelObject;

public class ModelDistanceComparator implements java.util.Comparator<DistcovidModelObject>{
    @Override
    public int compare(DistcovidModelObject o1, DistcovidModelObject o2) {
        return Double.compare(o1.getDistance(), o2.getDistance());
    }
}
