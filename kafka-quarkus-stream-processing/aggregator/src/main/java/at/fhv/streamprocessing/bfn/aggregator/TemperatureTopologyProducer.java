package at.fhv.streamprocessing.bfn.aggregator;

import java.time.Instant;
import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;

import at.fhv.streamprocessing.bfn.aggregator.model.Aggregation;
import at.fhv.streamprocessing.bfn.aggregator.model.SumAggregation;
import at.fhv.streamprocessing.bfn.aggregator.model.TemperatureMeasurement;
import at.fhv.streamprocessing.bfn.aggregator.model.WeatherStation;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

@ApplicationScoped
public class TemperatureTopologyProducer {

    public static final String WEATHER_STATIONS_STORE = "weather-stations-store";

    private static final String TEMPERATURE_VALUES_TOPIC = "temperature-year";
    private static final String TEMPERATURES_AGGREGATED_TOPIC = "temperatures-aggregated";


    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(TEMPERATURE_VALUES_TOPIC, Consumed.with(Serdes.String(), Serdes.Integer()))
        .to(                                                          
            TEMPERATURES_AGGREGATED_TOPIC,
            Produced.with(Serdes.String(), Serdes.Integer())
        );

        return builder.build();
    }
}