#!/system/bin/sh
IPTABLES=iptables
BUSYBOX=busybox
GREP=grep
ECHO=echo
# Try to find busybox
if /data/data/com.googlecode.droidwall.free/app_bin/busybox_g1 --help >/dev/null 2>/dev/null ; then
	BUSYBOX=/data/data/com.googlecode.droidwall.free/app_bin/busybox_g1
	GREP="$BUSYBOX grep"
	ECHO="$BUSYBOX echo"
elif busybox --help >/dev/null 2>/dev/null ; then
	BUSYBOX=busybox
elif /system/xbin/busybox --help >/dev/null 2>/dev/null ; then
	BUSYBOX=/system/xbin/busybox
elif /system/bin/busybox --help >/dev/null 2>/dev/null ; then
	BUSYBOX=/system/bin/busybox
fi
# Try to find grep
if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then
	if $ECHO 1 | $BUSYBOX grep -q 1 >/dev/null 2>/dev/null ; then
		GREP="$BUSYBOX grep"
	fi
	# Grep is absolutely required
	if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then
		$ECHO The grep command is required. DroidWall will not work.
		exit 1
	fi
fi
# Try to find iptables
if /data/data/com.googlecode.droidwall.free/app_bin/iptables_armv5 --version >/dev/null 2>/dev/null ; then
	IPTABLES=/data/data/com.googlecode.droidwall.free/app_bin/iptables_armv5
fi
$IPTABLES -F droidwall
$IPTABLES -F droidwall-reject
$IPTABLES -F droidwall-3g
$IPTABLES -F droidwall-wifi
exit
