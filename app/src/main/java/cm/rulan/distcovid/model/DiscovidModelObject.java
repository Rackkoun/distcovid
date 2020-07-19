package cm.rulan.distcovid.model;

public class DiscovidModelObject {
    private long _id;
    private double distance;
    private int during;
    private long datetime;

    // for formatting
    private String formattedDate;
    private String formattedTime;

    public DiscovidModelObject(double distance, int during, long datetime){
        this.distance = distance;
        this.during = during;
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

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getDuring() {
        return during;
    }

    public void setDuring(int during) {
        this.during = during;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
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
                ", during=" + during +
                ", datetime=" + datetime +
                ", formattedDate='" + formattedDate + '\'' +
                ", formattedTime='" + formattedTime + '\'' +
                '}';
    }
}
