package ru.practicum.stats.client;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.collector.ActionTypeProto;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.collector.UserActionProto;

@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub stub;

    public void collect(
            Long userId,
            Long eventId,
            ActionTypeProto actionType
    ) {

        UserActionProto request =
                UserActionProto.newBuilder()
                        .setUserId(userId)
                        .setEventId(eventId)
                        .setActionType(actionType)
                        .setTimestamp(
                                Timestamp.newBuilder()
                                        .setSeconds(
                                                System.currentTimeMillis() / 1000
                                        )
                                        .build()
                        )
                        .build();

        stub.collectUserAction(request);
    }
}