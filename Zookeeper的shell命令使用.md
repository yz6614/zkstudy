# Zookeeper的shell命令使用

## 1、help(查看帮助)

```
[zk: localhost:2181(CONNECTED) 0] help

ZooKeeper -server host:port cmd args
    stat path [watch]
    set path data [version]
    ls path [watch]
    delquota [-n|-b] path
    ls2 path [watch]
    setAcl path acl
    setquota -n|-b val path
    history 
    redo cmdno
    printwatches on|off
    delete path [version]
    sync path
    listquota path
    rmr path
    get path [watch]
    create [-s] [-e] path data acl
    addauth scheme auth
    quit 
    getAcl path
    close 
    connect host:port
```

## 2、connect host:port(在客户端命令行连接其他节点)

```
[zk: localhost:2181(CONNECTED) 1] connect xinsight6:2181
[zk: xinsight6:2181(CONNECTED) 1]
```

## 3、create(创建节点)

```
# 默认创建persistent、非sequential节点(断开连接不删除、不自动编号)
# 创建一个新的Znode节点"zk1"，以及和它相关字符'zk1'
[zk: localhost:2181(CONNECTED) 1] create /zk1 'zk1'
Created /zk1

# ls 查看该节点中的文件或目录，[]代表其中没有文件或子目录
[zk: localhost:2181(CONNECTED) 2] ls /zk1
[]

# 创建子节点
[zk: localhost:2181(CONNECTED) 3] create /zk1/zk1-1 'zk1-1'
Created /zk1/zk1-1

# 创建ephemeral节点(断开连接自动删除,不带编号的临时节点)
[zk: localhost:2181(CONNECTED) 2] create -e /zk2 "zk2"
Created /zk2

# 创建sequential节点(自动按顺序编号，带编号的持久性节点)
[zk: localhost:2181(CONNECTED) 4] create -s /mynode "mynode" 
Created /mynode0000000002

# 创建ephemeral节点(带编号的临时节点)
[zk: localhost:2181(CONNECTED) 3] create -s -e /zk3 "zk3"
Created /zk30000000003

```

## 4、ls(查看节点)和stat(查看znode状态)

```
# ls / 查看节点列表
[zk: localhost:2181(CONNECTED) 4] ls /
[zk1, zk20000000002, zk3]
# ls2 查看节点详细信息
[zk: localhost:2181(CONNECTED) 5] ls2 /zk1
[zk1-1] # 节点数据
cZxid = 0x400000011 # 节点创建的时候的zxid
ctime = Mon Dec 18 20:37:32 CST 2017 # 节点创建的时间
mZxid = 0x400000011 # 节点修改的时候的zxid，与子节点的修改无关
mtime = Mon Dec 18 20:37:32 CST 2017 # 节点修改的时间
pZxid = 0x400000012 # 子节点的创建/删除对应的 zxid，和修改无关，和孙子节点无关
cversion = 1 # 子节点的更新次数
dataVersion = 0 # 节点数据的更新次数
aclVersion = 0 # 节点(ACL)的更新次数
ephemeralOwner = 0x0 # 值表示与该节点绑定的 session id. 如果该节点不是 ephemeral 节点, ephemeralOwner 值为0
dataLength = 3 # 节点数据的字节数
numChildren = 1 # 子节点个数，不包含孙子节点
```

说明：

- zxid：zookeeper集群内部在工作的时候(选举、原子广播等事务操作)所使用的一个全局的分布式事务的编号，是一个64位的长整型数
- zxid分成两部分：  高32位表示当前的leader关系是否改变。低32位表示当前这个leader领导期间的所有事务编号。每更换一个leader，低32位就从0开始再计数，高32位加1

