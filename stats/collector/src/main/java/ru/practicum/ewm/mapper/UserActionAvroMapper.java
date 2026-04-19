package ru.practicum.ewm.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Component
@Slf4j
public class UserActionAvroMapper {
    public UserActionAvro toUserActionAvro(UserActionProto userActionProto) {
        log.info("toUserActionAvro: type={}, userId={}, eventId={}",
                userActionProto.getActionType(), userActionProto.getUserId(), userActionProto.getEventId());
        return UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setActionType(mapActionType(userActionProto.getActionType()))
                .setEventId(userActionProto.getEventId())
                .setTimestamp(Instant.ofEpochSecond(
                        userActionProto.getTimestamp().getSeconds(),
                        userActionProto.getTimestamp().getNanos()
                ))
                .build();
    }

    private ActionTypeAvro mapActionType(ActionTypeProto protoType) {
        return switch (protoType) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Неизвестный ActionType: " + protoType);
        };
    }
}
