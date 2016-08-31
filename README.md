scala-etcd [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [ ![Download](https://api.bintray.com/packages/liamhaworth/maven/scala-etcd/images/download.svg) ](https://bintray.com/liamhaworth/maven/scala-etcd/_latestVersion) [![Build Status](https://travis-ci.org/LiamHaworth/scala-etcd.svg?branch=master)](https://travis-ci.org/LiamHaworth/scala-etcd)
==========

A simple scala client library for [etcd]

This library builds upon the Spray HTTP client to implement a asynchronous client for querying Etcd. A majority of API calls for V2 are avaliable but if
one you require doesn't exist, please open an issue, or even better, submit a pull request!

**NOTE:** This repository and library is fork a from the hard work of [Matt Christiansen], this is no attempt to steal their work but instead to build on it and help it grow

Installing the library
======================

This library is housed at Bintray, to get the latest version and help on how to add it to your project, please click on the version tag in the title of the README

**NOTE:** The library is built against both Scala `2.10` and `2.11` for compatability

Usage
=====

To begin using the library please first ensure that you have an implicit [Actor System] defined, if you don't have one, you can create one like so

```Scala
implicit val system = ActorSystem("etcd")
```

The EtcdClient can then automatically retreive the actor system, if you don't want to, or can't, implicitly set the ActorSystem, thats fine, the client supports having the actor system passed to it during construction.

```Scala
val etcdClient = new EtcdClient("http://localhost:4001")(system)
```

For more examples on usage, please see the Wiki.

Authors and Copyright
=====================

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

|               |                                        |
|:--------------|:---------------------------------------|
| **Author**    | Matt Christiansen <matt@nikore.net>    |
| **Author**    | Liam Haworth <liam@haworth.id.au>      |
|               |                                        |
| **Copyright** | (c) 2014-16, Matt Christiansen.        |
| **Copyright** | (c) 2016, Liam Haworth.                |


[Matt Christiansen]: https://github.com/nikore
[etcd]: http://coreos.com/blog/distributed-configuration-with-etcd/
[Actor System]: http://akka.io/
