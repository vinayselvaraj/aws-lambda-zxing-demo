package hpaip;


import java.util.List;
import java.util.Map;

public class GSTQRCode {
    private int page;
    private Map<String, String> decodedSigned;
    private Map<String, String> data;
    private List<Point> geometry;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Map<String, String> getDecodedSigned() {
        return decodedSigned;
    }

    public void setDecodedSigned(Map<String, String> decodedSigned) {
        this.decodedSigned = decodedSigned;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
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
