package it.pagopa.interop.signalhub.pull.service.exception;



import it.pagopa.interop.signalhub.pull.service.rest.v1.dto.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

import static it.pagopa.interop.signalhub.pull.service.exception.ExceptionTypeEnum.GENERIC_ERROR;


@Component
public class PDNDExceptionHelper {



    public Problem handle(Throwable ex){
        ProblemBuilder builder = ProblemBuilder.builder();

        if (ex instanceof PDNDGenericException casted) {
            return builder.addExceptionType(casted.getExceptionType())
                    .addStatusCode(casted.getHttpStatus())
                    .setMessage(casted.getMessage())
                    .build();
        }

        return builder.addExceptionType(GENERIC_ERROR)
                .addStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

    }

    public String generateFallbackProblem() {
        String fallback = "{\n    \"status\": 500,\n    \"title\": \"Internal Server Error\",\n    \"detail\": \"Cannot output problem\",\n    \"traceId\": \"{traceid}\",\n    \"timestamp\": \"{timestamp}\",\n    \"errors\": [\n        {\n            \"code\": \"{errorcode}\",\n            \"element\": null,\n            \"detail\": null\n        }\n    ]\n}\n";
        fallback = fallback.replace("{traceid}", UUID.randomUUID().toString());
        fallback = fallback.replace("{timestamp}", Instant.now().toString());
        fallback = fallback.replace("{errorcode}", "GENERIC_ERROR");
        return fallback;
    }


}