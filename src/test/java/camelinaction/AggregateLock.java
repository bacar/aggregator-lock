/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package camelinaction;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * The ABC example for using the Aggregator EIP.
 * <p/>
 * This example have 4 messages send to the aggregator, by which one
 * message is published which contains the aggregation of message 1,2 and 4
 * as they use the same correlation key.
 * <p/>
 * See the class {@link camelinaction.SetAggregationStrategy} for how the messages
 * are actually aggregated together.
 *
 * @see camelinaction.SetAggregationStrategy
 * @version $Revision: 116 $
 */
public class AggregateLock extends CamelTestSupport {

    @Test
    public void testABC() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        // we expect ABC in the published message
        // notice: Only 1 message is expected
        mock.expectedBodiesReceivedInAnyOrder(
        		"[0:0, 0:1, 0:2, 0:3, 0:4]", 
        		"[1:0, 1:1, 1:2, 1:3, 1:4]");
        
        for (int i = 0; i<5; ++i) {
        	for(int j=0; j<2; ++j) {
                String body = j+":"+i;
        		Logger.getLogger(AggregateLock.class).info("About to send " + body + " to camel...");
				template.sendBodyAndHeader("seda:foo?concurrentConsumers=2", body, "myId", j);
        		Logger.getLogger(AggregateLock.class).info("Sent " + body + " to camel...");
        	}
		}
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("seda:foo?concurrentConsumers=2")
                    // do a little logging
                    .log("Aggregating ${body} with correlation key ${header.myId}")
                    .aggregate(header("myId"), new SetAggregationStrategy()).completionSize(5)
                        .convertBodyTo(String.class)
                        .log("Sending out ${body} after a short pause...")
                        .delay(3000)
                        .log("Sending out ${body} imminently!")
                        .to("mock:result");
            }
        };
    }
}
