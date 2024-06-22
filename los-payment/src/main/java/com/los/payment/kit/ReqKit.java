package com.los.payment.kit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.los.core.exception.LosException;
import jakarta.servlet.http.HttpServletRequest;

public class ReqKit {
    private static final ObjectMapper MAPPER;
    static {
        MAPPER = new ObjectMapper();
    }
    public static String toJson(Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new LosException("Object convert to string error");
        }
    }
    public static <T> T fromJson(String json, Class<T> clazz) {
        return MAPPER.convertValue(json, clazz);
    }
    public static <T> T fromJson(HttpServletRequest req,Class<T> clazz) {


    }
}
