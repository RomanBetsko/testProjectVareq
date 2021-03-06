package com.ua.stomat.appservices.dao;

import com.ua.stomat.appservices.entity.Appointment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface AppointmentRepository extends CrudRepository<Appointment, Long> {

    List<Appointment> findAll();

    Appointment findByAppointmentId(Integer appointmentId);

    List<Appointment> findByDateFromGreaterThanEqual(Timestamp dateFrom);

    List<Appointment> findByDateFromGreaterThanEqualAndDateFromLessThanEqual(Timestamp startDate, Timestamp endDate);

    List<Appointment> findByDateToGreaterThanEqualAndDateToLessThanEqual(Timestamp startDate, Timestamp endDate);

    List<Appointment> findByClientClientId(Integer clientId);
}
