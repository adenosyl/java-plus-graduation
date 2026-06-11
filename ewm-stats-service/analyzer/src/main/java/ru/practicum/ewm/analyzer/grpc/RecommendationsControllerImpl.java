package ru.practicum.ewm.analyzer.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.recommendations.*;
import ru.practicum.ewm.analyzer.service.RecommendationService;

@GrpcService
@RequiredArgsConstructor
public class RecommendationsControllerImpl
        extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService service;

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {

        service.getSimilarEvents(
                        request.getEventId(),
                        request.getUserId()
                )
                .stream()
                .limit(request.getMaxResults())
                .forEach(similarity -> {

                    RecommendedEventProto response =
                            RecommendedEventProto.newBuilder()
                                    .setEventId(similarity.getEventB())
                                    .setScore(similarity.getScore())
                                    .build();

                    responseObserver.onNext(response);
                });

        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {

        for (Long eventId : request.getEventIdList()) {

            double count =
                    service.getInteractionsCount(
                            eventId
                    );

            RecommendedEventProto response =
                    RecommendedEventProto.newBuilder()
                            .setEventId(eventId)
                            .setScore(count)
                            .build();

            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {

        service.getRecommendationsForUser(
                        request.getUserId()
                ).stream()
                .limit(request.getMaxResults())
                .forEach(similarity -> {

                    RecommendedEventProto response =
                            RecommendedEventProto.newBuilder()
                                    .setEventId(
                                            similarity.getEventB()
                                    )
                                    .setScore(
                                            similarity.getScore()
                                    )
                                    .build();

                    responseObserver.onNext(response);
                });

        responseObserver.onCompleted();
    }
}