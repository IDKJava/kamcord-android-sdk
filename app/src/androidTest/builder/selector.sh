#!/usr/bin/env bash
echo $EXECUTOR_NUMBER
case $EXECUTOR_NUMBER in
    0)
        #nexus 6
        DEVICE_ID="ZX1G22S7X2" ;;
    1)
        #nexus 4
        DEVICE_ID="047e1d53de4a0dac" ;;
    2)
        #nexus 10
        DEVICE_ID="R32CA07T1VJ" ;;
esac
echo $DEVICE_ID
export DEVICE_ID