package org.wildfly.extras.creaper.core.online;

import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.wildfly.extras.creaper.core.online.ModelNodeConstants.BATCH_RESULT;
import static org.wildfly.extras.creaper.core.online.ModelNodeConstants.DEFINED_RESULT_BOOLEAN;
import static org.wildfly.extras.creaper.core.online.ModelNodeConstants.DEFINED_RESULT_LIST_BOOLEAN;
import static org.wildfly.extras.creaper.core.online.ModelNodeConstants.DEFINED_RESULT_LIST_NUMBER;
import static org.wildfly.extras.creaper.core.online.ModelNodeConstants.DEFINED_RESULT_NUMBER;
import static org.wildfly.extras.creaper.core.online.ModelNodeConstants.FAILED;
import static org.wildfly.extras.creaper.core.online.ModelNodeConstants.RELOAD_REQUIRED;
import static org.wildfly.extras.creaper.core.online.ModelNodeConstants.RESTART_REQUIRED;
import static org.wildfly.extras.creaper.core.online.ModelNodeConstants.RESTART_REQUIRED_IN_DOMAIN;
import static org.wildfly.extras.creaper.core.online.ModelNodeConstants.SUCCESS;
import static org.wildfly.extras.creaper.core.online.ModelNodeConstants.NOT_DEFINED_RESULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ModelNodeResultTest {
    private static final String ADDITIONAL_ASSERTION_MESSAGE = "Additional assertion message";

    @Test
    public void isSuccess() throws IOException {
        ModelNodeResult result = new ModelNodeResult(SUCCESS);

        assertTrue(result.isSuccess());
        assertFalse(result.isFailed());
    }

    @Test
    public void isFailed() {
        ModelNodeResult result = new ModelNodeResult(FAILED);

        assertFalse(result.isSuccess());
        assertTrue(result.isFailed());
    }

    @Test
    public void assertSuccess() {
        ModelNodeResult result = new ModelNodeResult(SUCCESS);

        result.assertSuccess();

        try {
            result.assertFailed();
        } catch (AssertionError ignored) {
        }
    }

    @Test
    public void assertSuccessWithMessage() {
        ModelNodeResult result = new ModelNodeResult(SUCCESS);

        result.assertSuccess(ADDITIONAL_ASSERTION_MESSAGE);

        try {
            result.assertFailed(ADDITIONAL_ASSERTION_MESSAGE);
        } catch (AssertionError assertionError) {
            assertNotNull(assertionError.getMessage());
            assertTrue(assertionError.getMessage().contains(ADDITIONAL_ASSERTION_MESSAGE));
        }
    }

    @Test
    public void assertFailed() {
        ModelNodeResult result = new ModelNodeResult(FAILED);

        result.assertFailed();

        try {
            result.assertSuccess();
        } catch (AssertionError ignore) {
        }
    }

    @Test
    public void assertFailedWithMessage() {
        ModelNodeResult result = new ModelNodeResult(FAILED);

        result.assertFailed(ADDITIONAL_ASSERTION_MESSAGE);

        try {
            result.assertSuccess(ADDITIONAL_ASSERTION_MESSAGE);
        } catch (AssertionError assertionError) {
            assertNotNull(assertionError.getMessage());
            assertTrue(assertionError.getMessage().contains(ADDITIONAL_ASSERTION_MESSAGE));
        }
    }


    @Test
    public void hasDefinedValue() {
        ModelNodeResult result = new ModelNodeResult(DEFINED_RESULT_BOOLEAN);
        result.assertSuccess();
        assertTrue(result.hasDefinedValue());

        result = new ModelNodeResult(NOT_DEFINED_RESULT);
        result.assertSuccess();
        assertFalse(result.hasDefinedValue());
    }

    @Test
    public void assertDefinedValue() {
        ModelNodeResult result = new ModelNodeResult(DEFINED_RESULT_BOOLEAN);
        result.assertDefinedValue();

        result = new ModelNodeResult(NOT_DEFINED_RESULT);
        result.assertNotDefinedValue();
    }

    @Test
    public void assertDefinedValueWithMessage() {
        ModelNodeResult result = new ModelNodeResult(DEFINED_RESULT_BOOLEAN);
        result.assertDefinedValue(ADDITIONAL_ASSERTION_MESSAGE);

        result = new ModelNodeResult(NOT_DEFINED_RESULT);
        result.assertNotDefinedValue(ADDITIONAL_ASSERTION_MESSAGE);
    }

    @Test
    public void value() {
        ModelNodeResult result = new ModelNodeResult(DEFINED_RESULT_BOOLEAN);
        assertTrue(result.booleanValue());
        assertTrue(result.booleanValue(false));

        result = new ModelNodeResult(DEFINED_RESULT_NUMBER);
        assertEquals(13, result.intValue());
        assertEquals(13L, result.longValue());
        assertEquals("13", result.stringValue());
        assertEquals(13, result.intValue(42));
        assertEquals(13L, result.longValue(42L));
        assertEquals("13", result.stringValue("42"));
    }

    @Test
    public void listValue() {
        ModelNodeResult result = new ModelNodeResult(DEFINED_RESULT_LIST_BOOLEAN);
        assertEquals(Collections.singletonList(true), result.booleanListValue());
        assertEquals(Collections.singletonList(true), result.booleanListValue(Collections.singletonList(false)));

        result = new ModelNodeResult(DEFINED_RESULT_LIST_NUMBER);
        assertEquals(Collections.singletonList(13), result.intListValue());
        assertEquals(Collections.singletonList(13L), result.longListValue());
        assertEquals(Collections.singletonList("13"), result.stringListValue());
        assertEquals(Collections.singletonList(13), result.intListValue(Collections.singletonList(42)));
        assertEquals(Collections.singletonList(13L), result.longListValue(Collections.singletonList(42L)));
        assertEquals(Collections.singletonList("13"), result.stringListValue(Collections.singletonList("42")));
    }

    @Test
    public void undefinedValue() {
        ModelNodeResult result = new ModelNodeResult(NOT_DEFINED_RESULT);

        try {
            result.booleanValue();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            result.intValue();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            result.longValue();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            result.stringValue();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            result.booleanListValue();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            result.intListValue();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            result.longListValue();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            result.stringListValue();
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        assertEquals(true, result.booleanValue(true));
        assertEquals(42, result.intValue(42));
        assertEquals(42L, result.longValue(42L));
        assertEquals("42", result.stringValue("42"));
        assertEquals(Collections.<Boolean>emptyList(), result.booleanListValue(Collections.<Boolean>emptyList()));
        assertEquals(Collections.<Integer>emptyList(), result.intListValue(Collections.<Integer>emptyList()));
        assertEquals(Collections.<Long>emptyList(), result.longListValue(Collections.<Long>emptyList()));
        assertEquals(Collections.<String>emptyList(), result.stringListValue(Collections.<String>emptyList()));
        assertEquals(Collections.singletonList(true), result.booleanListValue(Collections.singletonList(true)));
        assertEquals(Collections.singletonList(42), result.intListValue(Collections.singletonList(42)));
        assertEquals(Collections.singletonList(42L), result.longListValue(Collections.singletonList(42L)));
        assertEquals(Collections.singletonList("42"), result.stringListValue(Collections.singletonList("42")));
    }

    @Test
    public void batch() {
        ModelNodeResult result = new ModelNodeResult(BATCH_RESULT);
        result.assertDefinedValue();

        try {
            result.forBatchStep(0);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        ModelNodeResult stepResult = result.forBatchStep(1);
        stepResult.assertDefinedValue();
        assertEquals("running", stepResult.stringValue());

        stepResult = result.forBatchStep(2);
        stepResult.assertDefinedValue();
        assertEquals("reload-required", stepResult.stringValue());

        try {
            result.forBatchStep(3);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void batchIterable() {
        ModelNodeResult result = new ModelNodeResult(BATCH_RESULT);
        result.assertDefinedValue();

        int count = 0;
        for (ModelNodeResult stepResult : result.forAllBatchSteps()) {
            count++;
            stepResult.assertDefinedValue();
        }

        assertEquals(2, count);
    }

    @Test
    public void headers() {
        ModelNodeResult result = new ModelNodeResult(DEFINED_RESULT_BOOLEAN);
        assertFalse(result.headers().isDefined());

        result = new ModelNodeResult(RELOAD_REQUIRED);
        assertTrue(result.headers().isDefined());

        result = new ModelNodeResult(RESTART_REQUIRED);
        assertTrue(result.headers().isDefined());

        result = new ModelNodeResult(RESTART_REQUIRED_IN_DOMAIN);
        assertFalse(result.headers().isDefined());
    }

    @Test
    public void isReloadRequired() {
        ModelNodeResult result = new ModelNodeResult(DEFINED_RESULT_BOOLEAN);
        assertFalse(result.isReloadRequired());

        result = new ModelNodeResult(RELOAD_REQUIRED);
        assertTrue(result.isReloadRequired());

        result = new ModelNodeResult(RESTART_REQUIRED);
        assertFalse(result.isReloadRequired());
    }

    @Test
    public void isRestartRequired() {
        ModelNodeResult result = new ModelNodeResult(DEFINED_RESULT_BOOLEAN);
        assertFalse(result.isRestartRequired());

        result = new ModelNodeResult(RELOAD_REQUIRED);
        assertFalse(result.isRestartRequired());

        result = new ModelNodeResult(RESTART_REQUIRED);
        assertTrue(result.isRestartRequired());
    }

    @Test
    public void isDomain() {
        ModelNodeResult result = new ModelNodeResult(DEFINED_RESULT_BOOLEAN);
        assertFalse(result.isFromDomain());

        result = new ModelNodeResult(RELOAD_REQUIRED);
        assertFalse(result.isFromDomain());

        result = new ModelNodeResult(RESTART_REQUIRED);
        assertFalse(result.isFromDomain());

        result = new ModelNodeResult(RESTART_REQUIRED_IN_DOMAIN);
        assertTrue(result.isFromDomain());
    }

    @Test
    public void forServer() {
        ModelNodeResult result = new ModelNodeResult(RESTART_REQUIRED_IN_DOMAIN);
        ModelNodeResult server1 = result.forServer("master", "server-one");
        ModelNodeResult server2 = result.forServer("master", "server-two");

        assertTrue(server1.isDefined());
        assertTrue(server1.isSuccess());
        assertTrue(server1.isRestartRequired());

        assertTrue(server2.isDefined());
        assertTrue(server2.isSuccess());
        assertTrue(server2.isRestartRequired());

        try {
            result.forServer("slave", "server-one");
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            result.forServer("master", "server-three");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }
}
