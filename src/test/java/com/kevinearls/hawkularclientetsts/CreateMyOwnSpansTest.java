package com.kevinearls.hawkularclientetsts;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.hawkular.apm.client.api.recorder.BatchTraceRecorder;
import org.hawkular.apm.client.opentracing.APMTracer;
import org.hawkular.apm.client.opentracing.DeploymentMetaData;
import org.hawkular.apm.trace.publisher.rest.client.TracePublisherRESTClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

import static org.hawkular.apm.client.api.sampler.Sampler.ALWAYS_SAMPLE;

/**
 * Created by kearls on 23/03/2017.
 */
public class CreateMyOwnSpansTest {
    private Tracer tracer;
    private Random random = new Random();

    public static final String HAWKULAR_APM_URL =  "http://localhost:8080";
    public static final String HAKWULAR_APM_PASSWORD = "password";
    public static final String HAWKULAR_APM_USER = "jdoe";
    public static final String DEMO_SERVICE_NAME = "opentracing-demo";

    @BeforeClass
    public void beforeClass() {
        // Create the Tracer
        TracePublisherRESTClient restClient = new TracePublisherRESTClient(HAWKULAR_APM_USER, HAKWULAR_APM_PASSWORD, HAWKULAR_APM_URL);
        BatchTraceRecorder traceRecorder = new BatchTraceRecorder.BatchTraceRecorderBuilder()
                .withTracePublisher(restClient)
                .build();

        DeploymentMetaData deploymentMetaData = new DeploymentMetaData(DEMO_SERVICE_NAME, "1");
        tracer = new APMTracer(traceRecorder, ALWAYS_SAMPLE, deploymentMetaData);   // This is an APMTracer
        System.out.println(">>> Tracer is a " + tracer.getClass().getCanonicalName());
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
        //dbProcessingSpan.setBaggageItem("Suitecase", "Tumi");

        dbProcessingSpan.finish();
        parentSpan.finish();

        System.out.println(">>> Finished?");
    }
}

