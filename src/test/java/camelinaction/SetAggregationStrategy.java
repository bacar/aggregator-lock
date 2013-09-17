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

import java.util.Set;
import java.util.TreeSet;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

/**
 * This is the aggregation strategy which is java code for <i>aggregating</i>
 * incoming messages with the existing aggregated message. In other words
 * you use this strategy to <i>merge</i> the messages together.
 *
 * @version $Revision: 116 $
 */
public class SetAggregationStrategy implements AggregationStrategy {

    /**
     * Aggregates the messages.
     *
     * @param oldExchange  the existing aggregated message. Is <tt>null</tt> the
     *                     very first time as there are no existing message.
     * @param newExchange  the incoming message. This is never <tt>null</tt>.
     * @return the aggregated message.
     */
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        // the first time there are no existing message and therefore
        // the oldExchange is null. In these cases we just return
        // the newExchange
    	
        String newBody = newExchange.getIn().getBody(String.class);

        if (oldExchange == null) {
			Set<String> vals = new TreeSet<String>();
			vals.add(newBody);
			newExchange.getIn().setBody(vals);
			return newExchange;
        }

		@SuppressWarnings("unchecked")
		Set<String> vals = oldExchange.getIn().getBody(Set.class);
		
        // the body should be the two bodies added together
        vals.add(newBody);

        // update the existing message with the added body
        oldExchange.getIn().setBody(vals);
        // and return it
        return oldExchange;
    }
    
}
