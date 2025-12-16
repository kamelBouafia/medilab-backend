package com.medilab.service;

import com.medilab.repository.LabRepository;
import com.medilab.repository.LabTestRepository;
import com.medilab.repository.PatientRepository;
import com.medilab.repository.StaffUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final LabRepository labRepo;
    private final StaffUserRepository staffRepo;
    private final PatientRepository patientRepo;
    private final LabTestRepository testRepo;

    public DatabaseSeeder(LabRepository labRepo, StaffUserRepository staffRepo, PatientRepository patientRepo,
            LabTestRepository testRepo) {
        this.labRepo = labRepo;
        this.staffRepo = staffRepo;
        this.patientRepo = patientRepo;
        this.testRepo = testRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        // if (labRepo.count() > 0) return;
        //
        // Lab lab = Lab.builder().id("labA").name("Demo Lab A").build();
        // labRepo.save(lab);
        //
        // StaffUser manager = StaffUser.builder().id("S001").name("Alice
        // Manager").role(StaffUser.Role.Manager).labId("labA").build();
        // StaffUser tech = StaffUser.builder().id("S002").name("Bob
        // Technician").role(StaffUser.Role.Technician).labId("labA").build();
        // staffRepo.saveAll(List.of(manager, tech));
        //
        // Patient p = Patient.builder().id("P001").name("John
        // Doe").dob(LocalDate.of(1990,1,1)).gender(Patient.Gender.Male).contact("+000").createdById("S001").labId("labA").build();
        // patientRepo.save(p);
        //
        // LabTest t1 = LabTest.builder().id("T01").name("Complete Blood Count
        // (CBC)").category("Hematology").labId("labA").build();
        // LabTest t2 = LabTest.builder().id("T02").name("Basic Metabolic Panel
        // (BMP)").category("Chemistry").labId("labA").build();
        // testRepo.saveAll(List.of(t1, t2));
    }
}
