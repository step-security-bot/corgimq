# Corgi Message Queue (CorgiMQ)

[![build](https://github.com/hailuand/corgio-mq/actions/workflows/maven.yaml/badge.svg)](https://github.com/hailuand/corgio-mq/actions/workflows/maven.yaml) [![codecov](https://codecov.io/github/hailuand/corgimq/graph/badge.svg?token=NYQYU42L1U)](https://codecov.io/github/hailuand/corgimq) [![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

![mascot.jpg](mascot.jpg)

A lightweight message queue library built using Java's JDBC API. Similar in spirit to [AWS SQS](https://aws.amazon.com/sqs/)
and [Redis Simple Message Queue](https://github.com/smrchy/rsmq), but entirely on top of your DBMS.

---

### Features
- Lightweight: bring **just your DBMS.** 🚀
- Batteries included: sensible out-of-the-box defaults with a few optional knobs to get you dangerous _fast_. 🔋
- Transactional: shared access of JDBC `Connection` available to provide transactional message handling. 🤝
- Auditable: audit information in the queue captures if, when, and who read a message. 🔎
- Guaranteed **exactly-once delivery** of a message to a reader - if someone's currently reading it, no one else receives it.
- Messages remain in queue until removed.

Complexity will find you - until then, don't go looking for it 🐶   

---

### Index
* [DBMS compatability & testing](#dbms-compatability--testing)
* [Get started](#get-started)
  * [Creating a queue](#creating-a-queue)
  * [Pushing messages](#pushing-messages)
  * [Reading messages](#reading-messages)
* [Configuration](#configuration)
* [References](#references)

---

### DBMS compatability & testing

| DBMS     | Status |
|----------|--------|
| H2       | ✅      |
| MySQL    | ✅      |
| Postgres | ✅      |

---

### Get started
#### Messages
Data meant to be enqueued is done with a `Message`:

```java
String serializedData = // ... data source
Message message = Message.of(serializedData);
```

#### Creating a queue
A message queue is managed by an instance of `MessageQueue`.

```java
MessageQueueConfig messageQueueConfig = MessageQueueConfig.builder()
        .queueName("poneglyphs") // Name of queue, table will have '_q' suffix
        .build();
MessageQueue messageQueue = MessageQueue.of(databaseConfig, messageQueueConfig);
messageQueue.createTableWithSchemaIfNotExists();
```

```
postgres=# \dt mq.*
           List of relations
 Schema |     Name     | Type  |  Owner
--------+--------------+-------+---------
 mq     | poneglyphs_q | table | shanks
(1 row)
```

#### Pushing messages
Once a `MessageQueue` is created, `Message`s can be pushed with `push(List)`:

```java
Message message1 = Message.of("Whole Cake Island");
Message message2 = Message.of("Zou");
messageQueue.push(List.of(message1, message2));
```

Table after pushing messages:

```
postgres=# select * from mq.poneglyphs_q;
                  id                  |        data       |        message_time        | read_count | read_by | processing_time
--------------------------------------+-----------------+----------------------------+------------+---------+-----------------
 9245867e-f7f1-40e4-9142-bb1457aff9ec | Whole Cake Island | 2024-02-27 10:12:57.486346 |          0 |         |
 a174f9d1-d3a9-4583-9396-d3ed575a4ebf | Zou               | 2024-02-27 10:12:57.486346 |          0 |         |
(2 rows)
```

#### Reading messages
Unread `Messages` in the queue are read in ascending `message_time` through an instance of `MessageHandler`:

```java
MessageHandler messageHandler = MessageHandler.of(messageQueue, MessageHandlerConfig.of(1)); // Read one message at a time
Supplier<Connection> connectionSupplier = // ...Code to acquire a connection to database
messageHandler.listen(connectionSupplier, messageBatch -> {
    for (Message message : messageBatch.messages()) {
        System.out.printf("Shanks - we found a road poneglyph at %s!%n", message.data());
    }
    return messageBatch.messages(); // List of Messages to be popped
    });
// "Shanks - we found a road poneglyph at Whole Cake Island!"
```

Table after handler execution:
```
postgres=# select * from mq.poneglyphs_q;
                  id                  |        data       |        message_time        | read_count | read_by  |      processing_time
--------------------------------------+-----------------+----------------------------+------------+----------+----------------------------
 a174f9d1-d3a9-4583-9396-d3ed575a4ebf | Zou               | 2024-02-27 10:12:57.486346 |          0 |          |
 9245867e-f7f1-40e4-9142-bb1457aff9ec | Whole Cake Island | 2024-02-27 10:12:57.486346 |          1 | beckman  | 2024-02-27 10:14:29.911197
(2 rows)
```
After handler execution, returned `Messages` have their `read_count`, `read_by`, and `processing_time` updated, and subsequent 
calls to `listen()` no longer receive them.

---

### ⚙️ Configuration
#### Message queue

🔤 `queueName`

Name of message queue. Each queue has its own table within the `mq` schema, suffixed with `_q`.

#### Message handler

🔢 `messageBatchSize`

Maximum number of `Message`s to serve to a `MessageHandler` in a single batch. 

_Default:_ `10`

---

### References
- https://adriano.fyi/posts/2023-09-24-choose-postgres-queue-technology/
- https://dagster.io/blog/skip-kafka-use-postgres-message-queue
- https://mcfunley.com/choose-boring-technology
