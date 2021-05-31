package com.westernacher.internal.feedback.config;

import com.bol.crypt.CryptVault;
import com.bol.secure.CachedEncryptionEventListener;
import com.google.common.base.Strings;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Configuration
@Slf4j
public class MongoConfiguration {
    @Value("${spring.data.mongodb.uri}")
    String mongoUri;

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

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new DateToZonedDateTimeConverter());
        converters.add(new ZonedDateTimeToDateConverter());
        return new MongoCustomConversions(converters);
    }

    @Bean
    public MongoClientOptions.Builder mongoClientOptions() {

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                .automatic(true)
                .build();

        CodecRegistry registry = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(
                        new ZonedDateTimeCodec()
                ),
                MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoCodecProvider)
        );

        return MongoClientOptions.builder()
                .codecRegistry(registry);
    }

    class ZonedDateTimeCodec implements Codec<ZonedDateTime> {
        @Override
        public Class<ZonedDateTime> getEncoderClass() {
            return ZonedDateTime.class;
        }

        @Override
        public void encode(BsonWriter writer, ZonedDateTime value, EncoderContext encoderContext) {
            writer.writeDateTime(value.toInstant().toEpochMilli());
        }

        @Override
        public ZonedDateTime decode(BsonReader reader, DecoderContext decoderContext) {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(reader.readDateTime()), ZoneId.systemDefault());
        }
    }

    class DateToZonedDateTimeConverter implements org.springframework.core.convert.converter.Converter<Date, ZonedDateTime> {

        @Override
        public ZonedDateTime convert(Date source) {
            return source == null ? null :
                    ZonedDateTime.ofInstant(source.toInstant(), ZoneId.systemDefault());
        }
    }

    class ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {

        @Override
        public Date convert(ZonedDateTime source) {
            return source == null ? null : Date.from(source.toInstant());
        }
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
