package ca.gc.aafc.collection.api.service;

import ca.gc.aafc.collection.api.entities.CollectingEvent;
import ca.gc.aafc.collection.api.entities.GeoreferenceAssertion;
import ca.gc.aafc.collection.api.validation.CollectingEventValidator;
import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CollectingEventService extends DefaultDinaService<CollectingEvent> {

  private final CollectingEventValidator collectingEventValidator;

  public CollectingEventService(@NonNull BaseDAO baseDAO, @NonNull CollectingEventValidator collectingEventValidator) {
    super(baseDAO);
    this.collectingEventValidator = collectingEventValidator;
  }

  @Override
  protected void preCreate(CollectingEvent entity) {
    entity.setUuid(UUID.randomUUID());
    assignAutomaticValues(entity);
    linkAssertions(entity);
    validateCollectingEvent(entity);
  }

  @Override
  public void preUpdate(CollectingEvent entity) {
    linkAssertions(entity);
    validateCollectingEvent(entity);
  }

  private static void linkAssertions(CollectingEvent entity) {
    List<GeoreferenceAssertion> geos = entity.getGeoReferenceAssertions();
    if (CollectionUtils.isNotEmpty(geos)) {
      geos.forEach(geoReferenceAssertion -> geoReferenceAssertion.setCollectingEvent(entity));
    }
    assignAutomaticValues(entity);
  }

  private static void assignAutomaticValues(CollectingEvent entity) {
    if (entity.getGeographicPlaceNameSourceDetail() != null) {
      entity.getGeographicPlaceNameSourceDetail().setRecordedOn(OffsetDateTime.now());
    }
  }

  public void validateCollectingEvent(CollectingEvent entity) {
    Errors errors = new BeanPropertyBindingResult(entity, entity.getUuid().toString());
    collectingEventValidator.validate(entity, errors);

    if (!errors.hasErrors()) {
      return;
    }

    Optional<String> errorMsg = errors.getAllErrors().stream().map(ObjectError::getDefaultMessage).findAny();
    errorMsg.ifPresent(msg -> {
      throw new IllegalArgumentException(msg);
    });
  }

}
