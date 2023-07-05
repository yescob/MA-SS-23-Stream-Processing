package at.fhv.streamprocessing.bfn.aggregator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import at.fhv.streamprocessing.bfn.aggregator.model.NOAARecord;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

@ApplicationScoped
public class TemperatureTopologyProducer {

    public static final String WEATHER_STATIONS_STORE = "weather-stations-store";

    private static final String TEMPERATURE_VALUES_TOPIC = "enriched-records";
    private static final String TEMPERATURES_AGGREGATED_TOPIC = "temperatures-aggregated";
    private static final String TEMPERATURES_MAX_TOPIC = "temperatures-max";
    private static final String DAYS_OVER_THIRTY = "days-over-thirty";


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


        KStream<String, NOAARecord> stream2 = builder.stream(TEMPERATURE_VALUES_TOPIC, Consumed.with(Serdes.String(), recordSerde));

        stream2.filter((key, value) -> value.getTemperature().intValue() != NOAARecord.MISSING_VALUE.intValue()
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


        KStream<String, NOAARecord> stream3 = builder.stream(TEMPERATURE_VALUES_TOPIC, Consumed.with(Serdes.String(), recordSerde));

        ObjectMapperSerde<OverThirtyCount> aggregationSerde = new ObjectMapperSerde<>(OverThirtyCount.class);
        ObjectMapperSerde<NOAARecord> aggregationSerdeNOAA = new ObjectMapperSerde<>(NOAARecord.class);


        stream3.filter((key, value) -> value.getTemperature().intValue() != NOAARecord.MISSING_VALUE.intValue()
                && String.valueOf(value.getQualityRecord()).matches(NOAARecord.ALLOWED_QUALITY_NUMBERS_REGEX))
        .map((key, value) -> KeyValue.pair(1, value))
        .groupByKey(Grouped.<Integer, NOAARecord>as(null)
            .withKeySerde(Serdes.Integer())
            .withValueSerde(aggregationSerdeNOAA))
        .aggregate(
            OverThirtyCount::new,
            (key, value, aggregation) -> aggregation.updateFrom(value),
               Materialized.<Integer, OverThirtyCount, KeyValueStore<Bytes, byte[]>>as("days-over-thirty-store")
                    .withKeySerde(Serdes.Integer())
                    .withValueSerde(aggregationSerde)
        )
        .toStream()
        .to(DAYS_OVER_THIRTY, Produced.with(Serdes.Integer(), aggregationSerde));




        KStream<String, NOAARecord> stream4 = builder.stream(TEMPERATURE_VALUES_TOPIC, Consumed.with(Serdes.String(), recordSerde));

        ObjectMapperSerde<NOAARecord> aggregationSerdeNOAA2 = new ObjectMapperSerde<>(NOAARecord.class);
        ObjectMapperSerde<MedianPercentile> medianPercentileSerde = new ObjectMapperSerde<>(MedianPercentile.class);
        // Calculate median and percentiles
        stream4.filter((key, value) -> value.getTemperature().intValue() != NOAARecord.MISSING_VALUE.intValue()
                && String.valueOf(value.getQualityRecord()).matches(NOAARecord.ALLOWED_QUALITY_NUMBERS_REGEX))
            .map((key, value) -> KeyValue.pair(key, value))
            .groupByKey(Grouped.<String, NOAARecord>as(null)
                .withKeySerde(Serdes.String())
                .withValueSerde(aggregationSerdeNOAA2))
            .aggregate(
               MedianPercentile::new,
                (key, value, aggregation) -> aggregation.updateList(value),
                Materialized.<String, MedianPercentile, KeyValueStore<Bytes, byte[]>>as("temperature-values-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(medianPercentileSerde) // Use custom Serializer and Deserializer for List<Integer>
            )
            .toStream()
            .to("summary-statistics", Produced.with(Serdes.String(), medianPercentileSerde)); // Use String Serde for summary statistics
        
            return builder.build();
    }

}
