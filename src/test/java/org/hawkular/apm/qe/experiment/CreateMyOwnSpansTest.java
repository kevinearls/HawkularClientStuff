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
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Kevin Earls on 23/03/2017.
 */
public class CreateMyOwnSpansTest extends ApmTestBase {
    private static final Logger logger = LoggerFactory.getLogger(CreateMyOwnSpansTest.class);

    @Test(enabled = false)
    public void simpleTest() throws Exception {
        Span parentSpan = tracer.buildSpan("do_something_important")
                .withTag("tag", "hello")
                .start();

        Span dbProcessingSpan = tracer.buildSpan("an_invoked_operation")
                .asChildOf(parentSpan)
                .start();
        dbProcessingSpan.setTag("randomValue", random.nextInt(500));
        dbProcessingSpan.setBaggageItem("Suitcase", "Tumi");
        dbProcessingSpan.finish();

        parentSpan.finish();

        logger.info(">>> Finished creating span");

        Thread.sleep(1000);    // See JIRA https://issues.jboss.org/browse/HWKAPM-872

        HawkularApmClient client = HawkularApmClient.builder("hawkular")
                .uri("http://localhost:8080")
                .basicAuthentication("jdoe", "password")
                .build();

        TraceClient traceClient = client.trace();
        Criteria criteria = new Criteria();
        ClientResponse<List<Trace>> clientResponse = traceClient.queryFragments(criteria);
        List<Trace> traces = clientResponse.getEntity();

        logger.info("Got " + traces.size() + " traces");

        for (Trace trace : traces ) {
            //trace.
        }
    }


}

