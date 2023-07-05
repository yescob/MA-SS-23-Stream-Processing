package at.fhv.streamprocessing.bfn.aggregator;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import at.fhv.streamprocessing.bfn.aggregator.model.NOAARecord;

public class MedianPercentile {
    
    //List does not work for values
    public String summaryStatistics = "";
    public Set<Integer> values = new HashSet<Integer>();


    public MedianPercentile updateList(NOAARecord record){

        values.add(record.getTemperature().intValue());
        List<Integer> valuesList = new LinkedList<>(values);


        Collections.sort(valuesList);
        
        // Calculate median
        int size = valuesList.size();
        double median = (size % 2 == 0)
                ? (valuesList.get(size / 2 - 1) + valuesList.get(size / 2)) / 2.0
                : valuesList.get(size / 2);
        
        // Calculate percentiles (e.g., 25th, 50th, and 75th percentiles)
        int percentile25 = valuesList.get((int) Math.ceil(size * 0.25) - 1);
        int percentile50 = valuesList.get((int) Math.ceil(size * 0.50) - 1);
        int percentile75 = valuesList.get((int) Math.ceil(size * 0.75) - 1);
        
        summaryStatistics = "Median: " + median +
            ", 25th Percentile: " + percentile25 +
            ", 50th Percentile: " + percentile50 +
            ", 75th Percentile: " + percentile75;

        return this;
    }
}
