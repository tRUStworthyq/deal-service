package ru.sber.dealservice.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.proto.CbrCurrencyServiceGrpc;

@Configuration
public class GrpcClientConfig {

    @Value("${app.grpc.currency-service.host:localhost}")
    private String host;

    @Value("${app.grpc.currency-service.port:9090}")
    private int port;

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel currencyServiceChannel() {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public CbrCurrencyServiceGrpc.CbrCurrencyServiceBlockingStub currencyServiceStub(ManagedChannel currencyServiceChannel) {
        return CbrCurrencyServiceGrpc.newBlockingStub(currencyServiceChannel);
    }
}