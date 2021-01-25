package zxingdemo;


import java.util.List;

public class QRCode {
    private int page;
    private String data;
    private List<Point> geometry;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<Point> getGeometry() {
        return geometry;
    }

    public void setGeometry(List<Point> geometry) {
        this.geometry = geometry;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(String.format("[page=%d,data=%s,geometry=%s]", page, data, geometry));
        return buff.toString();
    }
}
