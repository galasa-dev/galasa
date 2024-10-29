/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.ZosBatchJobcard.Typrun;
import dev.galasa.zosbatch.internal.properties.InputClass;
import dev.galasa.zosbatch.internal.properties.MsgClass;
import dev.galasa.zosbatch.internal.properties.MsgLevel;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({InputClass.class, MsgClass.class, MsgLevel.class})
public class TestZosBatchJobcard {
//
//    private static final String IMAGE = "IMAGE";
//
//	private ZosBatchJobcard zosBatchJobcard;
//
//	private ZosBatchJobcard zosBatchJobcardSpy;
//    
//    @Mock
//    IZosImage imageMock;
//    
//    @Before
//    public void setup() throws Exception {
//    	Mockito.when(imageMock.getImageID()).thenReturn(IMAGE);
//        zosBatchJobcard = new ZosBatchJobcard();
//        zosBatchJobcardSpy = PowerMockito.spy(zosBatchJobcard);
//    }
//    
//    @Test
//    public void testInputClass() {
//        String value = "inputClass";
//        Assert.assertEquals("problem with inputClass value", value, zosBatchJobcard.setInputClass(value).getInputClass());
//    }
//    
//    @Test
//    public void testMsgClass() {
//        String value = "msgClass";
//        Assert.assertEquals("problem with msgClass value", value, zosBatchJobcard.setMsgClass(value).getMsgClass());
//    }
//    
//    @Test
//    public void testmsgLevel() {
//        String value = "msgLevel";
//        Assert.assertEquals("problem with msgLevel value", value, zosBatchJobcard.setMsgLevel(value).getMsgLevel());
//    }
//    
//    @Test
//    public void testRegion() {
//        String value = "region";
//        Assert.assertEquals("problem with region value", value, zosBatchJobcard.setRegion(value).getRegion());
//    }
//    
//    @Test
//    public void testMemlimit() {
//        String value = "memlimit";
//        Assert.assertEquals("problem with memlimit value", value, zosBatchJobcard.setMemlimit(value).getMemlimit());
//    }
//    
//    @Test
//    public void testTyprun() {
//        Typrun value = Typrun.COPY;
//        Assert.assertEquals("problem with typrun value", value, zosBatchJobcard.setTyprun(value).getTyprun());
//        value = Typrun.HOLD;
//        Assert.assertEquals("problem with typrun value", value, zosBatchJobcard.setTyprun(value).getTyprun());
//        value = Typrun.JCLHOLD;
//        Assert.assertEquals("problem with typrun value", value, zosBatchJobcard.setTyprun(value).getTyprun());
//        value = Typrun.SCAN;
//        Assert.assertEquals("problem with typrun value", value, zosBatchJobcard.setTyprun(value).getTyprun());
//    }
//    
//    @Test
//    public void testUser() {
//        String value = "user";
//        Assert.assertEquals("problem with user value", value, zosBatchJobcard.setUser(value).getUser());
//    }
//    
//    @Test
//    public void testPassword() {
//        String value = "password";
//        Assert.assertEquals("problem with password value", value, zosBatchJobcard.setPassword(value).getPassword());
//    }
//    
//    @Test
//    public void testCond() {
//        String value = "cond";
//        Assert.assertEquals("problem with cond value", value, zosBatchJobcard.setCond(value).getCond());
//    }
//    
//    @Test
//    public void testTime() {
//        String value = "time";
//        Assert.assertEquals("problem with time value", value, zosBatchJobcard.setTime(value).getTime());
//    }
//    
//    @Test
//    public void testAccount() {
//        String value = "account";
//        Assert.assertEquals("problem with account value", value, zosBatchJobcard.setAccount(value).getAccount());
//    }
//    
//    @Test
//    public void testProgrammerName() {
//        String value = "programmerName";
//        Assert.assertEquals("problem with programmerName value", value, zosBatchJobcard.setProgrammerName(value).getProgrammerName());
//    }
//    
//    @Test
//    public void testJclWithJobcard() throws Exception {
//        String jobname = "jobname";        
//        String account = "account";        
//        String programmerName = "progname";        
//        String inputClass = "inputClass";       
//        String msgClass = "msgClass";     
//        String msgLevel = "msgLevel";        
//        String region = "region";
//        String memlimit = "memlimit";
//        String typrun = "SCAN";
//        String user = "user";
//        String password = "password";
//        String cond = "cond";
//        String time = "time";
//        
//        String jobcard = "//" + jobname + " JOB @@@@,\n" + 
//                         "//         CLASS=" + inputClass + ",\n" + 
//                         "//         MSGCLASS=" + msgClass + ",\n" + 
//                         "//         MSGLEVEL=" + msgLevel + "\n";
//        String expectedJobcard = jobcard.replace("@@@@", "");
//
//        Whitebox.setInternalState(zosBatchJobcardSpy, inputClass, inputClass);
//        Whitebox.setInternalState(zosBatchJobcardSpy, msgClass, msgClass);
//        Whitebox.setInternalState(zosBatchJobcardSpy, msgLevel, msgLevel);
//        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobcardSpy.getJobcard(jobname, imageMock));
//        
//        Whitebox.setInternalState(zosBatchJobcardSpy, account, (String) null);
//        Whitebox.setInternalState(zosBatchJobcardSpy, programmerName, programmerName);
//        expectedJobcard = jobcard.replace("@@@@", ",'" + programmerName + "'");
//        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobcardSpy.getJobcard(jobname, imageMock));
//
//        Whitebox.setInternalState(zosBatchJobcardSpy, account, account);
//        Whitebox.setInternalState(zosBatchJobcardSpy, programmerName, (String) null);
//        Whitebox.setInternalState(zosBatchJobcardSpy, inputClass, inputClass);
//        Whitebox.setInternalState(zosBatchJobcardSpy, msgClass, msgClass);
//        Whitebox.setInternalState(zosBatchJobcardSpy, msgLevel, msgLevel);
//        Whitebox.setInternalState(zosBatchJobcardSpy, region, region);
//        Whitebox.setInternalState(zosBatchJobcardSpy, memlimit, memlimit);
//        Whitebox.setInternalState(zosBatchJobcardSpy, "typrun", ZosBatchJobcard.Typrun.SCAN);
//        Whitebox.setInternalState(zosBatchJobcardSpy, user, user);
//        Whitebox.setInternalState(zosBatchJobcardSpy, password, password);
//        Whitebox.setInternalState(zosBatchJobcardSpy, cond, cond);
//        Whitebox.setInternalState(zosBatchJobcardSpy, time, time);
//        expectedJobcard = jobcard.replace("@@@@", "(" + account + "),\n" +
//                "//         REGION=" + region + ",\n" + 
//                "//         MEMLIMIT=" + memlimit + ",\n" + 
//                "//         TYPRUN=" + typrun + ",\n" +
//                "//         USER=" + user + ",\n" +
//                "//         PASSWORD=" + password + ",\n" + 
//                "//         COND=" + cond + ",\n" + 
//                "//         TIME=" + time);
//        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobcardSpy.getJobcard(jobname, imageMock));
//
//        Whitebox.setInternalState(zosBatchJobcardSpy, account, "(" + account);
//        expectedJobcard = jobcard.replace("@@@@", "(" + account + "),\n" +
//                "//         REGION=" + region + ",\n" + 
//                "//         MEMLIMIT=" + memlimit + ",\n" + 
//                "//         TYPRUN=" + typrun + ",\n" +
//                "//         USER=" + user + ",\n" +
//                "//         PASSWORD=" + password + ",\n" + 
//                "//         COND=" + cond + ",\n" + 
//                "//         TIME=" + time);
//        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobcardSpy.getJobcard(jobname, imageMock));
//
//        Whitebox.setInternalState(zosBatchJobcardSpy, account, account + ")");
//        expectedJobcard = jobcard.replace("@@@@", "(" + account + "),\n" +
//                "//         REGION=" + region + ",\n" + 
//                "//         MEMLIMIT=" + memlimit + ",\n" + 
//                "//         TYPRUN=" + typrun + ",\n" +
//                "//         USER=" + user + ",\n" +
//                "//         PASSWORD=" + password + ",\n" + 
//                "//         COND=" + cond + ",\n" + 
//                "//         TIME=" + time);
//        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobcardSpy.getJobcard(jobname, imageMock));
//        
//        PowerMockito.mockStatic(InputClass.class);
//        PowerMockito.doReturn(inputClass).when(InputClass.class, "get", Mockito.any());
//        Whitebox.setInternalState(zosBatchJobcardSpy, inputClass, (String) null);
//        PowerMockito.mockStatic(MsgClass.class);
//        PowerMockito.doReturn(msgClass).when(MsgClass.class, "get", Mockito.any());
//        Whitebox.setInternalState(zosBatchJobcardSpy, msgClass, (String) null);
//        PowerMockito.mockStatic(MsgLevel.class);
//        PowerMockito.doReturn(msgLevel).when(MsgLevel.class, "get", Mockito.any());
//        Whitebox.setInternalState(zosBatchJobcardSpy, msgLevel, (String) null);
//        expectedJobcard = jobcard.replace("@@@@", "(" + account + "),\n" +
//                "//         REGION=" + region + ",\n" + 
//                "//         MEMLIMIT=" + memlimit + ",\n" + 
//                "//         TYPRUN=" + typrun + ",\n" +
//                "//         USER=" + user + ",\n" +
//                "//         PASSWORD=" + password + ",\n" + 
//                "//         COND=" + cond + ",\n" + 
//                "//         TIME=" + time);
//        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobcardSpy.getJobcard(jobname, imageMock));
//    }
//    
//    @Test
//    public void testNulled() {
//        Assert.assertEquals("problem with nulled()", null, zosBatchJobcard.nulled(null));
//        Assert.assertEquals("problem with nulled()", null, zosBatchJobcard.nulled("  "));
//        Assert.assertEquals("problem with nulled()", "value", zosBatchJobcard.nulled(" value "));
//        Assert.assertEquals("problem with nulled()", "value", zosBatchJobcard.nulled("value"));
//    }
}
