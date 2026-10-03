package pl.net.karion.SpotRacer.spot.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpotRepository extends JpaRepository<Spot, UUID>, JpaSpecificationExecutor<Spot> {

    @Override
    @EntityGraph(attributePaths = "location")
    List<Spot> findAll(Sort sort);
}

