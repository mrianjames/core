# core
Core utilities and framework for low latency applications.

This library is a framework for building multi-threaded, componentised applications. It contains:
* Core container framwork for composing simple threadsafe components and tasks.
* Dispatchers that perform the mechanics of concurrency to simplify your codebase and massively reduce contention and complexity.
* Utilities that perform a variety of useful tasks:
** Timestamps to cope with variety of precision of timestamp. 
** Timestamped file parsing
** Object pooling - efficient and flexible classes to help you reduce object creation/removal overhead.

In many cases the routines presented are similar to other open-source initiatives but they differentiate themselves by having a focus on performance at the expense of the "generic scope". For example, there are plenty of other open source object pooling frameworks out there. However under concurrent load they tend to become bottlenecks and massively impact overall latency or throughput. 
