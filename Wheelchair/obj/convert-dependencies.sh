#!/bin/sh
# AUTO-GENERATED FILE, DO NOT EDIT!
if [ -f $1.org ]; then
  sed -e 's!^D:/Java/cygwin64/lib!/usr/lib!ig;s! D:/Java/cygwin64/lib! /usr/lib!ig;s!^D:/Java/cygwin64/bin!/usr/bin!ig;s! D:/Java/cygwin64/bin! /usr/bin!ig;s!^D:/Java/cygwin64/!/!ig;s! D:/Java/cygwin64/! /!ig;s!^I:!/cygdrive/i!ig;s! I:! /cygdrive/i!ig;s!^H:!/cygdrive/h!ig;s! H:! /cygdrive/h!ig;s!^G:!/cygdrive/g!ig;s! G:! /cygdrive/g!ig;s!^E:!/cygdrive/e!ig;s! E:! /cygdrive/e!ig;s!^D:!/cygdrive/d!ig;s! D:! /cygdrive/d!ig;s!^C:!/cygdrive/c!ig;s! C:! /cygdrive/c!ig;' $1.org > $1 && rm -f $1.org
fi
