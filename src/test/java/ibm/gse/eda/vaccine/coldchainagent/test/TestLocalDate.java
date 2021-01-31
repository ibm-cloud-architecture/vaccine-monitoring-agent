package ibm.gse.eda.vaccine.coldchainagent.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

public class TestLocalDate {
    
    @Test
    public void testParsing(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd"); 
        System.out.println(DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate ts = LocalDate.parse("2021-01-25",formatter);
        assertEquals(ts.getDayOfMonth(), 25);
        formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd H:mm:ss"); 
        LocalDateTime t = LocalDateTime.parse("2021-01-25 16:25:31",formatter);
        assertEquals(t.getHour(), 16);
        assertEquals(t.getDayOfMonth(), 25);
        System.out.println(t.toString());
        //LocalDate d = LocalDate.
    }
}
