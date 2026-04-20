package ru.practicum.ewm.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.service.RecommendationsService;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationsService recommendationsService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Получено сообщение getRecommendationsForUser, request = {}", request);
        try {
            // Получаем поток рекомендаций от сервиса
            recommendationsService.getRecommendationsForUser(request)
                    .forEach(responseObserver::onNext);
            // Завершаем поток
            responseObserver.onCompleted();
            log.info("Рекомендации успешно отправлены для пользователя: userId={}", request.getUserId());
        } catch (Exception e) {
            log.error("Ошибка при получении рекомендаций для пользователя {}", request.getUserId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Ошибка генерации рекомендаций: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Получено сообщение getSimilarEvents, request = {}", request);
        try {
            // Получаем поток мероприятий от сервиса
            recommendationsService.getSimilarEvents(request)
                    .forEach(responseObserver::onNext);
            // Завершаем поток
            responseObserver.onCompleted();
            log.info("Отправлен список мероприятий, похожих на {} для пользователя: userId={}",
                    request.getEventId(), request.getUserId());
        } catch (Exception e) {
            log.error("Ошибка при получении мероприятий, похожих на {}, для пользователя {}",
                    request.getEventId(), request.getUserId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Ошибка при получении мероприятий: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Получено сообщение getInteractionsCount, request = {}", request);

        try {
            // Получаем поток с суммой максимальных весов от сервиса
            recommendationsService.getInteractionsCount(request)
                    .forEach(responseObserver::onNext);
            // Завершаем поток
            responseObserver.onCompleted();
            log.info("Суммы максимальных весов успешно отправлены для списка мероприятий");
        } catch (Exception e) {
            log.error("Ошибка при получении максимальных весов для списка мероприятий", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Ошибка при получении максимальных весов: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }

    }

}
