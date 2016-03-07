package com.newrelic.plugins.hornetq.instance;

import org.apache.commons.lang3.StringUtils;

/**
 * Tipos de mensagem
 */
public enum MessageType {

    QUEUE {
        @Override
        boolean isIgnoredObject(Configuration configuration, String objName) {
            return configuration.isIgnoredQueue(objName);
        }
    }, TOPIC {
        @Override
        boolean isIgnoredObject(Configuration configuration, String objName) {
            return configuration.isIgnoredTopic(objName);
        }
    };


    abstract boolean isIgnoredObject(Configuration configuration, String objName);

    @Override
    public String toString() {
        return StringUtils.capitalize(this.name().toLowerCase());
    }


}
