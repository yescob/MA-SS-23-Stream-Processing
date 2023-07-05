package at.fhv.streamprocessing.bfn.aggregator;

import at.fhv.streamprocessing.bfn.aggregator.model.NOAARecord;

public class AverageTemp {

    public Integer totalTemperatur = 0;
    public Integer count = 0;
    public Double averageTemperature;

    public AverageTemp update(NOAARecord value) {
        totalTemperatur = totalTemperatur + value.getTemperature();
        count++;
        averageTemperature = (double) totalTemperatur / (double) count;
        return this;
    }

}
