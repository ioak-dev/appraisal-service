package com.westernacher.internal.feedback.config;

import com.bol.crypt.CryptVault;
import com.bol.secure.CachedEncryptionEventListener;
import com.google.common.base.Strings;
import com.mongodb.MongoClientURI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.util.Base64;

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


//    @Bean
//    public CryptVault cryptVault() {
//        return new CryptVault()
////                .with256BitAesCbcPkcs5PaddingAnd128BitSaltKey(0, Base64.getDecoder().decode("o0ujVbQbFv21CYEJKMM76ZNuxk7fjekayv/Pfa6480s="))
////                .with256BitAesCbcPkcs5PaddingAnd128BitSaltKey(1, Base64.getDecoder().decode("fkqjK03nu3qtMzXaA0nH9axYDMWlTZtGOiv94CoBt24="))
//                .with256BitAesCbcPkcs5PaddingAnd128BitSaltKey(0, Base64.getDecoder().decode("GAPWed5b7q9hLIjx/fvEpO9aBzrhSoSAFHIOzINZA0Q="))
//                .with256BitAesCbcPkcs5PaddingAnd128BitSaltKey(1, Base64.getDecoder().decode("fkqjK03nu3qtMzXaA0nH9axYDMWlTZtGOiv94CoBt24="))
//                .with256BitAesCbcPkcs5PaddingAnd128BitSaltKey(2, Base64.getDecoder().decode("/lWVfmhRlNSiiPsfbHI2QzF4TNLvKW2TjMgKw6tXs7Q="))
//                // can be omitted if it's the highest version
//                .withDefaultKeyVersion(2);
//    }
//
//    @Bean
//    public CachedEncryptionEventListener encryptionEventListener(CryptVault cryptVault) {
//        return new CachedEncryptionEventListener(cryptVault);
//    }
}
