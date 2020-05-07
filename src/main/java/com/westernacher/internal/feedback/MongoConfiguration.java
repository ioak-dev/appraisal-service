package com.westernacher.internal.feedback;

import com.google.common.base.Strings;
import com.mongodb.MongoClientURI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

@Configuration
@Slf4j
public class MongoConfiguration {
    @Value("${spring.data.mongodb.uri}")
    String mongoUri;

    /*@Bean
    public MongoClient mongoClient() {
        log.info("MONGOURIIIIIIIII"+System.getenv("MONGODB_URI"));
        String environmentUrl = Strings.isNullOrEmpty(System.getenv("MONGODB_URI")) ? mongoUri : System.getenv("MONGODB_URI");
        log.info("after seting :"+environmentUrl);
        MongoClientURI uri = new MongoClientURI(environmentUrl);
        return new MongoClient(uri);
    }*/

    @Bean
    public MongoDbFactory mongoDbFactory() {
        String environmentUrl = Strings.isNullOrEmpty(System.getenv("MONGODB_URI")) ? mongoUri : System.getenv("MONGODB_URI");
        return new SimpleMongoDbFactory(new MongoClientURI(environmentUrl));
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
        return mongoTemplate;
    }

}
