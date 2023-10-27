package it.pagopa.interop.signalhub.pull.service.repository.impl;

import it.pagopa.interop.signalhub.pull.service.entities.ConsumerEService;
import it.pagopa.interop.signalhub.pull.service.entities.EService;
import it.pagopa.interop.signalhub.pull.service.exception.PDNDGenericException;
import it.pagopa.interop.signalhub.pull.service.mapper.ConsumerEServiceMapper;
import it.pagopa.interop.signalhub.pull.service.mapper.EServiceMapper;
import it.pagopa.interop.signalhub.pull.service.repository.EServiceRepository;
import it.pagopa.interop.signalhub.pull.service.repository.cache.repository.ConsumerEServiceCacheRepository;
import it.pagopa.interop.signalhub.pull.service.repository.cache.repository.EServiceCacheRepository;
import it.pagopa.interop.signalhub.pull.service.utils.Const;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static it.pagopa.interop.signalhub.pull.service.exception.ExceptionTypeEnum.ESERVICE_STATUS_IS_NOT_ACTIVE;
import static org.springframework.data.relational.core.query.Criteria.where;


@Slf4j
@Repository
@AllArgsConstructor
public class EServiceRepositoryImpl implements EServiceRepository {

    private final R2dbcEntityTemplate template;
    private final EServiceCacheRepository cacheRepository;
    private final EServiceMapper mapper;

    @Override
    public Mono<EService> checkEServiceStatus(String eserviceId, String descriptorId) {

        return this.cacheRepository.findById(eserviceId , descriptorId)
                .doOnNext(cache -> log.info("[{}-{}] EService in cache", eserviceId))
                .flatMap(eServiceCache -> {
                    if(eServiceCache.getState().equals(Const.STATE_ACTIVE)) return Mono.just(eServiceCache);
                    return Mono.error(new PDNDGenericException(ESERVICE_STATUS_IS_NOT_ACTIVE, ESERVICE_STATUS_IS_NOT_ACTIVE.getMessage().concat(eserviceId)));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("[{}-{}] ConsumerEService no in cache",  eserviceId);
                    return Mono.empty();
                }))
                .map(mapper::toEntity)
                .switchIfEmpty(getFromDbAndSaveOnCache(eserviceId , descriptorId));

    }

    private Mono<EService> getFromDbAndSaveOnCache(String eserviceId, String descriptorId) {
        Query equals = Query.query(
                where(EService.COLUMN_ESERVICE_ID).is(eserviceId)
                        .and(where(EService.COLUMN_DESCRIPTOR_ID).is(descriptorId))
                        .and(where(EService.COLUMN_STATE).not(Const.STATE_ACTIVE))
        );
        return this.template.selectOne(equals, EService.class)
                .switchIfEmpty(Mono.defer(()-> {
                    log.info("[{}-{}] EService not founded into DB",  eserviceId);
                    return Mono.empty();
                }))
                .doOnNext(entity ->
                        log.info("[{}-{}] EService founded into DB",  eserviceId)
                )
                .flatMap(entity -> this.cacheRepository.save(mapper.toCache(entity)))
                .map(mapper::toEntity);
    }

}
