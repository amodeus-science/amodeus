# cd.ethz.idsc.amodeus <a href="https://travis-ci.org/idsc-frazzoli/amodeus"><img src="https://travis-ci.org/idsc-frazzoli/amodeus.svg?branch=master" alt="Build Status"></a>

Autonomous mobility-on-demand simulation library, version `1.2.6`

## Purpose

This repository is a library that allows the simulation of autonomous mobility-on-demand (AMoD) system including their fleet management algorithms in the multi-agent transportation simulation environment MATSim.

Try it, orchestrate your own fleet of amod-taxis!
To get started, install and run [amod](https://github.com/idsc-frazzoli/amod).
Here is a [visualization](https://www.youtube.com/watch?v=QkFtIQQSHto).

## Features

The code manages the dispatching of autonomous taxis in the MATSim environment.
It provides standard autonomous mobility-on-demand dispatching algorithms and an API to implement and test novel ones.

### Available Dispatching Algorithms

* **Adaptive Real-Time Rebalancing Policy** from *Robotic load balancing for mobility-on-demand systems* by Pavone, M., Smith, S.L., Frazzoli, E. and Rus, D., 2012.
* **Feedforward Fluidic Optimal Rebalancing Policy** from *Robotic load balancing for mobility-on-demand systems* by Pavone, M., Smith, S.L., Frazzoli, E. and Rus, D., 2012.
* **SQM algorithm** from *Fundamental Performance Limits and Efficient Polices for Transportation-On-Demand Systems* by M.Pavone, K.Treleaven, E.Frazzoli, 2010.
* **Demand-supply-balancing dispatching heuristic** from *Large-scale microscopic simulation of taxi services* by Maciejewski, M., and Bischoff J., 2015.

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
        <version>1.2.6</version>
      </dependency>
    </dependencies>

The source code is attached to every release.
