package ru.practicum.stats.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.recommendations.*;

import java.util.*;

@Component
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub stub;

    public List<RecommendedEventProto> getRecommendations(
            long userId,
            int maxResults
    ) {

        UserPredictionsRequestProto request =
                UserPredictionsRequestProto.newBuilder()
                        .setUserId(userId)
                        .setMaxResults(maxResults)
                        .build();

        List<RecommendedEventProto> result =
                new ArrayList<>();

        Iterator<RecommendedEventProto> iterator =
                stub.getRecommendationsForUser(request);

        iterator.forEachRemaining(result::add);

        return result;
    }

    public Map<Long, Double> getInteractionsCount(
            List<Long> eventIds
    ) {

        InteractionsCountRequestProto request =
                InteractionsCountRequestProto.newBuilder()
                        .addAllEventId(eventIds)
                        .build();

        Iterator<RecommendedEventProto> iterator =
                stub.getInteractionsCount(request);

        Map<Long, Double> result =
                new HashMap<>();

        iterator.forEachRemaining(response ->
                result.put(
                        response.getEventId(),
                        response.getScore()
                )
        );

        return result;
    }
}