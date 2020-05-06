How to reproduce the issue:
- Build server docker image using: `./sbt akka/docker:publishLocal`
- Run server and traefik using: `docker-compose up`
- Run akka-grpc or gatling client using (both use netty):
  - akka-grpc: `./sbt akka/runMain com.example.helloworld.GreeterClient`
  - gatling: `./sbt gatling/run`

Once the client exits, check the server logs:
- if you keep seeing logs like `server_1 | HelloReply(Traefik-85-2,UnknownFieldSet(Map()))` being printed, it means a connection is still up when the client is gone.
- otherwise everything is fine, run the client again a couple times to reproduce the issue

