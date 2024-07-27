# Hydra

[![Build Status](https://travis-ci.org/wherby/Hydra.svg?branch=master)](https://travis-ci.org/wherby/Hydra)

## What's Hydra?

Hydra is an Akka Cluster based system which provides high available container service for apps.

## Why is Hydra?

Hydra uses gossip protocol to detect the node failure and will redeploy the apps on the node to another node.
The failure detection by gossip protocol is very efficient. For more information see the links: 

  [Cluster Specification](https://doc.akka.io/docs/akka/current/scala/common/cluster.html)

  [Cluster Usage](https://doc.akka.io/docs/akka/current/scala/cluster-usage.html)
  
Within Hydra, you don't need to know anything about Akka Cluster and get the high available container framework.

## How Hydra detects node failure?

Hydra is based on Akka Cluster, Hydra will use Akka node to hold containers of apps. If the node failed, the Akka gossip
protocol will quickly detect the failure and report to Hydra to do further work.

## About the repository

The repository is copyed from https://bitbucket.org/whereby/hydra which is developing repository on bitbucket.


## User defined Classes

There are two classes user need to rewrite to meet their needs:

 1. [Container](./Docs/Container.md)  
 2. [Scheduler](./Docs/Scheduler.md)
 3. [External Actor Load](./Docs/ExternalActorLoad.md)

## About release:

 [0.1.0](https://github.com/wherby/HydraRelease/tree/master/0.1.0)
 
 [0.2.0](https://github.com/wherby/HydraRelease/tree/master/0.2.0)

 [0.2.1](https://github.com/wherby/HydraRelease/tree/master/0.2.1)


## Download from sonatype:

[sonatype repos](https://oss.sonatype.org/content/groups/staging/io/github/wherby/)


## Gitter about Hydra

[gitter Lobby](https://gitter.im/wherby/Lobby)

## More about Hydra

[Hydra tags](https://wherby.github.io/tags/#hydra)



## License and Terms of Use

### License Agreement

Effective July 25, 2024, the library is licensed under the AGPL-v3. Non-commercial use requires adherence to the current license terms. 
Commercial use necessitates explicit authorization from the author. All library implementations must comply with the latest license within three months of this date.

### Terms of Use for Commercial Exploitation of Library Content

#### Commercial Use and Copyright

Any commercial use of library content without explicit written consent from the author constitutes a breach of copyright.
Such unauthorized use obligates the user to contribute 1% of the commercial income derived from the use of the library to the author.


#### Grace Period for License Transition

Existing commercial users of previous library versions have a three-month grace period to transition.
After this period, users must either cease commercial use or comply with the latest license.

