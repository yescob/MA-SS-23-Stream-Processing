package at.fhv.streamprocessing.bfn.aggregator;

import java.time.Instant;
import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Qualifier;

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
import org.apache.kafka.streams.kstream.KStream;
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
    private static final String TEMPERATURES_MAX_TOPIC = "temperatures-max";
    private static final String DAYS_OVER_THIRTY = "days-over-thirty";


    // @ApplicationScoped
    // public Topology buildTopology() {
    //     StreamsBuilder builder = new StreamsBuilder();

    //     ObjectMapperSerde<NOAARecord> recordSerde = new ObjectMapperSerde<>(NOAARecord.class);

    //     builder.stream(TEMPERATURE_VALUES_TOPIC, Consumed.with(Serdes.String(), recordSerde))
    //     .map((key,value)-> KeyValue.pair(key, value.getTemperature()))
    //     .groupByKey(Grouped.<String, Integer>as(null)
    //         .withKeySerde(Serdes.String())
    //         .withValueSerde(Serdes.Integer()))
    //     .reduce(Integer::sum)
    //     .toStream()
    //     .to(                                                          
    //         TEMPERATURES_AGGREGATED_TOPIC,
    //         Produced.with(Serdes.String(), Serdes.Integer())
    //     );

    //     return builder.build();
    // }

    // @Produces
    // public Topology getMax() {
    //     StreamsBuilder builder = new StreamsBuilder();

    //     ObjectMapperSerde<NOAARecord> recordSerde = new ObjectMapperSerde<>(NOAARecord.class);

    //     builder.stream(TEMPERATURE_VALUES_TOPIC, Consumed.with(Serdes.String(), recordSerde))
    //     .filter((key, value) -> value.getTemperature().intValue() != NOAARecord.MISSING_VALUE.intValue() && String.valueOf(value.getQualityRecord()).matches(NOAARecord.ALLOWED_QUALITY_NUMBERS_REGEX))
    //     .map((key,value)-> KeyValue.pair(key, value.getTemperature()))
    //     .groupByKey(Grouped.<String, Integer>as(null)
    //         .withKeySerde(Serdes.String())
    //         .withValueSerde(Serdes.Integer()))
    //     .aggregate(
    //         () -> Integer.MIN_VALUE,
    //         (key, newValue, oldValue) -> Math.max(newValue, oldValue),
    //         Materialized.<String, Integer, KeyValueStore<Bytes, byte[]>>as(WEATHER_STATIONS_STORE)
    //             .withKeySerde(Serdes.String())
    //             .withValueSerde(Serdes.Integer())
    //     )
    //     .toStream()
    //     .to(                                                          
    //         TEMPERATURES_MAX_TOPIC,
    //         Produced.with(Serdes.String(), Serdes.Integer())
    //     );

    //     return builder.build();
    // }



    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        ObjectMapperSerde<NOAARecord> recordSerde = new ObjectMapperSerde<>(NOAARecord.class);

        KStream<String, NOAARecord> stream = builder.stream(TEMPERATURE_VALUES_TOPIC, Consumed.with(Serdes.String(), recordSerde));

        stream.map((key, value) -> KeyValue.pair(key, value.getTemperature()))
            .groupByKey(Grouped.<String, Integer>as(null)
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.Integer()))
            .reduce(Integer::sum)
            .toStream()
            .to(TEMPERATURES_AGGREGATED_TOPIC, Produced.with(Serdes.String(), Serdes.Integer()));

        stream.filter((key, value) -> value.getTemperature().intValue() != NOAARecord.MISSING_VALUE.intValue()
                && String.valueOf(value.getQualityRecord()).matches(NOAARecord.ALLOWED_QUALITY_NUMBERS_REGEX))
            .map((key, value) -> KeyValue.pair(key, value.getTemperature()))
            .groupByKey(Grouped.<String, Integer>as(null)
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.Integer()))
            .aggregate(
                () -> Integer.MIN_VALUE,
                (key, newValue, oldValue) -> Math.max(newValue, oldValue),
                Materialized.<String, Integer, KeyValueStore<Bytes, byte[]>>as(WEATHER_STATIONS_STORE)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(Serdes.Integer())
            )
            .toStream()
            .to(TEMPERATURES_MAX_TOPIC, Produced.with(Serdes.String(), Serdes.Integer()));


        
        ObjectMapperSerde<OverThirtyCount> aggregationSerde = new ObjectMapperSerde<>(OverThirtyCount.class);
        ObjectMapperSerde<NOAARecord> aggregationSerdeNOAA = new ObjectMapperSerde<>(NOAARecord.class);


        stream.filter((key, value) -> value.getTemperature().intValue() != NOAARecord.MISSING_VALUE.intValue()
                && String.valueOf(value.getQualityRecord()).matches(NOAARecord.ALLOWED_QUALITY_NUMBERS_REGEX))
        .map((key, value) -> KeyValue.pair(1, value))
        .groupByKey(Grouped.<Integer, NOAARecord>as(null)
            .withKeySerde(Serdes.Integer())
            .withValueSerde(aggregationSerdeNOAA))
        .aggregate(
            OverThirtyCount::new,
            (key, value, aggregation) -> aggregation.updateFrom(value),
               Materialized.<Integer, OverThirtyCount, KeyValueStore<Bytes, byte[]>>as("test")
                    .withKeySerde(Serdes.Integer())
                    .withValueSerde(aggregationSerde)
        )
        .toStream()
        .to(DAYS_OVER_THIRTY, Produced.with(Serdes.Integer(), aggregationSerde));

        return builder.build();
    }

}