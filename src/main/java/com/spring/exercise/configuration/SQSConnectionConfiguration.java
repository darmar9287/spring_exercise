package com.spring.exercise.configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PreDestroy;

@Configuration
@EnableSqs
@Getter
@Slf4j
public class SQSConnectionConfiguration {
    private AmazonSQSAsync client;
    private final String endpoint;
    private final String region;
    private final String accessKey;
    private final String secretKey;

    public SQSConnectionConfiguration(
            @Value("${application.sqs.host}") String endpoint,
            @Value("${application.sqs.region}") String region,
            @Value("${application.sqs.access_key}") String accessKey,
            @Value("${application.sqs.secret_key}") String secretKey
     ) {
        this.endpoint = endpoint;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    @Bean
    @Primary
    public AmazonSQSAsync amazonSQSClient() {
        log.info("Connecting to SQS Queue...");

        client = AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .build();

        log.info("Connected to SQS");

        return client;
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSqs) {
        return new QueueMessagingTemplate(amazonSQSClient());
    }

    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory() {
        SimpleMessageListenerContainerFactory msgListenerContainerFactory = new SimpleMessageListenerContainerFactory();
        msgListenerContainerFactory.setAmazonSqs(amazonSQSClient());

        return msgListenerContainerFactory;
    }

    @PreDestroy
    public void tearDownConnection() {
        client.shutdown();
    }
}