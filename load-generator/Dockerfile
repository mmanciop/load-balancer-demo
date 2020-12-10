FROM debian:buster-slim

ADD run.sh /app/run.sh

RUN apt-get update && \
    apt-get install -y curl

ENTRYPOINT [ "/app/run.sh" ]