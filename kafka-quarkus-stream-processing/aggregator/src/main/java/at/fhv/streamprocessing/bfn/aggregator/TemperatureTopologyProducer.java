package at.fhv.streamprocessing.bfn.aggregator;

import java.time.Instant;
import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;

import at.fhv.streamprocessing.bfn.aggregator.model.NOAARecord;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

@ApplicationScoped
public class TemperatureTopologyProducer {

    public static final String WEATHER_STATIONS_STORE = "weather-stations-store";

    private static final String TEMPERATURE_VALUES_TOPIC = "enriched-records";
    private static final String TEMPERATURES_AGGREGATED_TOPIC = "temperatures-aggregated";

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        ObjectMapperSerde<NOAARecord> recordSerde = new ObjectMapperSerde<>(NOAARecord.class);

        builder.stream(TEMPERATURE_VALUES_TOPIC, Consumed.with(Serdes.String(), recordSerde))
        .map((key,value)-> KeyValue.pair(key, value.getTemperature()))
        .groupByKey(Grouped.<String, Integer>as(null)
            .withKeySerde(Serdes.String())
            .withValueSerde(Serdes.Integer()))
        .reduce(Integer::sum)
        .toStream()
        .to(                                                          
            TEMPERATURES_AGGREGATED_TOPIC,
            Produced.with(Serdes.String(), Serdes.Integer())
        );

        return builder.build();
    }
}