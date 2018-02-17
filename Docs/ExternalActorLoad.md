# External Actor Loader

## What's Exteranl Actor Loader?

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
Get Url: IPAddress:9000/external






