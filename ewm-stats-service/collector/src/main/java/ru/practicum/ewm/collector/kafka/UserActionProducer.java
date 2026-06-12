package ru.practicum.ewm.collector.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor
public class UserActionProducer {

    private final KafkaTemplate<String, UserActionAvro> kafkaTemplate;

    public void send(UserActionAvro action) {

        kafkaTemplate.send(
                "stats.user-actions.v1",
                action
        ).whenComplete((result, ex) -> {

            if (ex != null) {
                System.err.println("SEND ERROR");
                ex.printStackTrace();
            } else {
                System.out.println(
                        "SENT TO KAFKA offset="
                                + result.getRecordMetadata().offset()
                );
            }
        });
    }
}