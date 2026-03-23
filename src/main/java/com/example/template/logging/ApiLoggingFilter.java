package com.example.template.logging;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ApiLoggingFilter implements WebFilter {

    private static final Logger API_LOG = LoggerFactory.getLogger("API_LOG");
    private final LogSanitizer sanitizer;

    public ApiLoggingFilter(LogSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var started = Instant.now();
        return DataBufferUtils.join(exchange.getRequest().getBody()).defaultIfEmpty(exchange.getResponse()
                        .bufferFactory().wrap(new byte[0]))
                .flatMap(buffer -> {
                    var requestBytes = new byte[buffer.readableByteCount()];
                    buffer.read(requestBytes);
                    DataBufferUtils.release(buffer);
                    var requestPayload = sanitizer.sanitize(new String(requestBytes, StandardCharsets.UTF_8));
                    var decoratedRequest = decorateRequest(exchange, requestBytes);
                    var responseBuffer = new StringBuilder();
                    var decoratedResponse = decorateResponse(exchange, responseBuffer);
                    var decoratedExchange = exchange.mutate().request(decoratedRequest).response(decoratedResponse).build();
                    return chain.filter(decoratedExchange)
                            .doFinally(signal -> API_LOG.info("method={} path={} status={} durationMs={} request={} response={}",
                                    exchange.getRequest().getMethod(),
                                    exchange.getRequest().getPath(),
                                    exchange.getResponse().getStatusCode(),
                                    Duration.between(started, Instant.now()).toMillis(),
                                    requestPayload,
                                    sanitizer.sanitize(responseBuffer.toString())));
                });
    }

    private ServerHttpRequestDecorator decorateRequest(ServerWebExchange exchange, byte[] bytes) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.defer(() -> Flux.just(exchange.getResponse().bufferFactory().wrap(bytes)));
            }
        };
    }

    private ServerHttpResponseDecorator decorateResponse(ServerWebExchange exchange, StringBuilder responseBuffer) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                return super.writeWith(Flux.from(body).map(dataBuffer -> {
                    var bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    responseBuffer.append(new String(bytes, StandardCharsets.UTF_8));
                    return exchange.getResponse().bufferFactory().wrap(bytes);
                }));
            }
        };
    }
}
