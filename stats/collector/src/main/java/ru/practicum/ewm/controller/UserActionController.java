package ru.practicum.ewm.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.service.CollectorService;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final CollectorService collectorService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info("Получено действие collectUserAction: type={}, userId={}, eventId={}",
                request.getActionType(), request.getUserId(), request.getEventId());
        try {
            collectorService.sendUserAction(request);
            log.info("User action успешно обработан: {}", request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception exception) {
            log.error("Ошибка обработки user action: {}", request, exception);
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(exception)));
        }
    }

}
