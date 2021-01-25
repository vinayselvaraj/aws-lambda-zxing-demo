package zxingdemo;

public class QRCode {
    private int page;
    private String data;
    private Point[] geometry;

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

    public Point[] getGeometry() {
        return geometry;
    }

    public void setGeometry(Point[] geometry) {
        this.geometry = geometry;
    }
}
