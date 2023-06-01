package at.fhv.streamprocessing.bfn.aggregator;

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
        System.out.println(record.key() + ": " +record.value());
    }


    
}
