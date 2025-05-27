# Kubernetes controller

The Kubernetes controller is responsible for looping (forever) looking for tests which are in the queued state.

When it finds one, it launches a Kubernetes pod to run the test.

## Helm ConfigMap Settings

### `kube_launch_interval_milliseconds`
The number of milliseconds which the test pod scheduler should delay between successive launches of test pods.

See `Settings.kubeLaunchIntervalMillisecs` for the default value.

Why do this ? 

If we don't do this, then all the tests get scheduled on the same node, and the 
node may run out of memory.

We assume that's because the usage statistics on a pod are not synchronized totally at
real-time, but have a lag in which they catch up. Hopefully this delay is greater
than the lag and when we actually schedule the next pod it gets evenly distributed over
the nodes which are available.

This may or may not be necessary if the scheduling policies in the cluster are changed.

If this value needs to be changed, the test launcher pod needs to be restarted for changes to take effect.

Any failure in reading the ConfigMap will be logged and ignored, resulting in the default value being returned.






