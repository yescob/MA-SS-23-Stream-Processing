package at.fhv.streamprocessing.bfn.aggregator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.time.Duration;

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
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;

import at.fhv.streamprocessing.bfn.aggregator.model.NOAARecord;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

@ApplicationScoped
public class TemperatureTopologyProducer {

    private static final String TEMPERATURE_VALUES_TOPIC = "enriched-records";

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        ObjectMapperSerde<NOAARecord> recordSerde = new ObjectMapperSerde<>(NOAARecord.class);

        KStream<String, NOAARecord> stream5 = builder.stream(TEMPERATURE_VALUES_TOPIC, Consumed.with(Serdes.String(), recordSerde));

        // Max average temperature over 5 days
        Duration windowSize = Duration.ofDays(5);
        Duration advanzeSize = Duration.ofDays(1);
        TimeWindows hoppingWindow = TimeWindows.ofSizeWithNoGrace(windowSize).advanceBy(advanzeSize);
        ObjectMapperSerde<AverageTemp> averageTempSerdes = new ObjectMapperSerde<>(AverageTemp.class);

        stream5.filter((key, value) -> value.getTemperature().intValue() != NOAARecord.MISSING_VALUE.intValue()
                && String.valueOf(value.getQualityRecord()).matches(NOAARecord.ALLOWED_QUALITY_NUMBERS_REGEX))
            .map((key, value) -> KeyValue.pair(key, value))
            .groupByKey(Grouped.with(Serdes.String(), recordSerde))
            .windowedBy(hoppingWindow)
            .aggregate(
                AverageTemp::new,
                (key, value, aggregate) -> aggregate.update(value),
                Materialized.<String, AverageTemp, WindowStore<Bytes, byte[]>>as("windowed-average-temp")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(averageTempSerdes)
            )
            // only send date after window has closed (final result of window)
            .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
            .toStream()
            .map((key,value) -> KeyValue.pair(key.key(), value.averageTemperature))
            .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
            .aggregate(
                () -> (double) Integer.MIN_VALUE,
                (key, newValue, oldValue) -> Math.max(newValue, oldValue),
                Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as("max-5-day-average-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(Serdes.Double())
            )
            .toStream()
            .to("max-average-temp-over-5-days", Produced.with(Serdes.String(), Serdes.Double()));
        
            return builder.build();
    }

}
