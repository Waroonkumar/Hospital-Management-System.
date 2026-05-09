CREATE DATABASE hospital_db1;
USE hospital_db1;

-- Table 1: Doctors (Master Data)
CREATE TABLE Doctor (
    DoctorID VARCHAR(10) PRIMARY KEY,
    DoctorName VARCHAR(50) NOT NULL,
    Specialization VARCHAR(50),
    Experience VARCHAR(20),
    Contact VARCHAR(15)
);

-- Table 2: Appointments (Transaction Data)
-- We create this first so Patient can reference the 'Current' Appointment
CREATE TABLE Appointment (
    AppointmentID VARCHAR(10) PRIMARY KEY,
    PatientID VARCHAR(10),
    DoctorID VARCHAR(10),
    ApptDate DATE,
    Status VARCHAR(20) DEFAULT 'Active',
    FOREIGN KEY (DoctorID) REFERENCES Doctor(DoctorID) ON DELETE SET NULL
);

-- Table 3: Patients (Master Data)
CREATE TABLE Patient (
    PatientID VARCHAR(10) PRIMARY KEY,
    Name VARCHAR(50) NOT NULL,
    Age INT,
    Gender VARCHAR(10),
    Phone VARCHAR(15),
    Disease VARCHAR(50),
    CurrentAppointmentID VARCHAR(10),
    FOREIGN KEY (CurrentAppointmentID) REFERENCES Appointment(AppointmentID) ON DELETE SET NULL
);

-- Add the missing link (Circular reference handled via ALTER)
ALTER TABLE Appointment ADD CONSTRAINT fk_patient 
FOREIGN KEY (PatientID) REFERENCES Patient(PatientID);
USE hospital_db1;

-- Drop the strict requirement on the Patient table
ALTER TABLE Patient DROP FOREIGN KEY patient_ibfk_1;

-- Re-add it as a 'soft' link that allows the Java code to work
ALTER TABLE Patient ADD CONSTRAINT fk_appt 
FOREIGN KEY (CurrentAppointmentID) REFERENCES Appointment(AppointmentID) 
ON DELETE SET NULL;

-- Insert Initial Doctors
INSERT INTO Doctor VALUES ('D01', 'Dr. Hassan', 'Cardiologist', '8 Years', '03331234567');
INSERT INTO Doctor VALUES ('D02', 'Dr. Sana', 'Neurologist', '5 Years', '03451234567');
INSERT INTO Doctor VALUES ('D03', 'Dr. Bilal', 'General Physician', '10 Years', '03007894561');

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE Patient;
TRUNCATE TABLE Appointment;
SET FOREIGN_KEY_CHECKS = 1;

-- Verify Doctors are there
SELECT * FROM Doctor;
SET GLOBAL FOREIGN_KEY_CHECKS = 0;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE Patient;
TRUNCATE TABLE Appointment;
