#!/bin/sh

export PATH=$GAUGE_ROOT/bin:$PATH
gauge --env ci-$1 specs/
