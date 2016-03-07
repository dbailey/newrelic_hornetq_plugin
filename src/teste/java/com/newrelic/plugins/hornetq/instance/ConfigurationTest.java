package com.newrelic.plugins.hornetq.instance;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ConfigurationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deve_criar() throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("ignoreQueues", "fila1,fila2");

        Configuration result = new Configuration(properties);

        assertEquals("localhost", result.getHost());
        assertTrue(result.isIgnoredQueue("fila1"));
        assertTrue(result.isIgnoredQueue("fila2"));
        assertFalse(result.isIgnoredQueue("fila3"));


    }
}