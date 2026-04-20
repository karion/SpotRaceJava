package pl.net.karion.SpotRacer.spot.model;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpotRepository extends JpaRepository<Spot, UUID>, JpaSpecificationExecutor<Spot> {
}
