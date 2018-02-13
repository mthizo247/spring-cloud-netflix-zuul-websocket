package com.github.mthizo247.cloud.netflix.zuul.web.util;

/**
 * Created by ronald22 on 13/02/2018.
 */
public interface ErrorAnalyzer {
    Throwable[] determineCauseChain(Throwable cause);

    Throwable getFirstThrowableOfType(Class<? extends Throwable> throwableType, Throwable[] chain);
}
