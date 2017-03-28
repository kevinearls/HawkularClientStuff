package org.hawkular.apm.qe.experiment;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import org.hawkular.apm.api.model.trace.Trace;
import org.hawkular.apm.client.HawkularApmClient;
import org.hawkular.apm.client.clients.TraceClient;
import org.hawkular.apm.client.model.Criteria;
import org.hawkular.client.core.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Kevin Earls on 23/03/2017.
 */
public class GenerateSpansTest extends ApmTestBase {
    private static final Logger logger = LoggerFactory.getLogger(GenerateSpansTest.class);
    private AtomicInteger childId = new AtomicInteger(0);

    @DataProvider(parallel = false)
    private final Object[][] getParameters() {
        // Number of spans to create, maximum depth of a span, maximum breadth
        Object[][] settings = new Object[][] {
                {10, 5, 4}
        };

        return settings;
    };

    @Test(dataProvider = "getParameters")
    public void generateSpans(int number, int depth, int maxBreadth) throws Exception {
        logger.info(">>>> generateSpans called with number " + number + " depth " + depth + " breadth " + maxBreadth);

        LocalDateTime now = LocalDateTime.now();
        String nowString = now.format(DateTimeFormatter.ISO_DATE_TIME);
        for (int i=1; i <= number; i++) {
            logger.info(">>>>> Creating parent " + i);
            Span parentSpan = tracer.buildSpan("ParentSpan")
                    .withTag("startTime", nowString)
                    .withTag("parentId", i)
                    .start();

            long startDelay = waitRandom();
            parentSpan.setTag("startDelay", startDelay);

            createChildSpan(parentSpan.context(), depth - 1, maxBreadth);

            parentSpan.finish();
            Thread.sleep(2 * 1000);  // TODO remove, workaround for JIRA ????
        }
    }


    /**
     *
     * @param parentSpan
     * @param depth
     * @param breadth
     */
    public void createChildSpan(SpanContext parentSpan, int depth, int breadth) {
        logger.info("createChildSpan called with depth " + depth + " breadth " + breadth);
        int breadthForThisLevel = random.nextInt(breadth) + 1;
        logger.info(">>> Creating " + breadthForThisLevel + " children at this level");

        for (int i = 0; i < breadthForThisLevel; i++) {
            int id = childId.getAndIncrement();
            logger.info(">>>> Creating child " + id);
            Span child =  tracer.buildSpan("an_invoked_operation")
                    .asChildOf(parentSpan)
                    .start();
            child.setTag("ChildId", id);
            child.setTag("randomValue", random.nextInt(500));
            child.setTag("delay", waitRandom());

            if (depth > 0) {
                int childDepth = random.nextInt(depth);
                if (childDepth > 0) {
                    logger.info(">>>> About to create children with depth of " + childDepth);
                    createChildSpan(child.context(), childDepth, breadth);
                }
            }

            child.finish();
        }
    }
}

