package at.fhv.streamprocessing.bfn.aggregator;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import at.fhv.streamprocessing.bfn.aggregator.model.NOAARecord;

public class OverThirtyCount {
    
    public Set<LocalDate> alreadyCountedDates = new HashSet<LocalDate>();
    public int count;

    public OverThirtyCount updateFrom(NOAARecord record){

        if(record.getTemperature().intValue() >= 82){

            LocalDate currentDate = LocalDate.of(Integer.parseInt(record.getYear()), 
            Integer.parseInt(record.getMonth()),Integer.parseInt(record.getDay()));

            if(!alreadyCountedDates.contains(currentDate)){
                alreadyCountedDates.add(currentDate);
                count++;
            }
        }
        return this;
    }
}
