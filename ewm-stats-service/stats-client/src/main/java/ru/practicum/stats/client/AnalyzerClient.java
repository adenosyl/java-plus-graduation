package ru.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.recommendations.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
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
}