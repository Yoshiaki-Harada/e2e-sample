FROM gradle:jdk16

RUN curl -SsL https://downloads.gauge.org/stable | sh

# Install gauge plugins
RUN gauge install java && \
    gauge install screenshot

ENV PATH=$HOME/.gauge:$PATH
