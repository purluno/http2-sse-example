package app

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.asFlux
import org.apache.logging.log4j.kotlin.Logging
import org.springframework.http.codec.ServerSentEvent
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import javax.annotation.PreDestroy

@RestController
class MainController {

    companion object : Logging

    private val coroutineJob: Job = Job()

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + coroutineJob)

    /**
     * A channel to send messages.
     */
    private val messageChannel: Channel<String> = Channel()

    /**
     * A [SharedFlow] to broadcast received messages.
     */
    private val messageSharedFlow: SharedFlow<String> = messageChannel.consumeAsFlow().shareIn(coroutineScope, SharingStarted.Eagerly)

    @PreDestroy
    fun stop(): Unit = runBlocking {
        logger.info("MainController: Stopping ...")
        messageChannel.close()
        coroutineJob.cancelAndJoin()
        logger.info("MainController: Stopped.")
    }

    /**
     * Receives a message in the request body(JSON), then broadcasts it to connected clients for server-sent events.
     *
     * Spring WebFlux supports the `suspend` keyword of Kotlin Coroutines.
     */
    @PostMapping("send")
    suspend fun send(@RequestBody requestBody: Map<String, Any>) {
        val message = requestBody["message"] as String
        messageChannel.send(message)
    }

    /**
     * Opens a stream for server-sent events.
     * Messages sent using [send] will be received by the connected client.
     */
    @GetMapping("events")
    fun events(request: ServerHttpRequest): Flux<ServerSentEvent<String>> {
        val connectionId = request.id
        val remoteAddress = request.remoteAddress
        return messageSharedFlow
            .onSubscription {
                logger.debug { "SSE: open: connection id = $connectionId, remote = $remoteAddress" }
                emit("connected")
            }
            .map { ServerSentEvent.builder(it).build() }
            .asFlux()
            .doOnCancel {
                logger.debug { "SSE: closed: connection id = $connectionId, remote = $remoteAddress" }
            }
    }

}
