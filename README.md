# cd.ethz.idsc.amodeus <a href="https://travis-ci.org/idsc-frazzoli/amodeus"><img src="https://travis-ci.org/idsc-frazzoli/amodeus.svg?branch=master" alt="Build Status"></a>

Autonomous mobility-on-demand simulation library, version `1.5.9`

## Purpose

This repository is a library that allows the simulation of autonomous mobility-on-demand (AMoD) system including their fleet management algorithms in the multi-agent transportation simulation environment MATSim.

Try it, orchestrate your own fleet of amod-taxis!
To get started, install and run [amod](https://github.com/idsc-frazzoli/amod).
Here is a [visualization](https://www.youtube.com/watch?v=QkFtIQQSHto).

Our website is [amodeus.science](https://www.amodeus.science/).

## Features

The code manages the dispatching of autonomous taxis in the MATSim environment.
It provides standard autonomous mobility-on-demand dispatching algorithms and an API to implement and test novel ones.

### Available Unit Capacity Dispatching Algorithms

* **Adaptive Real-Time Rebalancing Policy** from *Robotic load balancing for mobility-on-demand systems* by Pavone, M., Smith, S.L., Frazzoli, E. and Rus, D., 2012.
* **Feedforward Fluidic Optimal Rebalancing Policy** from *Robotic load balancing for mobility-on-demand systems* by Pavone, M., Smith, S.L., Frazzoli, E. and Rus, D., 2012.
* **Global Bipartite Matching Policy** fromRuch, Claudio, Sebastian Hörl, and Emilio Frazzoli. "Amodeus, a simulation-based testbed for autonomous mobility-on-demand systems." 2018 21st International Conference on Intelligent Transportation Systems (ITSC). IEEE, 2018.
* **SQM algorithm** from *Fundamental Performance Limits and Efficient Polices for Transportation-On-Demand Systems* by M.Pavone, K.Treleaven, E.Frazzoli, 2010.
* **Demand-supply-balancing dispatching heuristic** from *Large-scale microscopic simulation of taxi services* by Maciejewski, M., and Bischoff J., 2015.
* **First Come First Served Strategy with Grid Rebalancing** from *Operations of Shared Autonomous Vehicle
Fleet for Austin, Texas, Market* by Fagnant, D. J., Kockelman, K. M., and Bansal, P., 2015.
* **Feedforward time-varying rebalancing policy** from Spieser, Kevin, Samitha Samaranayake, and Emilio Frazzoli. "Vehicle routing for shared-mobility systems with time-varying demand." American Control Conference (ACC), 2016. IEEE, 2016.


### Available Ride Sharing Dispatching Algorithms
* **Demand-supply-balancing with Beam Extension for Ride Sharing** Demand Supply Balancing heuristic from *Large-scale microscopic simulation of taxi services* by Maciejewski, M., and Bischoff J., 2015 extended with ride sharing if two requests start close to each other and have a similar direction.
* **Dynamic Ride Sharing Strategy** from *Dynamic ride-sharing and optimal fleet sizing for a system of shared autonomous vehicles* by Fagnant, D. J., and Kockelman, K. M., 2015.
* **T-Share** from Ma, Shuo, Yu Zheng, and Ouri Wolfson. "T-share: A large-scale dynamic taxi ridesharing service." Data Engineering (ICDE), 2013 IEEE 29th International Conference on. IEEE, 2013.
* **High-Capacity Algorithm** from Alonso-Mora, Javier, et al. "On-demand high-capacity ride-sharing via dynamic trip-vehicle assignment." Proceedings of the National Academy of Sciences 114.3 (2017): 462-467.

## Gallery

<table><tr>
<td>

![p1t1](https://user-images.githubusercontent.com/4012178/38852194-23c0b602-4219-11e8-90af-ce5c589ddf47.png)

<td>

![p1t4](https://user-images.githubusercontent.com/4012178/38852209-30616834-4219-11e8-81db-41fe71f7599e.png)

<td>

![p1t3](https://user-images.githubusercontent.com/4012178/38852252-4f4d178e-4219-11e8-9634-434200922ed0.png)

<td>

![p1t2](https://user-images.githubusercontent.com/4012178/38852212-3200c8d8-4219-11e8-9dad-eb0aa33e1357.png)

</tr></table>

## Integration

Specify `repository` and `dependency` of the amodeus library in the `pom.xml` file of your maven project:

    <repositories>
      <repository>
        <id>amodeus-mvn-repo</id>
        <url>https://raw.github.com/idsc-frazzoli/amodeus/mvn-repo/</url>
        <snapshots>
          <enabled>true</enabled>
          <updatePolicy>always</updatePolicy>
        </snapshots>
      </repository>
    </repositories>
    
    <dependencies>
      <dependency>
        <groupId>ch.ethz.idsc</groupId>
        <artifactId>amodeus</artifactId>
        <version>1.5.9</version>
      </dependency>
    </dependencies>

The source code is attached to every release.
