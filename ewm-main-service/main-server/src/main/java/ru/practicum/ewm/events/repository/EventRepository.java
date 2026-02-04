package ru.practicum.ewm.events.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.events.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    @Query(value = """
    SELECT *
    FROM events e
    WHERE e.state = 'PUBLISHED'
      AND earth_distance(
            ll_to_earth(e.lat, e.lon),
            ll_to_earth(:lat, :lon)
          ) <= :radius
    """, nativeQuery = true)
    List<Event> findPublishedEventsNear(
            @Param("lat") Double lat,
            @Param("lon") Double lon,
            @Param("radius") Integer radius
    );
}
