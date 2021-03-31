package project.study.jgm.customvocabulary.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.validation.BindingResult;

import java.io.IOException;

public class BindingResultSerializer extends JsonSerializer<BindingResult> {
    @Override
    public void serialize(BindingResult bindingResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();

        bindingResult.getGlobalErrors().forEach(objectError -> {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("objectName", objectError.getObjectName());
                jsonGenerator.writeStringField("code", objectError.getCode());
                jsonGenerator.writeStringField("defaultMessage", objectError.getDefaultMessage());
                jsonGenerator.writeEndObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        bindingResult.getFieldErrors().forEach(fieldError -> {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("objectName", fieldError.getObjectName());
                jsonGenerator.writeStringField("code", fieldError.getCode());
                jsonGenerator.writeStringField("defaultMessage", fieldError.getDefaultMessage());
                jsonGenerator.writeStringField("field", fieldError.getField());
                Object rejectedValue = fieldError.getRejectedValue();
                if (rejectedValue != null) {
                    jsonGenerator.writeStringField("rejectedValue", rejectedValue.toString());
                }
                jsonGenerator.writeEndObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        jsonGenerator.writeEndArray();
    }
}
