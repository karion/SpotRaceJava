package pl.net.karion.SpotRacer.company.service;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.net.karion.SpotRacer.assignment.exception.SpotAlreadyAssignedException;
import pl.net.karion.SpotRacer.company.api.CompanyRequest;
import pl.net.karion.SpotRacer.company.api.CompanyResponse;
import pl.net.karion.SpotRacer.company.exception.CompanyHaveActiveElements;
import pl.net.karion.SpotRacer.company.exception.CompanyNameCollision;
import pl.net.karion.SpotRacer.company.exception.CompanyNotFound;
import pl.net.karion.SpotRacer.company.model.Company;
import pl.net.karion.SpotRacer.company.model.CompanyRepository;
import pl.net.karion.SpotRacer.reservation.exception.ReservationAlreadyTakenException;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.spot.service.SpotMapper;
import pl.net.karion.SpotRacer.spot.service.SpotSpecifications;

import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(
        CompanyRepository companyRepository
    ) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public CompanyResponse create(CompanyRequest companyRequest) {

        Company company = new Company(
            companyRequest.name(),
            companyRequest.active()
        );

        try {
            Company savedCompany = companyRepository.saveAndFlush(company);
            return CompanyMapper.toResponse(savedCompany);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException cve) {
                if ("uq_company__normalized_name_active".equals(cve.getConstraintName())) {
                    throw new CompanyNameCollision();
                }
            }
            throw ex;
        }
    }

    @Transactional
    public CompanyResponse update(UUID companyId, CompanyRequest companyRequest) {
        Company company = companyRepository.findById(companyId).orElse(null);

        if (company == null) {
            throw new CompanyNotFound();
        }

        company.setName(companyRequest.name());
        company.setActive(companyRequest.active());

        try {
            Company savedCompany = companyRepository.saveAndFlush(company);
            return CompanyMapper.toResponse(savedCompany);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException cve) {
                if ("uq_company__normalized_name_active".equals(cve.getConstraintName())) {
                    throw new CompanyNameCollision();
                }
            }
            throw ex;
        }
    }

    @Transactional
    public void delete(UUID companyId) {
        Company company = companyRepository.findById(companyId).orElse(null);


        if (company == null) {
            throw new CompanyNotFound();
        }

        //@TODO Add check if Company have Spots, Assignments or Reservations
        throw new CompanyHaveActiveElements();
    }

    public CompanyResponse getById(UUID companyId) {
        Company company = companyRepository.findById(companyId).orElse(null);

        if (company == null) {
            throw new CompanyNotFound();
        }

        return CompanyMapper.toResponse(company);
    }

    public Page<CompanyResponse> getAll(String search, Pageable pageable) {
        Specification<Company> spec = Specification
            .where(CompanySpecification.hasName(search))
            ;

        return this.companyRepository.findAll(spec, pageable)
            .map(CompanyMapper::toResponse);
    }
}
