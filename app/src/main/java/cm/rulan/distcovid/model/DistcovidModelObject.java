package cm.rulan.distcovid.model;

public class DistcovidModelObject implements java.lang.Comparable<DistcovidModelObject>{
    private long _id;
    private final double distance;
    private final long datetime;

    // for formatting
    private String formattedDate;
    private String formattedTime;

    public DistcovidModelObject(double distance, long datetime){
        this.distance = distance;
        this.datetime = datetime;
        _id = -1;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public double getDistance() {
        return distance;
    }

    public long getDatetime() {
        return datetime;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    @Override
    public String toString() {
        return "DiscovidModelObject{" +
                "_id=" + _id +
                ", distance=" + distance +
                ", datetime=" + datetime +
                ", formattedDate='" + formattedDate + '\'' +
                ", formattedTime='" + formattedTime + '\'' +
                '}';
    }

    @Override
    public int compareTo(DistcovidModelObject o) {
        if (getFormattedDate() == null || o.getFormattedDate() == null)
            return 0;
        return getFormattedDate().compareTo(o.getFormattedDate());
    }
}
