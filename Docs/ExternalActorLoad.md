# External Actor Loader

## What's External Actor Loader?

External Actor Loader is a loader which will load actor in dynamic way to Hydra. The loader will load the actor definition
from jar file which is provided by user. The Actor will be placed under "/user/externalLoader" path of actor system. and the
actor uses a separated dispatcher("external-dispatcher").


## How to use External Actor Loader?

### Register Actor:
Post the body to IPAddress:9000/external
```
{
	"jarAddress":"C:\\temp\\a\\ExternalPackage.jar",
	"address":"127.0.0.1:2553",
	"className": "hydra.cluster.external.actors.TestActor"
}
```

to register TestActor which is defined in ExternalPackage.jar and delpoy to node "127.0.0.1:2553".

### Query Actor:
Post Url: IPAddress:9000/queryext

```
{"address":"127.0.0.1:5531"}
```

to query the external actors registered on node "127.0.0.1:5531"

### Delete Actor:
Delete Url: IPAddress:9000/queryext

```
{
	"actorName":"akka://ClusterSystem/user/externalLoader/TestActor858451434",
	"address":"127.0.0.1:5531"
}

```

to delete the external actor name "TestActor858451434" on node "127.0.0.1:5531"







