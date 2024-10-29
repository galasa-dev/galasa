//******************************************************
//* DELETE logstreams used by any previous instances
//* of this CICS
//******************************************************
//DELSTR   EXEC PGM=IXCMIAPU
//SYSPRINT DD SYSOUT=A,DCB=RECFM=FBA
//SYSIN    DD *
   DATA TYPE(LOGR) REPORT(NO)
   DELETE LOGSTREAM NAME(++OWNER++.++APPLID++.DFHSECR)
/*
//*
//DELMOD   EXEC PGM=IXCMIAPU
//SYSPRINT DD SYSOUT=A,DCB=RECFM=FBA
//SYSIN    DD *
   DATA TYPE(LOGR) REPORT(NO)
   DELETE LOGSTREAM NAME(++OWNER++.++APPLID++.MODEL)
/*
  SET MAXCC=0
//