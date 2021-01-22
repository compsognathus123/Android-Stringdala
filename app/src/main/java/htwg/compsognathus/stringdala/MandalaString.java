package htwg.compsognathus.stringdala;

public class MandalaString
{
    private int index_start;
    private int index_end;

    private double length;

    private double xStart;
    private double xEnd;

    private double yStart;
    private double yEnd;

    private boolean sorted;

    public MandalaString(int index, int modulus, double times)
    {
        this.index_start = index;
        this.index_end = (int)(times * index) % modulus;

        this.xStart = -Math.cos(index_start * (2 * Math.PI/modulus));
        this.xEnd = -Math.cos(index_end * (2 * Math.PI/modulus));

        this.yStart = Math.sin(index_start * (2 * Math.PI/modulus));
        this.yEnd = Math.sin(index_end * (2 * Math.PI/modulus));
        this.length = Math.sqrt(Math.pow(xStart-xEnd, 2) + Math.pow(yStart-yEnd, 2)) / 2;
    }

    public double getxEnd() {
        return xEnd;
    }

    public double getxStart() {
        return xStart;
    }

    public double getyStart() {
        return yStart;
    }

    public double getyEnd() {
        return yEnd;
    }

    public int getIndexEnd() {
        return index_end;
    }

    public int getIndexStart() {
        return index_start;
    }

    public double getLength() {
        return length;
    }

    public boolean isSorted()
    {
        return sorted;
    }

    public void setSorted(boolean sorted)
    {
        this.sorted = sorted;
    }
}
