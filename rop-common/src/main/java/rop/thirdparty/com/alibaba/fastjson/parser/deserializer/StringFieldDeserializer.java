package rop.thirdparty.com.alibaba.fastjson.parser.deserializer;

import java.lang.reflect.Type;
import java.util.Map;

import rop.thirdparty.com.alibaba.fastjson.parser.DefaultJSONParser;
import rop.thirdparty.com.alibaba.fastjson.parser.JSONLexer;
import rop.thirdparty.com.alibaba.fastjson.parser.JSONToken;
import rop.thirdparty.com.alibaba.fastjson.parser.ParserConfig;
import rop.thirdparty.com.alibaba.fastjson.util.FieldInfo;

public class StringFieldDeserializer extends FieldDeserializer {

    private final ObjectDeserializer fieldValueDeserilizer;

    public StringFieldDeserializer(ParserConfig config, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);

        fieldValueDeserilizer = config.getDeserializer(fieldInfo);
    }

    @Override
    public void parseField(DefaultJSONParser parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        String value;

        final JSONLexer lexer = parser.getLexer();
        if (lexer.token() == JSONToken.LITERAL_STRING) {
            value = lexer.stringVal();
            lexer.nextToken(JSONToken.COMMA);
        } else {

            Object obj = parser.parse();

            if (obj == null) {
                value = null;
            } else {
                value = obj.toString();
            }
        }

        if (object == null) {
            fieldValues.put(fieldInfo.getName(), value);
        } else {
            setValue(object, value);
        }
    }

    public int getFastMatchToken() {
        return fieldValueDeserilizer.getFastMatchToken();
    }
}
