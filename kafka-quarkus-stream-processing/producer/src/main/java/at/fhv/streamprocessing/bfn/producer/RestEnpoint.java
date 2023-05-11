package at.fhv.streamprocessing.bfn.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@ApplicationScoped
@Path("/")
public class RestEnpoint {

    String filePath = "pub/data/noaa/2023/"; 
    String fileName = "010010-99999-2023.gz";

    @GET
    public void test(){
        FTPReader.getFile();
    }
    
}
