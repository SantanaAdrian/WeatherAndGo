package com.weatherandgo.backend.repository;

import com.weatherandgo.backend.entity.WeatherQueryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherQueryLogRepository extends JpaRepository<WeatherQueryLog, Long> {

    List<WeatherQueryLog> findTop10ByOrderByCreatedAtDesc();

}