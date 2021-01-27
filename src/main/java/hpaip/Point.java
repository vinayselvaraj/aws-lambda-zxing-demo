package hpaip;

public class Point {
    float x;
    float y;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String toString() {
        return String.format("(x=%f, y=%f)", x, y);
    }
}
