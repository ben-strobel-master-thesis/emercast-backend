package com.strobel.emercast.backend.configuration;

import com.strobel.emercast.backend.db.models.TUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
public class MongoConfiguration {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(new TUID.WriteConverter(), new TUID.ReadConverter()));
    }
}
