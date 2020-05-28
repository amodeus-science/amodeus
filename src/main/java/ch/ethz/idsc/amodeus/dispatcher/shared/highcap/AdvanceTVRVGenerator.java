/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.matsim.amodeus.dvrp.request.AVRequest;

/* package */ class AdvanceTVRVGenerator {
    private final Set<Set<AVRequest>> rvEdges = new HashSet<>();
    private final double pickupDurationPerStop;
    private final double dropoffDurationPerStop;
    private final Map<Set<AVRequest>, Double> rvEdgesValidityMap = new HashMap<>();

    public AdvanceTVRVGenerator(double pickupDurationPerStop, double dropoffDurationPerStop) {
        this.pickupDurationPerStop = pickupDurationPerStop;
        this.dropoffDurationPerStop = dropoffDurationPerStop;
    }

    public Set<Set<AVRequest>> generateRVGraph(Set<AVRequest> newAddedRequests, Set<AVRequest> removedRequests, //
            Set<AVRequest> remainedRequests, double now, TravelTimeComputation ttc, Map<AVRequest, RequestKeyInfo> requestKeyInfoMap) {

        // 1. remove edges that contains no longer open/valid request and remove no longer valid RV edges
        if (!rvEdges.isEmpty()) {
            // 1.1 add no longer valid edges to edgeToRemove
            Set<Set<AVRequest>> edgesToRemove = rvEdges.stream().filter(rvEdge -> rvEdgesValidityMap.get(rvEdge) < now).collect(Collectors.toSet());

            // 1.2 add edge containing removed request to edgesToRemove
            rvEdges.stream().filter(edge -> removedRequests.stream().anyMatch(edge::contains)).forEach(edgesToRemove::add);

            // 1.3 remove
            rvEdges.removeAll(edgesToRemove);
            edgesToRemove.forEach(rvEdgesValidityMap::remove);
        }

        // 2. add new edges between one remained request and one new added request
        if (!rvEdges.isEmpty())
            for (AVRequest avRequest : remainedRequests)
                for (AVRequest avRequest2 : newAddedRequests) {
                    double edgeSlackTime = getEdgeValidUntil(avRequest, avRequest2, now, requestKeyInfoMap, ttc);
                    if (edgeSlackTime > 0) {
                        Set<AVRequest> possibleEdge = new HashSet<>();
                        possibleEdge.add(avRequest);
                        possibleEdge.add(avRequest2);
                        rvEdges.add(possibleEdge);
                        rvEdgesValidityMap.put(possibleEdge, edgeSlackTime + now);
                    }
                }

        // 3. add new rvEdges (between new added requests)
        List<AVRequest> newAddedRequestList = new ArrayList<>(newAddedRequests);
        for (int i = 0; i < newAddedRequestList.size(); i++)
            for (int j = i + 1; j < newAddedRequestList.size(); j++) {
                double edgeSlackTime = getEdgeValidUntil(newAddedRequestList.get(i), newAddedRequestList.get(j), now, requestKeyInfoMap, ttc);
                if (edgeSlackTime > 0) {
                    Set<AVRequest> newEdge = new HashSet<>();
                    newEdge.add(newAddedRequestList.get(i));
                    newEdge.add(newAddedRequestList.get(j));
                    rvEdges.add(newEdge);
                    rvEdgesValidityMap.put(newEdge, edgeSlackTime + now);
                }
            }

        return rvEdges; // rvEdges will still be here, and will be used next time the function is called
    }

    private double getEdgeValidUntil(AVRequest request1, AVRequest request2, double now, //
            Map<AVRequest, RequestKeyInfo> requestKeyInfoMap, TravelTimeComputation ttc) {
        double edgeSlackTime = 0.0;
        double ss1 = 0.0;
        double ss2 = 0.0;
        double ss3 = 0.0;
        double ss4 = 0.0;

        // r1 first
        double t1 = ttc.of(request1.getFromLink(), request2.getFromLink(), now, false);

        double value = requestKeyInfoMap.get(request2).getDeadlinePickUp();
        if (now + t1 <= value) {
            double s1 = value - t1 - now - pickupDurationPerStop;

            ttc.storeInCache(request1.getFromLink(), request2.getFromLink(), t1);

            // case 1
            double t2 = ttc.of(request2.getFromLink(), request2.getToLink(), now, true);
            double value2 = requestKeyInfoMap.get(request2).getDeadlineDropOff();
            if (now + t1 + t2 + 2 * pickupDurationPerStop <= value2) {
                double s2 = value2 - t1 - t2 - now - 2 * pickupDurationPerStop;
                double t3 = ttc.of(request2.getToLink(), request1.getToLink(), now, false);
                if (now + t1 + t2 + t3 + 2 * pickupDurationPerStop + dropoffDurationPerStop <= requestKeyInfoMap.get(request1).getDeadlineDropOff()) {
                    double s3 = requestKeyInfoMap.get(request1).getDeadlineDropOff() - t1 - t2 - t3 - now - 2 * pickupDurationPerStop - dropoffDurationPerStop;
                    ttc.storeInCache(request2.getToLink(), request1.getToLink(), t3);
                    ss1 = getMinValue(s1, s2, s3);
                }
            }

            // case 2
            double t4 = ttc.of(request2.getFromLink(), request1.getToLink(), now, false);
            if (now + t1 + t4 + 2 * pickupDurationPerStop <= requestKeyInfoMap.get(request1).getDeadlineDropOff()) {
                double s4 = requestKeyInfoMap.get(request1).getDeadlineDropOff() - t1 - t4 - now - 2 * pickupDurationPerStop;
                ttc.storeInCache(request2.getFromLink(), request1.getToLink(), t4);
                double t5 = ttc.of(request1.getToLink(), request2.getToLink(), now, false);
                if (now + t1 + t4 + t5 + 2 * pickupDurationPerStop + dropoffDurationPerStop <= requestKeyInfoMap.get(request2).getDeadlineDropOff()) {
                    double s5 = requestKeyInfoMap.get(request2).getDeadlineDropOff() - now - t1 - t4 - t5 - 2 * pickupDurationPerStop - dropoffDurationPerStop;
                    ttc.storeInCache(request1.getToLink(), request2.getToLink(), t5);
                    ss2 = getMinValue(s1, s4, s5);
                }
            }
        }
        // r2 first
        double t6 = ttc.of(request2.getFromLink(), request1.getFromLink(), now, false);
        if (now + t6 + pickupDurationPerStop <= requestKeyInfoMap.get(request1).getDeadlinePickUp()) {
            double s6 = requestKeyInfoMap.get(request1).getDeadlinePickUp() - now - t6 - pickupDurationPerStop;
            ttc.storeInCache(request2.getFromLink(), request1.getFromLink(), t6);
            // case 3
            double t7 = ttc.of(request1.getFromLink(), request1.getToLink(), now, true);
            if (now + t6 + t7 + 2 * pickupDurationPerStop <= requestKeyInfoMap.get(request1).getDeadlineDropOff()) {
                double s7 = requestKeyInfoMap.get(request1).getDeadlineDropOff() - now - t6 - t7 - 2 * pickupDurationPerStop;
                double t5 = ttc.of(request1.getToLink(), request2.getToLink(), now, false);
                if (now + t6 + t7 + t5 + 2 * pickupDurationPerStop + dropoffDurationPerStop <= requestKeyInfoMap.get(request2).getDeadlineDropOff()) {
                    double s8 = requestKeyInfoMap.get(request2).getDeadlineDropOff() - now - t6 - t7 - t5 - 2 * pickupDurationPerStop - dropoffDurationPerStop;
                    ttc.storeInCache(request1.getToLink(), request2.getToLink(), t5);
                    ss3 = getMinValue(s6, s7, s8);
                }
            }

            // case 4
            double t8 = ttc.of(request1.getFromLink(), request2.getToLink(), now, false);
            if (now + t6 + t8 + 2 * pickupDurationPerStop <= requestKeyInfoMap.get(request2).getDeadlineDropOff()) {
                double s9 = requestKeyInfoMap.get(request2).getDeadlineDropOff() - now - t6 - t8 - 2 * pickupDurationPerStop;
                ttc.storeInCache(request1.getFromLink(), request2.getToLink(), t8);
                double t3 = ttc.of(request2.getToLink(), request1.getToLink(), now, false);
                if (now + t6 + t8 + t3 + 2 * pickupDurationPerStop + dropoffDurationPerStop <= requestKeyInfoMap.get(request1).getDeadlineDropOff()) {
                    double s10 = requestKeyInfoMap.get(request1).getDeadlineDropOff() - now - t6 - t8 - t3 - 2 * pickupDurationPerStop - dropoffDurationPerStop;
                    ttc.storeInCache(request2.getToLink(), request1.getToLink(), t3);
                    ss4 = getMinValue(s6, s9, s10);
                }
            }
        }
        edgeSlackTime = getMaxValue(ss1, ss2, ss3, ss4);

        return edgeSlackTime;
    }

    private static double getMinValue(double s1, double s2, double s3) {
        return DoubleStream.of(s1, s2, s3).min().getAsDouble();
    }

    private static double getMaxValue(double s1, double s2, double s3, double s4) {
        return DoubleStream.of(s1, s2, s3, s4).max().getAsDouble();
    }
}
