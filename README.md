# cd.ethz.idsc.amodeus <a href="https://travis-ci.org/idsc-frazzoli/amodeus"><img src="https://travis-ci.org/idsc-frazzoli/amodeus.svg?branch=master" alt="Build Status"></a>

Autonomous mobility-on-demand simulation library, version `1.0.0`

## Purpose

This repository is a library that allows the simulation of autonomous mobility-on-demand (AMoD) system including their fleet management algorithms in the multi-agent transportation simulation environment MATSim.

Try it, orchestrate your own fleet of amod-taxis!

## Features

The code manages the dispatching of autonomous taxis in the MATSim environment. It provides standard autonomous mobility-on-demand dispatching algorithms and a an API to implement and test novel ones.

## Gallery

<table>
<tr>
<td>

![usecase_amodeus](https://user-images.githubusercontent.com/4012178/35968174-668b6e54-0cc3-11e8-9c1b-a3e011fa0600.png)

Zurich

<td>

![San Francisco](https://user-images.githubusercontent.com/4012178/37365948-4ab45794-26ff-11e8-8e2d-ceb1b526e962.png)

San Francisco

</table>

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
        <version>1.0.0</version>
      </dependency>
    </dependencies>

The source code is attached to every release.
