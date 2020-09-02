package ibm.gse.eda.vaccine.coldchainagent.test;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ibm.gse.eda.vaccine.coldchainagent.domain.ReeferAggregate;
import ibm.gse.eda.vaccine.coldchainagent.domain.Telemetry;

public class TestReeferAggregate {
    
    @Test
    public void testLastTemperatureRecords(){
        // 3 elements in the list
        ReeferAggregate firstRecord = new ReeferAggregate(4,3,10);
        firstRecord.update("reeferID", 10);
        Assertions.assertEquals(10,firstRecord.getTemperatureList().getFirst());
        firstRecord.update("reeferID", 9);
        Assertions.assertEquals(10,firstRecord.getTemperatureList().getFirst());
        firstRecord.update("reeferID", 7);
        Assertions.assertEquals(10,firstRecord.getTemperatureList().getFirst());
        firstRecord.update("reeferID", 5);
        Assertions.assertEquals(9,firstRecord.getTemperatureList().getFirst());
        firstRecord.update("reeferID", 2);
        Assertions.assertEquals(7,firstRecord.getTemperatureList().getFirst());
        Assertions.assertEquals(3,firstRecord.getTemperatureList().size());
        Assertions.assertEquals(2,firstRecord.getTemperatureList().getLast());
    }

    @Test
    public void testRaisingTemperatureAboveThresholdForLongTime(){
        ReeferAggregate firstRecord = new ReeferAggregate(4,10);
        Assertions.assertEquals(10, firstRecord.getMaxTemperature());
        firstRecord.update("reeferID", 15);
        Assertions.assertEquals(1, firstRecord.getViolatedTemperatureCount());
        firstRecord.update("reeferID", 16);
        Assertions.assertEquals(2, firstRecord.getViolatedTemperatureCount());
        Assertions.assertFalse(firstRecord.hasTooManyViolations());
        firstRecord.update("reeferID", 17);
        Assertions.assertEquals(3, firstRecord.getViolatedTemperatureCount());
        firstRecord.update("reeferID", 15);
        firstRecord.update("reeferID", 15);
        Assertions.assertEquals(5, firstRecord.getViolatedTemperatureCount());
        Assertions.assertTrue(firstRecord.hasTooManyViolations());
    }


    @Test
    public void testRaisingTemperatureAboveThresholdButLessThanDangerous(){
        ReeferAggregate firstRecord = new ReeferAggregate(4,10);
        Assertions.assertEquals(10, firstRecord.getMaxTemperature());
        firstRecord.update("reeferID", 15);
        Assertions.assertEquals(1, firstRecord.getViolatedTemperatureCount());
        firstRecord.update("reeferID", 16);
        Assertions.assertEquals(2, firstRecord.getViolatedTemperatureCount());
        Assertions.assertFalse(firstRecord.hasTooManyViolations());
        firstRecord.update("reeferID", 17);
        Assertions.assertEquals(3, firstRecord.getViolatedTemperatureCount());
        // until the number of T > max T is less than maxCount then we are safe
        firstRecord.update("reeferID", 9);
        firstRecord.update("reeferID", 9);
        Assertions.assertEquals(0, firstRecord.getViolatedTemperatureCount());
        Assertions.assertFalse(firstRecord.hasTooManyViolations());
    }


    @Test
    public void testOnceTooManyGoingDoneIsStillBad(){
        ReeferAggregate firstRecord = new ReeferAggregate(4,10);
        firstRecord.update("reeferID", 15);
        firstRecord.update("reeferID", 16);
        firstRecord.update("reeferID", 17);
        firstRecord.update("reeferID", 20);
        firstRecord.update("reeferID", 21);
        Assertions.assertTrue(firstRecord.hasTooManyViolations());
        firstRecord.update("reeferID", 9);
        firstRecord.update("reeferID", 9);
        Assertions.assertEquals(0, firstRecord.getViolatedTemperatureCount());
        Assertions.assertTrue(firstRecord.hasTooManyViolations());
        Assertions.assertEquals(21,firstRecord.maxTemperatureRegistered());
    }
}