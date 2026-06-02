package com.weatherandgo.backend.repository;

import com.weatherandgo.backend.entity.WeatherProviderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherProviderLogRepository extends JpaRepository<WeatherProviderLog, Long> {

    List<WeatherProviderLog> findTop20ByOrderByCreatedAtDesc();

}