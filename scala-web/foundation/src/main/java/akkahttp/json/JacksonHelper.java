package akkahttp.json;

import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.util.ByteString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2017-03-31.
 */
public class JacksonHelper {

    public static <T> Marshaller<T, RequestEntity> marshaller() {
        return marshaller(Jackson.defaultObjectMapper);
    }

    public static <T> Marshaller<T, RequestEntity> marshaller(ObjectMapper mapper) {
        return Marshaller.wrapEntity(
                u -> toJSON(mapper, u),
                Marshaller.stringToEntity(),
                MediaTypes.APPLICATION_JSON
        );
    }

    public static <T> Unmarshaller<ByteString, T> byteStringUnmarshaller(Class<T> expectedType) {
        return byteStringUnmarshaller(Jackson.defaultObjectMapper, expectedType);
    }

    public static <T> Unmarshaller<HttpEntity, T> unmarshaller(Class<T> expectedType) {
        return unmarshaller(Jackson.defaultObjectMapper, expectedType);
    }

    public static <T> Unmarshaller<HttpEntity, T> unmarshaller(ObjectMapper mapper, Class<T> expectedType) {
        return Unmarshaller.forMediaType(MediaTypes.APPLICATION_JSON, Unmarshaller.entityToString())
                .thenApply(s -> fromJSON(mapper, s, expectedType));
    }

    public static <T> Unmarshaller<ByteString, T> byteStringUnmarshaller(ObjectMapper mapper, Class<T> expectedType) {
        return Unmarshaller.sync(s -> fromJSON(mapper, s.utf8String(), expectedType));
    }

    private static String toJSON(ObjectMapper mapper, Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot marshal to JSON: " + object, e);
        }
    }

    private static <T> T fromJSON(ObjectMapper mapper, String json, Class<T> expectedType) {
        try {
            return mapper.readerFor(expectedType).readValue(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot unmarshal JSON as " + expectedType.getSimpleName(), e);
        }
    }
}
