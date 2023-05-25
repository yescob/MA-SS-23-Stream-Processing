package at.fhv.streamprocessing.bfn.aggregator.model;

public class SumAggregation {

    public String year;
    public Integer sum;

    public SumAggregation updateFrom(Integer measurement) {
        sum += measurement;

        return this;
    }
    
}
