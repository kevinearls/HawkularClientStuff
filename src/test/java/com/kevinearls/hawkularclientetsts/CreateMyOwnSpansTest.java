package com.kevinearls.hawkularclientetsts;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.hawkular.apm.api.model.trace.Trace;
import org.hawkular.apm.client.HawkularApmClient;
import org.hawkular.apm.client.api.recorder.BatchTraceRecorder;
import org.hawkular.apm.client.clients.TraceClient;
import org.hawkular.apm.client.model.Criteria;
import org.hawkular.apm.client.opentracing.APMTracer;
import org.hawkular.apm.client.opentracing.DeploymentMetaData;
import org.hawkular.apm.trace.publisher.rest.client.TracePublisherRESTClient;
import org.hawkular.client.core.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;

import static org.hawkular.apm.client.api.sampler.Sampler.ALWAYS_SAMPLE;

/**
 * Created by kearls on 23/03/2017.
 */
public class CreateMyOwnSpansTest {
    private static final Logger logger = LoggerFactory.getLogger(CreateMyOwnSpansTest.class);
    private Tracer tracer;
    private Random random = new Random();

    // Default values, can be overridden by environment variables
    public static String HAWKULAR_APM_URI =  "http://localhost:8080";
    public static String HAKWULAR_APM_PASSWORD = "password";
    public static String HAWKULAR_APM_USERNAME = "jdoe";
    public static String HAWKULAR_SERVICE_NAME = "random-stuff";

    @BeforeClass
    public void beforeClass() {
        // Look for external EVs
        HAWKULAR_APM_URI = getEnv("HAWKULAR_APM_URI", HAWKULAR_APM_URI);
        HAKWULAR_APM_PASSWORD = getEnv("HAKWULAR_APM_PASSWORD", HAKWULAR_APM_PASSWORD);
        HAWKULAR_APM_USERNAME = getEnv("HAWKULAR_APM_USERNAME", HAWKULAR_APM_USERNAME);
        HAWKULAR_SERVICE_NAME = getEnv("HAWKULAR_SERVICE_NAME", HAWKULAR_SERVICE_NAME);

        // Create a Hawkular APM Tracer
        TracePublisherRESTClient restClient = new TracePublisherRESTClient(HAWKULAR_APM_USERNAME, HAKWULAR_APM_PASSWORD, HAWKULAR_APM_URI);
        BatchTraceRecorder traceRecorder = new BatchTraceRecorder.BatchTraceRecorderBuilder()
                .withTracePublisher(restClient)
                .build();

        DeploymentMetaData deploymentMetaData = new DeploymentMetaData(HAWKULAR_SERVICE_NAME, "1");
        tracer = new APMTracer(traceRecorder, ALWAYS_SAMPLE, deploymentMetaData);   // This is an APMTracer
        logger.debug(">>> Tracer is a " + tracer.getClass().getCanonicalName());
    }


    @AfterClass
    public void afterClass() {

    }

    @Test
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

    public String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }
}

