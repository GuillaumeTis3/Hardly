package com.guillaume.hardly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guillaume.hardly.model.MessageHL7;

public interface MessageHL7Repository extends JpaRepository<MessageHL7, Long> {

    // Tous les messages d'un patient par IPP
    List<MessageHL7> findByPatientIpp(String ipp);

    // Messages par direction
    List<MessageHL7> findByDirection(String direction);

    // Messages par direction ordonnés par date décroissante
    List<MessageHL7> findByDirectionOrderByDateReceptionDesc(String direction);

    // Tous les messages ordonnés par date décroissante
    List<MessageHL7> findAllByOrderByDateReceptionDesc();

    // Messages d'un patient par direction
    List<MessageHL7> findByPatientIppAndDirection(String ipp, String direction);

}