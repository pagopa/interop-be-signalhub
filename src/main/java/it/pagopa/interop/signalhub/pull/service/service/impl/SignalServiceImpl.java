package it.pagopa.interop.signalhub.pull.service.service.impl;

import it.pagopa.interop.signalhub.pull.service.exception.ExceptionTypeEnum;
import it.pagopa.interop.signalhub.pull.service.exception.PocGenericException;
import it.pagopa.interop.signalhub.pull.service.mapper.SignalMapper;
import it.pagopa.interop.signalhub.pull.service.repository.ConsumerEserviceRepository;
import it.pagopa.interop.signalhub.pull.service.repository.SignalRepository;
import it.pagopa.interop.signalhub.pull.service.rest.v1.dto.Signal;
import it.pagopa.interop.signalhub.pull.service.service.SignalService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@AllArgsConstructor
public class SignalServiceImpl implements SignalService {
    private ConsumerEserviceRepository consumerEserviceRepository;
    private SignalRepository signalRepository;
    private SignalMapper signalMapper;

    @Override
    public Flux<Signal> pullSignal(String consumer, String eServiceId, Long indexSignal) {
        long finalIndexSignal = indexSignal;
        long start = finalIndexSignal+1;
        long end = finalIndexSignal+100;
        return consumerEserviceRepository.findByConsumerIdAndEServiceId(consumer, eServiceId)
                .switchIfEmpty(Mono.error(new PocGenericException(ExceptionTypeEnum.CORRESPONDENCE_NOT_FOUND, ExceptionTypeEnum.CORRESPONDENCE_NOT_FOUND.getMessage().concat(eServiceId), HttpStatus.FORBIDDEN)))
                .doOnNext(eService -> log.info("Faccio una ricerca tra {} - {}", start, end))
                .flatMapMany(eservice -> signalRepository.findSignal(eServiceId, start, end))
                .map(signalMapper::toDto);
    }


}
