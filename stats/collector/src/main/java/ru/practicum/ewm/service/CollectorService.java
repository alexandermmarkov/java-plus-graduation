package ru.practicum.ewm.service;

import ru.practicum.ewm.stats.proto.UserActionProto;

public interface CollectorService {
    void sendUserAction(UserActionProto userAction);
}
