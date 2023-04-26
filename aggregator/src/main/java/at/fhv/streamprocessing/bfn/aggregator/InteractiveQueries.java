package at.fhv.streamprocessing.bfn.aggregator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import at.fhv.streamprocessing.bfn.aggregator.model.Aggregation;
import at.fhv.streamprocessing.bfn.aggregator.model.WeatherStationData;

@ApplicationScoped
public class InteractiveQueries {


    @Inject
    KafkaStreams streams;

    public GetWeatherStationDataResult getWeatherStationData(int id) {
        Aggregation result = getWeatherStationStore().get(id);

        if (result != null) {
            return GetWeatherStationDataResult.found(WeatherStationData.from(result)); 
        }
        else {
            return GetWeatherStationDataResult.notFound();                             
        }
    }

    private ReadOnlyKeyValueStore<Integer, Aggregation> getWeatherStationStore() {
        while (true) {
            try {
                return streams.store(StoreQueryParameters.fromNameAndType(TopologyProducer.WEATHER_STATIONS_STORE, QueryableStoreTypes.keyValueStore()));
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }
}