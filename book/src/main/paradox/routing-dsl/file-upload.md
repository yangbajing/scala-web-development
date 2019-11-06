# 实战：大文件断点上传、下载和秒传

本章，使用Akka HTTP和Akka Stream做为后端服务，可以很优雅的实现大文件的断点续传。原理其实非常的简单，
前端计算文件的hash（使用sha256），将hash传到后端查询是否有相同文件已上传，若有将返回已上传文件及文件长度（bytes）。
这时候前端就可以知道文件的上传进度，进而判断还需要断点续传的偏移量或者已上传完成（这就是秒传）。

这里有一个设计取舍：客户端对单个文件不做分片，从文件头开始上传。这样的一个好处是可简化服务端的实现，
同时也可以优化服务端对文件的存储 *（同一个文件将一直使用APPEND的方式写入文件，这样可以更高效的利用磁盘IO。同时，
不需要分块合并，若文件很大，生成的大量分块在文件上传完成后再次合并会是一个非常大的资源开销）* 。

## 断点下载

这个怎么说呢？断点下载Akka HTTP原生支持。你只需要使用如下代码：

@@snip [FileRoute.scala](../../../../../foundation/src/main/scala/fileupload/controller/FileRoute.scala) { #downloadRoute }

`FileUtils.getLocalPath(hash)`函数通过对`hash`值（sha256hex）进行计算和拼接，
获取实际文件的本地存储路径再交给Akka HTTP提供的`getFromFile`指令，剩下的工作就交给Akka。

@@snip [FileUtils.scala](../../../../../foundation/src/main/scala/fileupload/util/FileUtils.scala) { #getLocalPath }

我们可以通过向Akka HTTP发起`HEAD`请求来查看支持的HTTP功能，看到在反回的header里有`Accept-Ranges: bytes`，
意思是服务端支持使用字节为单位的范围下载（断点下载功能既基于此实现）。

```
$ curl --head http://localhost:33333/file/download/7d0559e2f7bf42f0c2becc7fbf91b20ca2e7ec373c941fca21314169de9c7ef4
HTTP/1.1 200 OK
Last-Modified: Fri, 28 Dec 2018 14:12:32 GMT
ETag: "132766a7f528d080"
Accept-Ranges: bytes
Server: akka-http/10.1.6
Date: Sat, 29 Dec 2018 02:17:41 GMT
Content-Type: application/octet-stream
Content-Length: 65463496
```

@@@ note 
通过以下sbt task可以启动文件上传示例程序：
```sbtshell
sbt "foundation/runMain fileupload.Main" 
```

看到如下输出代表程序启动成功：
```
......
[info] Done packaging.
[info] Running fileupload.Main 
fileupload.Main$ - startup success, ServerBinding(/127.0.0.1:33333)
```
@@@

## 断点上传

很遗憾，Akka HTTP默认不支持断点上传，这需要自行实现。但是，Akka HTTP做为一个toolkit，足够灵活且强大，实现断点上传功能so easy。

### 断点上传实现

基于常规的代码设计方式，我们需要`Controller`、`Service`，那就先从`Controller`开始：

#### FileRoute#uploadRoute

@@snip [FileRoute.scala](../../../../../foundation/src/main/scala/fileupload/controller/FileRoute.scala) { #uploadRoute }

这里需要注意的一个指令是`withoutSizeLimit`，默认Akka HTTP对请求大小限制比较低，我们可以通过`withoutSizeLimit`
指令取消对单个API的请求大小限制，同时又不影响整个Web服务的大小限制。另外，这里通过`entity(as[Multipart.FormData])`以
**Unmarshaller**的方式获取整个`Multipart.FormData`对象并传入`FileService#handleUpload`函数进行处理。

#### FileService#handleUpload

@@snip [FileServiceImpl.scala](../../../../../foundation/src/main/scala/fileupload/service/FileServiceImpl.scala) { #handleUpload }

`formData.parts`是一个Akka Stream流，类型签名为`Source[Multipart.FormData.BodyPart, Any]`。有关Akka Stream
更详细的资料请参阅[Akka Streams官方文档](https://doc.akka.io/docs/akka/current/stream/index.html?language=scala)。这里，
每个`part`都代表`FormData`的一个字段（对应HTML 5的`FormData`类型，同时前端需要使用
`Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryrP4DAyu31ilqEWmz`方式发起请求）。每个`part.name`
都是用英号逗号分隔的三部分来做为请求的字段名，分别是：`<hash>.<content length>.<start position>`，
这样我们就可以在不加入任何其它字段的情况下告知服务端当前上传文件的sha256hex计算出的hash值、
文件大小（bytes）和上传起始偏移量。

#### FileUtils#uploadFile

文件上传的核心逻辑在`FileUtils#uploadFile`函数：

@@snip [FileUtils.scala](../../../../../foundation/src/main/scala/fileupload/util/FileUtils.scala) { #uploadFile }

`uploadFile`函数根据是否为续传来分别调用`uploadContinue`或`uploadNewFile`函数。首先来看看新文件上传时的代码逻辑：

@@snip [FileUtils.scala](../../../../../foundation/src/main/scala/fileupload/util/FileUtils.scala) { #uploadNewFile }

1. 根据前端是否上传了有效的hash值（sha256hex）来判断是把文件先写入临时文件还是直接写入实际的本地存储位置（根据hash值计算出本地实际的存储位置）。
2. Akka HTTP中，上传的文件以流的方式进入，在此对每个`ByteString`计算并更新sha256值。
3. 在Akka Stream的Sink端，接收流传入的元素并写入本地文件。
4. 文件写入结束后调用`sha.digest()`方法获取已上传文件的sha256值。
5. 根据是否临时文件来判断是否需要将临时文件移动到实际的本地存储路径，通过文件的hash值来计算出实际的本地存储路径。

断点上传时的逻辑其实相对简单，需要注意的是在`(1)`处调用`FileIO.toPath`将流定入本地时需要以`APPEND`模式追加写入到已存在文件。

@@snip [FileUtils.scala](../../../../../foundation/src/main/scala/fileupload/util/FileUtils.scala) { #uploadContinue }

## 秒传

在已实现断点上传功能之上，秒传的实现逻辑就非常清晰了。客户端在调用`file/upload`接口上传文件之前先调用`/file/progress/{hash}`接口判断相同hash值文件的上传情况，再决定下一步处理。

1. 客户端计算文件hash，并以文件hash和文件大小作为参数调用`/file/progress/{hash}`接口
2. 服务端根据上传hash值判断文件是否已上传，若存在返回已上传文档大小（bytes）
3. 客户端收到服务端响应后根据文件是否存在及已存在文件大小判断**秒传**、**断点续传**还是**新上传**
4. **秒传**，返回文件长度与当前准备上传文件长度大小一致
5. **断点续传**，返回文件大小比当前准备上传文件长度小
6. **新上传**，返回文件不存在
7. 其它情况，作为新文件上传

秒传的代码逻辑台下：

@@snip [FileRoute.scala](../../../../../foundation/src/main/scala/fileupload/controller/FileRoute.scala) { #uploadRoute }

文件上传进度服务实现如下。

@@snip [FileServiceImpl.scala](../../../../../foundation/src/main/scala/fileupload/service/FileServiceImpl.scala) { #progressByHash }

## 运行示例

通过以下sbt task可以启动文件上传示例程序：`sbt "foundation/runMain fileupload.Main"`。启动示例程序后，
打开浏览器输入地址：[http://localhost:33333/file-upload/upload.html](http://localhost:33333/file-upload/upload.html) 
可访问大文件上传示例页面。

## 小结

本文以Akka HTTP和HTML 5讲述了怎样实现一个支持大文件断点上传、下载和秒传的示例应用程序。
