#!/bin/bash

# Usage: bash template.sh [FILE]
# Create .yaml files through each template(.tpl) that has variable substitutions(bash style).
# eg. $var or ${var}
#
# A FILE is a script that set or generate variables used by template files.
# A FILE is set as "setenv.sh", if not present.

. ${1:-setenv.sh}
out_dir=${out_dir:-.}
mkdir $out_dir 2>/dev/null

# https://stackoverflow.com/a/42386902
# https://wiki.kldp.org/HOWTO/html/Adv-Bash-Scr-HOWTO/parameter-substitution.html
ls *.tpl | while read tpl; do
  out=${tpl%.tpl}.yaml
  eval "echo \"$(cat $tpl)\"" > ${out_dir:-.}/$out
done
