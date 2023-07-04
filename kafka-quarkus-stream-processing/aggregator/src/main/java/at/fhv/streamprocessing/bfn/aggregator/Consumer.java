package at.fhv.streamprocessing.bfn.aggregator;

import org.apache.kafka.streams.kstream.Window;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import at.fhv.streamprocessing.bfn.aggregator.model.NOAARecord;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Consumer {

    @Broadcast
    @Channel("enriched-records")
    Emitter<Record<String,NOAARecord>> nooaEmitter;

    @Incoming("temperature-records")
    public void consume(Record<Integer,String> record) {
    
        NOAARecord currentRecord = new NOAARecord(record.value());
    
        nooaEmitter.send(Record.of(currentRecord.getYear(),currentRecord));
    }

    @Incoming("temperatures-aggregated")
    public void consume2(Record<String,Integer> record) {
        System.out.println("Temperatures aggregated: " + record.value());
    }
    @Incoming("temperatures-max")
    public void consume3(Record<String,Integer> record) {
        System.out.println("Max Temperatures: " + record.value());
    }
    @Incoming("days-over-thirty")
    public void consume4(Record<Integer,OverThirtyCount> record) {
        System.out.println("Days where Temp is >30: " + record.value().count);
    }
    @Incoming("summary-statistics")
    public void consume5(Record<Integer,MedianPercentile> record) {
        System.out.println(record.value().summaryStatistics);
    }
    
}
