package ru.practicum.ewm.client;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectorClient {
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void collectUserAction(Long userId, Long eventId, ActionTypeProto action) {
        var now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        UserActionProto userActionProto = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(action)
                .setTimestamp(timestamp)
                .build();
        try {
            client.collectUserAction(userActionProto);
        } catch (Exception e) {
            log.warn("Ошибка при вызове collectUserAction", e);
        }
    }
}
