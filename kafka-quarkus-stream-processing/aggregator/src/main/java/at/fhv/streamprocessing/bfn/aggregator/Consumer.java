package at.fhv.streamprocessing.bfn.aggregator;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import at.fhv.streamprocessing.bfn.aggregator.model.SumAggregation;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class Consumer {

    private static final Integer START_POSITION_YEAR = 16;
    private static final Integer END_POSITION_YEAR = 19;
    private static final Integer START_POSITION_TEMPERATURE = 88;
    private static final Integer END_POSITION_TEMPERATURE = 92;
    private static final Integer POSITION_QUALITY_CODE = 93;
    private static final Integer MISSING_VALUE = 9999;
    private static final String ALLOWED_QUALITY_NUMBERS_REGEX = "[01459]";

    @Inject
    @Broadcast
    @Channel("temperature-year")
    Emitter<Record<String, Integer>> temperatureEmitter;

    @Incoming("temperature-records")
    public void consume(Record<Integer,String> record) {
    
        String year = record.value().substring(START_POSITION_YEAR-1,END_POSITION_YEAR); // year is key for reducer
        
        Integer temperature = Integer.parseInt(record.value().substring(START_POSITION_TEMPERATURE-1,END_POSITION_TEMPERATURE));
        String qualityRecord = record.value().substring(POSITION_QUALITY_CODE-1, POSITION_QUALITY_CODE);

        temperatureEmitter.send(Record.of(year,temperature));
    }

    @Incoming("temperatures-aggregated")
    public void consume2(Record<String,SumAggregation> record) {
        System.out.println(record.value());
    }

    @Incoming("temperature-year")
    public void consume3(Record<String,Integer> record) {
        System.out.println(record.value());
    }
    
}
