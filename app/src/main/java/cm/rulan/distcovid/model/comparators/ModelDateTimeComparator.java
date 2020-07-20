package cm.rulan.distcovid.model.comparators;

import cm.rulan.distcovid.model.DistcovidModelObject;

public class ModelDateTimeComparator implements java.util.Comparator<DistcovidModelObject> {
    @Override
    public int compare(DistcovidModelObject o1, DistcovidModelObject o2) {
        return Long.compare(o1.getDatetime(), o2.getDatetime());
    }
}
