package ibm.gse.eda.vaccine.coldchainagent.ut;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ibm.gse.eda.vaccine.coldchainagent.domain.ReeferAggregate;

public class TestReeferAggregate {
    
    @Test
    public void shouldKeepThreeElementsInListFIFO(){
        // 3 elements in the list
        ReeferAggregate firstRecord = new ReeferAggregate(4,10,3);
        firstRecord.updateTemperature(10);
        Assertions.assertEquals(10,firstRecord.getTemperatureList().getFirst());
        firstRecord.updateTemperature(9);
        Assertions.assertEquals(10,firstRecord.getTemperatureList().getFirst());
        firstRecord.updateTemperature(8);
        Assertions.assertEquals(10,firstRecord.getTemperatureList().getFirst());
        firstRecord.updateTemperature(5);
        Assertions.assertEquals(9,firstRecord.getTemperatureList().getFirst());
        firstRecord.updateTemperature(2);
        Assertions.assertEquals(8,firstRecord.getTemperatureList().getFirst());
        Assertions.assertEquals(3,firstRecord.getTemperatureList().size());
        Assertions.assertEquals(2,firstRecord.getTemperatureList().getLast());
    }

    @Test
    public void testRaisingTemperatureAboveThresholdForLongTime(){
        ReeferAggregate firstRecord = new ReeferAggregate(4,10);
        Assertions.assertEquals(10, firstRecord.getMaxTemperature());
        firstRecord.updateTemperature(15);
        Assertions.assertEquals(1, firstRecord.getViolatedTemperatureCount());
        firstRecord.updateTemperature(16);
        Assertions.assertEquals(2, firstRecord.getViolatedTemperatureCount());
        Assertions.assertFalse(firstRecord.hasTooManyViolations());
        firstRecord.updateTemperature(17);
        Assertions.assertEquals(3, firstRecord.getViolatedTemperatureCount());
        firstRecord.updateTemperature(15);
        firstRecord.updateTemperature(15);
        Assertions.assertEquals(5, firstRecord.getViolatedTemperatureCount());
        Assertions.assertTrue(firstRecord.hasTooManyViolations());
    }


    @Test
    public void testRaisingTemperatureAboveThresholdButLessThanDangerous(){
        ReeferAggregate firstRecord = new ReeferAggregate(4,10);
        Assertions.assertEquals(10, firstRecord.getMaxTemperature());
        firstRecord.updateTemperature( 15);
        Assertions.assertEquals(1, firstRecord.getViolatedTemperatureCount());
        firstRecord.updateTemperature(16);
        Assertions.assertEquals(2, firstRecord.getViolatedTemperatureCount());
        Assertions.assertFalse(firstRecord.hasTooManyViolations());
        firstRecord.updateTemperature(17);
        Assertions.assertEquals(3, firstRecord.getViolatedTemperatureCount());
        // until the number of T > max T is less than maxCount then we are safe
        firstRecord.updateTemperature(9);
        firstRecord.updateTemperature(9);
        Assertions.assertEquals(0, firstRecord.getViolatedTemperatureCount());
        Assertions.assertFalse(firstRecord.hasTooManyViolations());
    }


    @Test
    public void testOnceTooManyGoingDoneIsStillBad(){
        ReeferAggregate firstRecord = new ReeferAggregate(4,10);
        firstRecord.updateTemperature(15);
        firstRecord.updateTemperature(16);
        firstRecord.updateTemperature(17);
        firstRecord.updateTemperature(20);
        firstRecord.updateTemperature(21);
        Assertions.assertTrue(firstRecord.hasTooManyViolations());
        firstRecord.updateTemperature(9);
        firstRecord.updateTemperature(9);
        Assertions.assertEquals(0, firstRecord.getViolatedTemperatureCount());
        Assertions.assertTrue(firstRecord.hasTooManyViolations());
        Assertions.assertEquals(21,firstRecord.maxTemperatureRegistered());
    }
}