package at.fhv.streamprocessing.bfn.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

/**
 * A bean producing random temperature data every second.
 * The values are written to a Kafka topic (temperature-values).
 * Another topic contains the name of weather stations (weather-stations).
 * The Kafka configuration is specified in the application configuration.
 */
@ApplicationScoped
public class Producer {

    private static final Logger LOG = Logger.getLogger(Producer.class);

    @Inject
    @Channel("temperature-records")
    Emitter<String> priceEmitter;
                                    
    public void sendToKafka(String record) {
        priceEmitter.send(record);
        LOG.info("Record send to Kafka: " + record);
    }
}
