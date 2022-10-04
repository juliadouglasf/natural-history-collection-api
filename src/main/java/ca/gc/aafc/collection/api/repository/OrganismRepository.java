package ca.gc.aafc.collection.api.repository;

import ca.gc.aafc.collection.api.dto.OrganismDto;
import ca.gc.aafc.collection.api.entities.Organism;
import ca.gc.aafc.collection.api.service.OrganismService;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.repository.external.ExternalResourceProvider;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.DinaAuthorizationService;
import ca.gc.aafc.dina.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrganismRepository extends DinaRepository<OrganismDto, Organism> {

  private final DinaAuthenticatedUser dinaAuthenticatedUser;

  public OrganismRepository(
          @NonNull OrganismService dinaService,
          ExternalResourceProvider externalResourceProvider,
          DinaAuthorizationService groupAuthorizationService,
          @NonNull BuildProperties buildProperties,
          AuditService auditService,
          Optional<DinaAuthenticatedUser> dinaAuthenticatedUser,
          ObjectMapper objectMapper
  ) {
    super(dinaService, groupAuthorizationService, Optional.of(auditService),
        new DinaMapper<>(OrganismDto.class), OrganismDto.class,
        Organism.class, null, externalResourceProvider, buildProperties, objectMapper);
    this.dinaAuthenticatedUser = dinaAuthenticatedUser.orElse(null);
  }

  @Override
  public <S extends OrganismDto> S create(S resource) {
    if (dinaAuthenticatedUser != null) {
      resource.setCreatedBy(dinaAuthenticatedUser.getUsername());
    }
    return super.create(resource);
  }

}
