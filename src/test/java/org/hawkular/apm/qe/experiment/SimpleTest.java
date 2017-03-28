package org.hawkular.apm.qe.experiment;

import org.hawkular.apm.api.model.Property;
import org.hawkular.apm.api.model.analytics.EndpointInfo;
import org.hawkular.apm.api.model.trace.Trace;
import org.hawkular.apm.client.model.Criteria;
import org.hawkular.apm.client.HawkularApmClient;
import org.hawkular.apm.client.clients.AnalyticsClient;
import org.hawkular.apm.client.clients.ConfigurationClient;
import org.hawkular.apm.client.clients.TraceClient;
import org.hawkular.client.core.ClientResponse;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

/**
 * Created by kearls on 22/03/2017.
 */
public class SimpleTest extends ApmTestBase {

    @Test
    public void simpleTest() throws Exception {
        HawkularApmClient client = HawkularApmClient.builder("hawkular")
                .uri("http://localhost:8080")
                .basicAuthentication("jdoe", "password")
                .build();
        System.out.println("Tenant " + client.getTenant());

        ConfigurationClient configurationClient = client.configuration();
        AnalyticsClient analyticsClient = client.analytics();
        System.out.println("AnalyticsClient is a " + analyticsClient.getClass().getCanonicalName());
        long now = System.currentTimeMillis();
        // FIXME what parameters are needed here?
        ClientResponse<List<EndpointInfo>> boundEndpoints = analyticsClient.listBoundEndpoint("opentracing-demo", (long) 0, now);
        List<EndpointInfo> boundEndpointsInfo = boundEndpoints.getEntity();
        System.out.println("ENDPOINT Count " + boundEndpointsInfo.size());


        TraceClient traceClient = client.trace();


        System.out.println("TraceLient is a " + traceClient.getClass().getCanonicalName());
        Criteria criteria = new Criteria();      // TODO How to set criteria; are we really testing REST here?
        criteria.addProperty("waitTime", "250", org.hawkular.apm.api.services.Criteria.Operator.GT);
        ClientResponse<List<Trace>> clientResponse = traceClient.queryFragments(criteria);
        List<Trace> traces = clientResponse.getEntity();

        for (Trace trace : traces) {

            //System.out.println(trace.toString());
            Set<Property> ick = trace.getProperties("waitTime");
            for (Property p : ick) {
                System.out.println("WaitTime: " + trace.getFragmentId() + " - " + p.getValue());
            }
            //System.out.println(trace.getProperties("waitTime"));
        }

        System.out.println("Got " + traces.size() + " traces");

        /*
        ClientResponse<Long> traceCompletionCount = analyticsClient.traceCompletionCount(criteria);

        System.out.println("Trace completion count " + traceCompletionCount.getEntity());

        ClientResponse<Percentiles> traceCompletionPercentiles = analyticsClient.traceCompletionPercentiles(criteria);
        Percentiles percentiles = traceCompletionPercentiles.getEntity();

        Map<Integer, Long> blah = percentiles.getPercentiles();
        System.out.println("---- PERCENTILES -----");
        for (Integer i : blah.keySet()) {
            System.out.println("\t" + i + ": " + blah.get(i));
        }
        */

        System.out.println("Finished");
    }
}
