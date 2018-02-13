package com.github.mthizo247.cloud.netflix.zuul.web.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronald22 on 13/02/2018.
 */
public class DefaultErrorAnalyzer implements ErrorAnalyzer {

    @Override
    public Throwable[] determineCauseChain(Throwable throwable) {
        List<Throwable> chain = new ArrayList<>();
        Throwable currentThrowable = throwable;

        while (currentThrowable != null) {
            chain.add(currentThrowable);
            currentThrowable = currentThrowable.getCause();
        }

        return chain.toArray(new Throwable[chain.size()]);
    }

    @Override
    public Throwable getFirstThrowableOfType(Class<? extends Throwable> throwableType, Throwable[] chain) {
        if (chain != null) {
            for (Throwable t : chain) {
                if ((t != null) && throwableType.isInstance(t)) {
                    return t;
                }
            }
        }

        return null;
    }
}
