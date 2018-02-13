package com.github.mthizo247.cloud.netflix.zuul.web.util;

import org.springframework.security.web.util.ThrowableAnalyzer;

/**
 * Created by ronald22 on 13/02/2018.
 */
public class DefaultErrorAnalyzer implements ErrorAnalyzer {
    private ThrowableAnalyzer analyzer = new ThrowableAnalyzer();

    @Override
    public Throwable[] determineCauseChain(Throwable cause) {
        return analyzer.determineCauseChain(cause);
    }

    @Override
    public Throwable getFirstThrowableOfType(Class<? extends Throwable> throwableType, Throwable[] chain) {
        return analyzer.getFirstThrowableOfType(throwableType, chain);
    }
}
