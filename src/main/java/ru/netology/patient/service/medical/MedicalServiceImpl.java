package ru.netology.patient.service.medical;

import java.math.BigDecimal;

import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;

public class MedicalServiceImpl implements MedicalService {

    private final PatientInfoRepository patientInfoRepository;
    private final SendAlertService alertService;

    public MedicalServiceImpl(PatientInfoRepository patientInfoRepository, SendAlertService alertService) {
        this.patientInfoRepository = patientInfoRepository;
        this.alertService = alertService;
    }

    @Override
    public void checkBloodPressure(String patientId, BloodPressure bloodPressure) {
        PatientInfo patientInfo = getPatientInfo(patientId);
        if (!patientInfo.getHealthInfo().getBloodPressure().equals(bloodPressure)) {
            String message = String.format("Warning, patient with id: %s, need help", patientInfo.getId());
            alertService.send(message);
        }
    }

    @Override
    public void checkTemperature(String patientId, BigDecimal temperature) {
        PatientInfo patientInfo = getPatientInfo(patientId);
        if (patientInfo.getHealthInfo().getNormalTemperature().subtract(new BigDecimal("1.5")).compareTo(temperature) > 0) {
            String message = String.format("Warning, patient with id: %s, need help", patientInfo.getId());
            //System.out.printf("Warning, patient with id: %s, need help", patientInfo.getId());
            alertService.send(message);
        }
    }

    private PatientInfo getPatientInfo(String patientId) {
        PatientInfo patientInfo = patientInfoRepository.getById(patientId);
        if (patientInfo == null) {
            throw new RuntimeException("Patient not found");
        }
        return patientInfo;
    }
}
