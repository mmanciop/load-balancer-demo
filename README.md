# NGINX unfair load-balancing demo

## Prerequisites

A `docker-compose` installation running on your machine. This demo has been created and tested on Mac OS X with `docker-compose` and `docker-machine`.

## Configure

Create a `.env` file in the root of the checked-out version of this repository and enter the following text, with the values adjusted as necessary:

```text
agent_key=<TODO FILL UP>
agent_endpoint=<local ip or remote host; e.g., saas-us-west-2.instana.io>
agent_endpoint_port=<443 already set as default; or 4443 for local>
agent_zone=<name of the zone for the agent; default: envoy-tracing-demo>
```

## Setup

Pull the latest static agent image:

```sh
grep agent_key .env | sed 's/^[^=]*=//' | docker login containers.instana.io -u _ --password-stdin
docker pull containers.instana.io/instana/release/agent/static:latest
```

Build and run the demo:

```sh
(cd server-app && ./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=server-app)
docker-compose build
docker-compose up
```
