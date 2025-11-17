package com.example.coreboard.domain.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;


import java.io.StringWriter;


import static org.junit.jupiter.api.Assertions.*;


@ContextConfiguration(classes = WebConfig.class)
@ExtendWith(SpringExtension.class)
class WebConfigTest {

    @Test
    void addInterceptors() {
        WebConfig web = new WebConfig();
        web.addInterceptors(new InterceptorRegistry());
    }

    @Test
    @DisplayName("value_notNull")
    void serialize_should_escape_when_value_not_null() throws Exception {

        WebConfig.JsonXssSafeStringSerializer serializer = new WebConfig.JsonXssSafeStringSerializer();
        StringWriter writer = new StringWriter();
        JsonFactory factory = new JsonFactory();
        JsonGenerator generator = factory.createGenerator(writer);

        SerializerProvider provider = null;

        serializer.serialize("<script>alert('x')</script>", generator, provider);
        generator.flush();

        // <script>alert('x')</script>
        // &lt;script&gtalert(&#39;x&#39;)&lt;script&gt&gt;
        assertEquals("\"&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;\"", writer.toString());
    }

    @Test
    @DisplayName("value_null")
    void serialize_should_write_null_when_value_is_null() throws Exception {
        WebConfig.JsonXssSafeStringSerializer serializer = new WebConfig.JsonXssSafeStringSerializer();
        StringWriter writer = new StringWriter();
        JsonFactory factory = new JsonFactory();
        JsonGenerator generator = factory.createGenerator(writer);
        SerializerProvider provider = null;

        serializer.serialize(null, generator, provider);
        generator.flush();

        assertEquals("null", writer.toString());
    }
}