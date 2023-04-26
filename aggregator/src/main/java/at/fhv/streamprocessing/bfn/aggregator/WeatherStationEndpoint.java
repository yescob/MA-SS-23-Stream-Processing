package at.fhv.streamprocessing.bfn.aggregator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@ApplicationScoped
@Path("/weather-stations")
public class WeatherStationEndpoint {

    @Inject
    InteractiveQueries interactiveQueries;

    @GET
    @Path("/data/{id}")
    public Response getWeatherStationData(int id) {
        GetWeatherStationDataResult result = interactiveQueries.getWeatherStationData(id);

        if (result.getResult().isPresent()) {  
            return Response.ok(result.getResult().get()).build();
        }
        else {
            return Response.status(Status.NOT_FOUND.getStatusCode(),
                    "No data found for weather station " + id).build();
        }
    }
}