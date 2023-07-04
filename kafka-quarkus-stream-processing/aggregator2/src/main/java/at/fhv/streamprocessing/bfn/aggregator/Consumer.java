package at.fhv.streamprocessing.bfn.aggregator;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Consumer {

    @Incoming("max-average-temp-over-5-days")
    public void consume(Record<String,Double> record) {
        System.out.println("Max-Average-over-5-days " + record.value());
    }


    
}
