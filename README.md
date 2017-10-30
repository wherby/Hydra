# Hydra

[![Build Status](https://travis-ci.org/wherby/Hydra.svg?branch=master)](https://travis-ci.org/wherby/Hydra)

## What's Hydra?

Hydra is an Akka Cluster based system which provides high available service for apps in container.

## Why is Hydra?

Hydra uses gossip protocol to detect the node failure and will redeploy the apps on the node to another node.
The failure detection by gossip protocol is very efficient. For more information see the links: 

  [Cluster Specification](https://doc.akka.io/docs/akka/current/scala/common/cluster.html)

  [Cluster Usage](https://doc.akka.io/docs/akka/current/scala/cluster-usage.html)
  
Within Hydra, you don't need to know anything about Akka Cluster and get the high available container framework.



## About the repository

The repository is copyed from https://bitbucket.org/whereby/hydra which is developing repository on bitbucket.


## User defined Classes

There are two classes user need to rewrite to meet their needs:

 1. [Container](./Docs/Container.md)  
 2. [Scheduler](./Docs/Scheduler.md)




