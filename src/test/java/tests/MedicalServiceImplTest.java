package tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.alert.SendAlertServiceImpl;
import ru.netology.patient.service.medical.MedicalService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

public class MedicalServiceImplTest {
    
    private PatientInfo testPatientInfo;
    private SendAlertService sendAlertService;
    private PatientInfoRepository patientInfoRepository;
    private MedicalService medicalService;
    private BloodPressure bloodPressure;
    private BigDecimal temperature;
    private String id;
    private ByteArrayOutputStream out;
    
    @BeforeEach
    public void init() {
        // given
        id = "3.1416";
        testPatientInfo
                = new PatientInfo(
                        id,
                        "Анатолий", 
                        "Чубайс", 
                        LocalDate.of(1956, 06, 16),
                        new HealthInfo(
                                new BigDecimal("36.6"), 
                                new BloodPressure(120, 80)
                        )
                    );
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        
        sendAlertService = Mockito.spy(SendAlertServiceImpl.class);
        
        patientInfoRepository = Mockito.mock(PatientInfoRepository.class);
        Mockito.when(patientInfoRepository.getById(id))
                .thenReturn(testPatientInfo);
        
        medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);
    }
    
    @AfterEach
    public void after() {
        System.setOut(System.out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    @Test
    public void MedicalServiceImpl_testWhenBloodPressureNotNormal() {
        // given
        bloodPressure = new BloodPressure(190, 105); 
        String expected = String.format("Warning, patient with id: %s, need help\n", id);
        
        // when
        medicalService.checkBloodPressure(id, bloodPressure);
        String result = out.toString();
        
        // then
        //Mockito.verify(sendAlertService).send(argumentCaptor.capture());
        assertEquals(expected, result);
    }
    
    @Test
    public void MedicalServiceImpl_testWhenTemperatureNotNormal() {
        // given
        temperature = new BigDecimal("35.09");
        String expected = String.format("Warning, patient with id: %s, need help\n", id);
        
        // when    
        // Так я и не понял логику работы этого метода. Почему он считает ненормальной
        // только температуру ниже на 1,5 гр., чем нормальная? Обычно температуру
        // измеряют не только мертвецам ))
        medicalService.checkTemperature(id, temperature);
        String result = out.toString();
        
        // then
        assertEquals(expected, result);
    }
    
    @Test
    public void MedicalServiceImpl_testWhenAllRight() {
        //given
        temperature = new BigDecimal("48.78"); // живой, covid-111119, но - помощь не нужна )
        bloodPressure = new BloodPressure(120, 80);
        
        // when
        medicalService.checkBloodPressure(id, bloodPressure);
        medicalService.checkTemperature(id, temperature);
        
        // then
        Mockito.verify(sendAlertService, Mockito.times(0)).send(Mockito.anyString());
    }
}
