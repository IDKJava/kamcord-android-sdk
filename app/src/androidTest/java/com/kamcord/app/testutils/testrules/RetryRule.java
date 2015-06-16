package com.kamcord.app.testutils.testrules;

import android.util.EventLogTags;

import com.kamcord.app.utils.StringUtils;

import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static com.kamcord.app.testutils.SystemUtilities.stopService;

/**
 * Created by Mehmet on 6/11/15.
 */
public class RetryRule implements TestRule {

    private int retryCount;

    public RetryRule(int retryCount) {
        this.retryCount = retryCount;
    }

    public Statement apply(Statement base, Description description) {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable caughtThrowable = null;

                for (int i = 0; i < retryCount; i++) {
                    try {
                        base.evaluate();
                        return;
                    } catch (Throwable t) {
                        caughtThrowable = t;
                        System.err.println(String.format("%s run %d failed", description.getDisplayName(), (i + 1)));
                        if(t instanceof NullPointerException){
                            throw t;
                        }
                    }
                }
                System.err.println(String.format("%s giving up after %s failures", description.getDisplayName(), retryCount));
                throw caughtThrowable;
            }
        };
    }
}