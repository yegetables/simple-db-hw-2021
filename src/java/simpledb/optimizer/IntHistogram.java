package simpledb.optimizer;

import simpledb.execution.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram implements Histogram<Integer> {

    private List<Long> lists;
    private int min, max, buckets;
    private double range, sum = 0;

    /**
     * Create a new IntHistogram.
     * <p>
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     * <p>
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     * <p>
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't
     * simply store every value that you see in a sorted list.
     *
     * @param buckets The number of buckets to split the input value into.
     * @param min     The minimum integer value that will ever be passed to this class for histogramming
     * @param max     The maximum integer value that will ever be passed to this class for histogramming
     */
    public IntHistogram(int buckets, int min, int max) {
        this.lists = new ArrayList<>(buckets);
        this.min = min;
        this.max = max;
        this.buckets = buckets;
        this.sum = 0;
        this.range = (max - min + 1) / (1.0 * buckets);
        for (int i = 0; i < buckets; i++) lists.add(0L);
    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     *
     * @param v Value to add to the histogram
     */
    public void addValue(Integer v) {
        Integer index = getIndex(v);
        if (index == null) return;
        if (index < 0 || index >= lists.size()) return;
        lists.set(index, lists.get(index) + 1);
        sum++;
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     * <p>
     * For example, if "op" is "GREATER_THAN" and "v" is 5,
     * return your estimate of the fraction of elements that are greater than 5.
     *
     * @param op Operator
     * @param v  Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, Integer v) {
        double cost = 0;
        Integer index = getIndex(v);
        switch (op)
        {
            case GREATER_THAN ->
            {
                if (index < 0) return 1;
                if (index >= lists.size()) return 0;
                for (int i = index + 1; i < lists.size(); i++)
                {
                    cost += (double) lists.get(i) / sum;
                }
                double b_f = (double) lists.get(index) / sum;
                double b_r = (min + (1 + index) * range - v) / range;
                cost += b_f * b_r;
            }
            case EQUALS ->
            {
                if (index < 0) return 0;
                if (index >= lists.size()) return 0;
                cost += (double) lists.get(index) / (range * 1.0 * sum);
            }
            case LIKE ->
            {
                if (index < 0) return 0;
                if (index >= lists.size()) return 0;
                cost += (double) lists.get(index) / sum;
            }
            case LESS_THAN ->
            {
                if (index < 0) return 0;
                if (index >= lists.size()) return 1;
                for (int i = 0; i < index; i++)
                {
                    cost += (double) lists.get(i) / sum;
                }

                double b_f = (double) lists.get(index) / sum;
                double b_r = (double) (v - (min + index * range)) / range;
                cost += b_f * b_r;
            }
            case GREATER_THAN_OR_EQ ->
            {
                cost += estimateSelectivity(Predicate.Op.GREATER_THAN, v);
                cost += estimateSelectivity(Predicate.Op.EQUALS, v);
            }
            case LESS_THAN_OR_EQ ->
            {
                cost += estimateSelectivity(Predicate.Op.LESS_THAN, v);
                cost += estimateSelectivity(Predicate.Op.EQUALS, v);
            }
            case NOT_EQUALS ->
            {
                cost = 1 - estimateSelectivity(Predicate.Op.EQUALS, v);
            }
        }
        return cost > 1.0 ? 1 : (cost < 0 ? 0 : cost);
    }

    /**
     * @return the average selectivity of this histogram.
     * <p>
     * This is not an indispensable method to implement the basic
     * join optimization. It may be needed if you want to
     * implement a more efficient optimization
     */
    public double avgSelectivity() {
        // some code goes here
        return 1.0;
    }

    /**
     * @return A string describing this histogram, for debugging purposes
     */
    @Override
    public String toString() {
        return "IntHistogram{" + "lists=" + lists + ", min=" + min + ", max=" + max + ", buckets=" + buckets + ", sum=" + sum + ", range=" + range + '}';
    }

    private Integer getIndex(int v) {
        if (v < min) return -1;
        if (v > max) return buckets;
        return (int) ((v - min) / range);
        //       v= min+n*range <=max
    }
}
