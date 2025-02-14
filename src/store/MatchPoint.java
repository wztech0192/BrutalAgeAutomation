package store;

import org.opencv.core.Mat;

public class MatchPoint {

    public int x;
    public int y;
    public double threshold;
    public Mat mat;

    public MatchPoint(int x, int y){
        this.x= x;
        this.y=y;
    }
    public MatchPoint(int x, int y, double treshold){
        this(x, y);
        this.threshold = treshold;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setMat(Mat mat) {
        this.mat = mat;
    }

    public Mat getMat() {
        return mat;
    }
}