| **状态属性**   | **说明**                                                     |
| -------------- | ------------------------------------------------------------ |
| cZxid          | 数据节点创建时的事务ID                                       |
| ctime          | 数据节点创建时的时间                                         |
| mZxid          | 数据节点最后一次更新时的事务ID                               |
| mtime          | 数据节点最后一次更新时的时间                                 |
| pZxid          | 数据节点的子节点列表最后一次被修改（是子节点列表变更，而不是子节点内容变更）时的事务ID。这里的pZxid为`pZxid = 0x400000012`,说明zk1下的子节点zk1-1的cZxid也0x400000012，因为我们还没有对zk1-1进行其他事务操作，所以pZxid就是zk1-1的创建时的事务编号 |
| cversion       | 子节点的版本号                                               |
| dataVersion    | 数据节点的版本号                                             |
| aclVersion     | 数据节点的ACL版本号                                          |
| ephemeralOwner | 如果节点是临时节点，则表示创建该节点的会话的SessionID；如果节点是持久节点，则该属性值为0 |
| dataLength     | 数据内容的长度                                               |
| numChildren    | 数据节点当前的子节点个数                                     |

```
# stat：查看znode状态
[zk: localhost:2181(CONNECTED) 8] stat /zk1/zk1-1
cZxid = 0x400000012
ctime = Mon Dec 18 20:38:42 CST 2017
mZxid = 0x400000012
mtime = Mon Dec 18 20:38:42 CST 2017
pZxid = 0x400000012
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 5
numChildren = 0
```

## 5、get(获取znode数据)和set(设置znode数据)

```
[zk: localhost:2181(CONNECTED) 9] set /zk1/zk1-1 'zk1-1-version2'
[zk: localhost:2181(CONNECTED) 10] get /zk1/zk1-1
zk1-1-version2
```

## 6、delete(只能删除一个znode)、rmr(级联删除)

```
[zk: localhost:2181(CONNECTED) 11] delete /zk1
Node not empty: /zk1
[zk: localhost:2181(CONNECTED) 12] delete /zk1/zk1-1
[zk: localhost:2181(CONNECTED) 13] rmr /zk1
[zk: localhost:2181(CONNECTED) 14] ls /
[zookeeper]
```

## 7、监听

```
### (1)"NodeDataChanged"事件
[zk: localhost:2181(CONNECTED) 15] create /jed "jed"
[zk: localhost:2181(CONNECTED) 16] get /jed
jed

# 给/jed节点添加"数据变化"的监听事件
[zk: localhost:2181(CONNECTED) 17] get /jed watch

# 并在hadoop02中修改/jed的数据
[zk: localhost:2181(CONNECTED) 2] 


# hadoop01中监听到/jed节点数据变化，并打印提示信息
[zk: localhost:2181(CONNECTED) 18] 
WATCHER::

WatchedEvent state:SyncConnected type:NodeDataChanged path:/jed

### (2)"NodeChildrenChanged"事件
[zk: localhost:2181(CONNECTED) 19] ls /jed watch
[zk: localhost:2181(CONNECTED) 3] create /jed/jed1 "jed1"
[zk: localhost:2181(CONNECTED) 20] 
WATCHER::

WatchedEvent state:SyncConnected type:NodeChildrenChanged path:/jed4

### (3)"NodeDeleted"事件
[zk: localhost:2181(CONNECTED) 28] stat /jed watch
[zk: localhost:2181(CONNECTED) 4] rmr /jed
[zk: localhost:2181(CONNECTED) 30] 
WATCHER::

WatchedEvent state:SyncConnected type:NodeDeleted path:/jed

### (4)"NodeCreated"事件
# zookeeper cli 方式无法进行监听
```

## 8、close(关闭当前session)

```
[zk: localhost:2181(CONNECTED) 20] close
2019-07-16 16:17:41,879 [myid:] - INFO  [main:ZooKeeper@684] - Session: 0x36bb06c27316d1f closed
[zk: localhost:2181(CLOSED) 21] 2019-07-16 16:17:41,879 [myid:] - INFO  [main-EventThread:ClientCnxn$EventThread@512] - EventThread shut down
```

## 9、quit(退出客户端命令行)

```
[zk: xinsight6:2181(CONNECTED) 1] quit
Quitting...
2019-07-16 16:25:15,463 [myid:] - INFO  [main:ZooKeeper@684] - Session: 0x36bb06c273172e4 closed
2019-07-16 16:25:15,463 [myid:] - INFO  [main-EventThread:ClientCnxn$EventThread@512] - EventThread shut down
```

## 