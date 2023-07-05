package at.fhv.streamprocessing.bfn.aggregator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import at.fhv.streamprocessing.bfn.aggregator.model.NOAARecord;

public class EventTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        
        final NOAARecord event = (NOAARecord) record.value();
        final LocalDateTime localDateTime = LocalDateTime.of(Integer.parseInt(event.getYear()), 
            Integer.parseInt(event.getMonth()),Integer.parseInt(event.getDay()), 0,0);
        final ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        final long timestamp = zdt.toInstant().toEpochMilli();
        return timestamp;
        
    }
}
