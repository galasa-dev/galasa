/*rexx*/
null = msg('off')
parse upper arg dsn
listdsiRc = listdsi("'"dsn"'" directory smsinfo)
json.1  = '{"listdsirc":"'    || listdsiRc    || '",'
json.2  = ' "sysreason":"'    || sysreason    || '",'
json.3  = ' "sysdsname":"'    || sysdsname    || '",'
json.4  = ' "sysvolume":"'    || sysvolume    || '",'
json.5  = ' "sysunit":"'      || sysunit      || '",'
json.6  = ' "sysdsorg":"'     || sysdsorg     || '",'
json.7  = ' "sysrecfm":"'     || sysrecfm     || '",'
json.8  = ' "syslrecl":"'     || syslrecl     || '",'
json.9  = ' "sysblksize":"'   || sysblksize   || '",'
json.10 = ' "syskeylen":"'    || syskeylen    || '",'
json.11 = ' "sysalloc":"'     || sysalloc     || '",'
json.12 = ' "sysused":"'      || sysused      || '",'
json.13 = ' "sysusedpages":"' || sysusedpages || '",'
json.14 = ' "sysprimary":"'   || sysprimary   || '",'
json.15 = ' "sysseconds":"'   || sysseconds   || '",'
json.16 = ' "sysunits":"'     || sysunits     || '",'
json.17 = ' "sysextents":"'   || sysextents   || '",'
json.18 = ' "syscreate":"'    || syscreate    || '",'
json.19 = ' "sysrefdate":"'   || sysrefdate   || '",'
json.20 = ' "sysexdate":"'    || sysexdate    || '",'
json.21 = ' "syspassword":"'  || syspassword  || '",'
json.22 = ' "sysracfa":"'     || sysracfa     || '",'
json.23 = ' "sysupdated":"'   || sysupdated   || '",'
json.24 = ' "systrkscyl":"'   || systrkscyl   || '",'
json.25 = ' "sysblkstrk":"'   || sysblkstrk   || '",'
json.26 = ' "sysadirblk":"'   || sysadirblk   || '",'
json.27 = ' "sysudirblk":"'   || sysudirblk   || '",'
json.28 = ' "sysmembers":"'   || sysmembers   || '",'
json.29 = ' "sysdssms":"'     || sysdssms     || '",'
json.30 = ' "sysdataclass":"' || sysdataclass || '",'
json.31 = ' "sysstorclass":"' || sysstorclass || '",'
json.32 = ' "sysmgmtclass":"' || sysmgmtclass || '",'
json.33 = ' "sysmsglvl1":"'   || sysmsglvl1   || '",'
json.34 = ' "sysmsglvl2":"'   || sysmsglvl2   || '"}'
json.0 = 34
parse source with . . . execDsn .
address TSO "ALLOC DD(JSON) DSN('" || execDsn || "(JSON)') SHR"
address TSO "EXECIO * DISKW JSON (STEM json. FINIS)"
exit listdsiRc
