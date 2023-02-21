package com.github.cloudgyb.rpc.message;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author geng
 * @since 2023/02/21 21:18:16
 */
public enum MessageSerialTypeEnum {
    JDK, GSON;

    public static MessageSerialTypeEnum getByOrdinal(int ordinal) {
        return Arrays.stream(MessageSerialTypeEnum.values())
                .filter(e -> e.ordinal() == ordinal)
                .findFirst()
                .orElse(GSON);
    }

    public byte[] serialize(RPCMessage message) {
        int type = this.ordinal();
        if (type == JDK.ordinal()) {
            // todo
            return new byte[0];
        } else if (type == GSON.ordinal()) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassTypeAdapter()).create();
            return gson.toJson(message).getBytes(StandardCharsets.UTF_8);
        }
        return new byte[0];
    }

    public RPCMessage deserialize(byte[] message, int messageType) {
        int type = this.ordinal();
        if (type == JDK.ordinal()) {
            // todo
            return null;
        } else if (type == GSON.ordinal()) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassTypeAdapter()).create();
            return gson.fromJson(new String(message), messageType == RPCMessage.RPC_REQUEST_MSG ? RPCRequestMessage.class : null);
        }
        return null;
    }


    static class ClassTypeAdapter implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                return Class.forName(jsonElement.getAsString());
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public JsonElement serialize(Class aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(aClass.getTypeName());
        }
    }
}
