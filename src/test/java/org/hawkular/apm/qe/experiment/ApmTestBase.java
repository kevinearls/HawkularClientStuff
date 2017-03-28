package org.hawkular.apm.qe.experiment;

import io.opentracing.Tracer;
import org.hawkular.apm.client.api.recorder.BatchTraceRecorder;
import org.hawkular.apm.client.opentracing.APMTracer;
import org.hawkular.apm.client.opentracing.DeploymentMetaData;
import org.hawkular.apm.trace.publisher.rest.client.TracePublisherRESTClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import java.util.Random;

import static org.hawkular.apm.client.api.sampler.Sampler.ALWAYS_SAMPLE;

/**
 * Created by Kevin Earls on 28/03/2017.
 */
public class ApmTestBase {
    private static final Logger logger = LoggerFactory.getLogger(ApmTestBase.class);
    private static final Long MAX_WAIT_IN_MILLISECONDS = 100L;

    Tracer tracer;
    Random random = new Random();

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

    /**
     * Look for an environment variable with the given name; if it exists return its value,
     * otherwise return the default
     *
     * @param name
     * @param defaultValue
     * @return found environment variable value or given default value
     */
    String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    /**
     * Wait for a random amount of time up to MAX_WAIT_IN_MILLISECONDS
     *
     * @return number of milliseconds waited
     */
    long waitRandom() {
        long waitTime = (long) (random.nextDouble() * MAX_WAIT_IN_MILLISECONDS);
        try {
            logger.info("Sleeping for " + waitTime + " ms");
            Thread.sleep((int) waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return waitTime;
    }
}
