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
    public <T> T getProperty(String key, Class<T> targetType) {
        Object v = map.get(key);
        if (v != null) {
            return targetType.cast(v);
        }
        return null;
    }
}
