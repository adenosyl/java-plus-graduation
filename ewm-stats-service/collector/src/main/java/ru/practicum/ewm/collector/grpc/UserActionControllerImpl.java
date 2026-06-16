package ru.practicum.ewm.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.collector.kafka.UserActionProducer;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.collector.UserActionProto;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@GrpcService
@RequiredArgsConstructor
public class UserActionControllerImpl
        extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionProducer producer;

    @Override
    public void collectUserAction(
            UserActionProto request,
            StreamObserver<Empty> responseObserver
    ) {

        UserActionAvro action = UserActionAvro.newBuilder()
                .setUserId(request.getUserId())
                .setEventId(request.getEventId())
                .setActionType(request.getActionType().name())
                .setTimestamp(request.getTimestamp().getSeconds())
                .build();

        System.out.println("Created Avro object: " + action);

        producer.send(action);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}