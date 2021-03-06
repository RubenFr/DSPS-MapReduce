package Round2;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Gram2 implements WritableComparable<Gram2> {

    private final Text w1;
    private final Text w2;
    private final IntWritable decade;

    public Gram2() {
        w1 = new Text("*");
        w2 = new Text("*");
        decade = new IntWritable(0);
    }

    public Gram2(String w1, String w2, IntWritable dec) {
        this.w1 = new Text(w1);
        this.w2 = new Text(w2);
        this.decade = new IntWritable((dec.get() / 10) * 10);
    }

    public Gram2(int dec) {
        this.w1 = new Text("*");
        this.w2 = new Text("*");
        this.decade = new IntWritable(dec);
    }

    @Override
    public int compareTo(Gram2 other) {
        // N is always "smaller"
        // Sort by decade and then w1 < w2 or w2 < w1
        // if this.w1 == other.w1, put <w1, *> before <w1,w2>
        int res;

        if ((res = this.getDecade() - other.getDecade()) != 0)      // Decade
            return res;

        else if ((res = this.getW1().compareTo(other.getW1())) != 0)            // W1
            return res;

        else
            return this.getW2().compareTo(other.getW2());                       // W2
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        w1.write(dataOutput);
        w2.write(dataOutput);
        decade.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        w1.readFields(dataInput);
        w2.readFields(dataInput);
        decade.readFields(dataInput);
    }

    public int getDecade() {
        return this.decade.get() / 10;
    }

    public String getW1() {
        return this.w1.toString();
    }

    public String getW2() {
        return this.w2.toString();
    }

    @Override
    public String toString() {
        return this.decade + " " + this.w1 + " " + this.w2;
    }

    public Text toText() {
        return new Text(this.toString());
    }
}
