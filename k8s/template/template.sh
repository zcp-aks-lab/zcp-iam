#!/bin/bash
# Usage: bash template.sh [FILE]
# *.tpl   Create .yaml files through variable substitutions (bash style).
#         eg. $var or ${var}
# *.tpl2  Create .yaml files through the '##var##' style.
#         If you must keep double-qoute("keep") of content, use this. (like JSON)
#
# A FILE is a script that set or generate variables used by template files.
# A FILE is set as "setenv.sh", if not present.

## load setenv.sh
arg1=${arg1:=setenv.sh}
_setenv=
_env=
_all=

_all=$(set | awk -F'=' '{print "^"$1"="}')
_env=$(cat $arg1 | grep "$_all" | awk -F'=' '{print "^"$1"="}')   # for exist variables, remove lines of assignment
_setenv=$(cat $arg1 | grep -v "$_all")

eval "$_setenv"

## prepare generation
opt=$(echo -e "$(set | grep -v "$_all" | awk -F'=' '{print "^"$1"="}')\n${_env}")
out_dir=${out_dir:=.}
mkdir -p $out_dir 2>/dev/null

# backup
set | grep "$opt" > ${out_dir}/varlables.log
cp $arg1 ${out_dir}

# show variables
echo ">> Variables" && cat ${out_dir}/varlables.log


## convert *.tpl (using eval)
# https://stackoverflow.com/a/42386902
# https://wiki.kldp.org/HOWTO/html/Adv-Bash-Scr-HOWTO/parameter-substitution.html
(ls *.tpl 2>/dev/null) | while read tpl; do
  out=${out_dir}/${tpl%.tpl}.yaml
  eval "echo \"$(cat $tpl)\"" > $out
done

## convert *.tpl2 (using sed)
# https://ops.tips/gists/shell-replacing-variable-by-file-content/
# https://www.linuxquestions.org/questions/programming-9/bash-how-to-list-all-variables-305017/#td_post_1550513
(ls *.tpl2 2>/dev/null) | while read tpl; do
  out=${out_dir}/${tpl%.tpl2}.yaml
  cp $tpl $out

  for var in $(set | grep "$opt" | cut -d'=' -f1); do
    sed -i '' "s|##${var}##|${!var}|g" "$out"
  done
done
