package com.fengsheng.network

import com.fengsheng.Config
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

object Network {
    fun init() {
        runBlocking {
            listOf(
//                launch(Dispatchers.IO) { initGameNetwork() },
                launch(Dispatchers.IO) { initGameWebSocketNetwork() },
                launch(Dispatchers.IO) { initGmNetwork() },
                if (Config.FileServerPort != 0) launch(Dispatchers.IO) { initFileServer(Config.FileServerPort) } else null
            ).filterNotNull().joinAll()
        }
    }

//    private fun initGameNetwork() {
//        val bossGroup: EventLoopGroup = NioEventLoopGroup()
//        val workerGroup: EventLoopGroup = NioEventLoopGroup()
//        try {
//            val bootstrap = ServerBootstrap()
//            bootstrap.group(bossGroup, workerGroup)
//                .channel(NioServerSocketChannel::class.java)
//                .childHandler(ProtoServerInitializer())
//            val future = bootstrap.bind(Config.ListenPort)
//            future.addListener { channelFuture ->
//                channelFuture.isSuccess || throw channelFuture.cause()
//            }
//            future.channel().closeFuture().sync()
//        } finally {
//            bossGroup.shutdownGracefully()
//            workerGroup.shutdownGracefully()
//        }
//    }

    private fun initGameWebSocketNetwork() {
        val bossGroup: EventLoopGroup = NioEventLoopGroup()
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(WebSocketServerInitializer())
            val future = bootstrap.bind(Config.ListenWebSocketPort)
            future.addListener { channelFuture ->
                channelFuture.isSuccess || throw channelFuture.cause()
            }
            future.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    private fun initGmNetwork() {
        val bossGroup: EventLoopGroup = NioEventLoopGroup()
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(HttpServerInitializer())
            val future = bootstrap.bind(Config.GmListenPort)
            future.addListener { channelFuture ->
                channelFuture.isSuccess || throw channelFuture.cause()
            }
            future.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    private fun initFileServer(port: Int) {
        embeddedServer(Netty, port = port) {
            routing {
                get("/") {
                    val directory = File("files")
                    val files = directory.listFiles()
                    val response = buildString {
                        append("<html><body>")
                        files?.forEach { file ->
                            append("<a href=\"/${file.name}\">${file.name}</a><br/>")
                        }
                        append("</body></html>")
                    }
                    call.respondText(response, ContentType.Text.Html)
                }

                staticFiles("", File("files"))
            }
        }.start(wait = true)
    }
}
