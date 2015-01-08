#!/usr/bin/python
import os, string

print "Counting lines in all Kotlin (*.kt) files:"
codefiles = []

dirinfo = os.walk(os.getcwd())
for item in dirinfo:
    for f in item[2]:
        if f[-2:] == 'kt':
            codefiles.append([item[0],"/",f])

def is_whitespace_only(line):
    for char in line:
        if char not in string.whitespace:
            return False
    return True

lines = 0
code_lines = 0
for f in codefiles:
    this_lines = 0
    this_code_lines = 0
    with open("".join(f)) as fin:
        for line in fin:
            if is_whitespace_only(line):
                this_lines += 1
            else:
                this_lines += 1
                this_code_lines += 1
    lines += this_lines
    code_lines += this_code_lines
    gap = " " * (20 - len(f[2]))
    gap2 = " " * (8 - len(str(this_code_lines)))
    out ="%s%s - SLOC: %d%s(%d)" % (f[2], gap, this_code_lines, gap2, this_lines)
    gap3 = " " * (48 - len(out))
    out = out + "%s%s.kt" % (gap3, f[0])
    print out

output = "Total lines of code: %d    Not counting whitespace lines: %d" % (lines, code_lines)
output_len = len(output)
print "="*output_len
print output
print "="*output_len
