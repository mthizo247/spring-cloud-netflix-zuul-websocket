package com.github.mthizo247.cloud.netflix.zuul.web.util;

import org.springframework.core.env.AbstractPropertyResolver;

import java.util.Map;

/**
 * @author Ronald Mthombeni
 */
public class MapPropertyResolver extends AbstractPropertyResolver {

    private Map<String, Object> map;

    public MapPropertyResolver(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    protected String getPropertyAsRawString(String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }

    @Override
    public boolean containsProperty(String key) {
        return map.containsKey(key);
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key, String.class);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        if (map.containsKey(key)) {
            return targetType.cast(map.get(key));
        }
        return null;
    }

    @Override
    public <T> Class<T> getPropertyAsClass(String key, Class<T> targetType) {
        return null;
    }
}
