package project.study.jgm.customvocabulary.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.validation.Errors;

import java.io.IOException;

@JsonComponent
public class ErrorsSerializer extends JsonSerializer<Errors> {
    @Override
    public void serialize(Errors errors, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();

        errors.getGlobalErrors().forEach(objectError -> {
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

        errors.getFieldErrors().forEach(fieldError -> {
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
