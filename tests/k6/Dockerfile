FROM grafana/k6:0.54.0 AS k6

FROM node:22.12.0-alpine3.21
COPY --from=k6 /usr/bin/k6 /usr/bin/k6
COPY . /k6
RUN mkdir -p /results && chown 12345:12345 /results
USER 12345:12345
WORKDIR /k6
ENV PORT=8080 K6_RUNNER_RESULTS=/results
EXPOSE 8080
CMD ["node", "/k6/runner/server.mjs"]
