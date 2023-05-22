#!/system/bin/sh
DAEMON_PID=$(pgrep -f CuPerfMonitor)
kill $DAEMON_PID