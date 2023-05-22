#!/system/bin/sh
BASE_DIR=$(dirname "$0")
if [ ! -d ${BASE_DIR}/records ] ; then
  mkdir -p ${BASE_DIR}/records
fi
TIME_STAMP=$(date '+%Y-%m-%d-%H-%M')
${BASE_DIR}/perf_monitor -R ${BASE_DIR}/config.json ${BASE_DIR}/records/record-${TIME_STAMP}.csv