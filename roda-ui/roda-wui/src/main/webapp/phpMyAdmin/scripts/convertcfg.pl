#!/usr/bin/perl
#
# $Id: convertcfg.pl 5152 2003-11-18 15:20:45Z nijel $
#
# Configuration converter
# Converts from old-style (Pre-2.3) configuration files to new format found in PMA-2.3
#
# Takes input from STDIN, sends output to STDOUT
#
# By Robin Johnson robbat2@users.sourceforge.net
# Many thanks to Patrick Lougheed pat@tfsb.org
#

while(<>) 
{	s/\$cfg(\w+)/\$cfg\[\'$1\'\]/g; 
	print; 
	}


