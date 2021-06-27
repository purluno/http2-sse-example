# http2-sse-example

A simple message broadcasting HTTP/2 server application, using Spring Boot WebFlux and server-sent events.

## Development Environment

- Java Platform (JDK 11)
- Kotlin, Kotlin Coroutines
- Spring Boot, Spring WebFlux
- HTML, CSS, JavaScript, Axios

## Run

`gradle run`, `gradle bootRun` or run from IDE.
You can test it on a browser at https://localhost:8443/.

There's [a server certificate chain](src/main/resources/app-combined.p12)
generated using [Minica](https://github.com/jsha/minica) at `PROJECT_ROOT/src/main/resources/app-combined.p12`.
It's for `app` and `localhost` domain.
you may register it to your system or browser as trusted CA or certificate (password = `secret`),
or you can replace it with your own certificate chain.

## Docker

You can run this application using Docker.

```
docker run -it --rm -p 8443:8443 purluno/http2-sse-example
```
