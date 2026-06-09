package ru.practicum.ewm.collector;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.practicum.ewm.stats.proto.collector.ActionTypeProto;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.collector.UserActionProto;

public class GrpcTestClient {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        UserActionControllerGrpc.UserActionControllerBlockingStub stub =
                UserActionControllerGrpc.newBlockingStub(channel);

        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(1)
                .setEventId(100)
                .setActionType(ActionTypeProto.ACTION_VIEW)
                .setTimestamp(
                        Timestamp.newBuilder()
                                .setSeconds(System.currentTimeMillis() / 1000)
                                .build()
                )
                .build();

        Empty response = stub.collectUserAction(request);

        System.out.println("Response received: " + response);

        channel.shutdown();
    }
}