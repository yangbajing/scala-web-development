package akkahttp.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2017-03-31.
 */
public class Jackson {
    public static final ObjectMapper defaultObjectMapper =
            new ObjectMapper()
                    .findAndRegisterModules()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
//                    .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                    .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                    .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
                    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                    .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

}