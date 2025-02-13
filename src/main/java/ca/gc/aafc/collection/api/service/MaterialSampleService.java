package ca.gc.aafc.collection.api.service;

import ca.gc.aafc.collection.api.dao.CollectionHierarchicalDataDAO;
import ca.gc.aafc.collection.api.dto.MaterialSampleDto;
import ca.gc.aafc.collection.api.entities.Association;
import ca.gc.aafc.collection.api.entities.CollectionManagedAttribute;
import ca.gc.aafc.collection.api.entities.ExtensionValue;
import ca.gc.aafc.collection.api.entities.ImmutableMaterialSample;
import ca.gc.aafc.collection.api.entities.MaterialSample;
import ca.gc.aafc.collection.api.validation.AssociationValidator;
import ca.gc.aafc.collection.api.validation.CollectionManagedAttributeValueValidator;
import ca.gc.aafc.collection.api.validation.MaterialSampleExtensionValueValidator;
import ca.gc.aafc.collection.api.validation.MaterialSampleValidator;
import ca.gc.aafc.collection.api.validation.RestrictionExtensionValueValidator;
import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.jpa.PredicateSupplier;
import ca.gc.aafc.dina.service.MessageProducingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@Log4j2
public class MaterialSampleService extends MessageProducingService<MaterialSample> {

  private final MaterialSampleValidator materialSampleValidator;
  private final AssociationValidator associationValidator;
  private final CollectionManagedAttributeValueValidator collectionManagedAttributeValueValidator;
  private final CollectionManagedAttributeValueValidator.CollectionManagedAttributeValidationContext validationContext;
  private final CollectionHierarchicalDataDAO hierarchicalDataService;

  private final MaterialSampleExtensionValueValidator materialSampleExtensionValueValidator;
  private final RestrictionExtensionValueValidator restrictionExtensionValueValidator;

  public MaterialSampleService(
    @NonNull BaseDAO baseDAO,
    @NonNull SmartValidator sv,
    @NonNull MaterialSampleValidator materialSampleValidator,
    @NonNull CollectionManagedAttributeValueValidator collectionManagedAttributeValueValidator,
    @NonNull AssociationValidator associationValidator,
    @NonNull CollectionHierarchicalDataDAO hierarchicalDataService,
    @NonNull MaterialSampleExtensionValueValidator materialSampleExtensionValueValidator,
    @NonNull RestrictionExtensionValueValidator restrictionExtensionValueValidator,
    ApplicationEventPublisher eventPublisher
  ) {
    super(baseDAO, sv, MaterialSampleDto.TYPENAME, eventPublisher);
    this.materialSampleValidator = materialSampleValidator;
    this.collectionManagedAttributeValueValidator = collectionManagedAttributeValueValidator;
    this.associationValidator = associationValidator;
    this.hierarchicalDataService = hierarchicalDataService;
    this.materialSampleExtensionValueValidator = materialSampleExtensionValueValidator;
    this.restrictionExtensionValueValidator = restrictionExtensionValueValidator;
    this.validationContext = CollectionManagedAttributeValueValidator.CollectionManagedAttributeValidationContext
            .from(CollectionManagedAttribute.ManagedAttributeComponent.MATERIAL_SAMPLE);
  }

  @Override
  public <T> List<T> findAll(
    @NonNull Class<T> entityClass,
    @NonNull PredicateSupplier<T> where,
    BiFunction<CriteriaBuilder, Root<T>, List<Order>> orderBy,
    int startIndex,
    int maxResult,
    @NonNull Set<String> includes,
    @NonNull Set<String> relationships
  ) {

    log.debug("Relationships received: {}", relationships);
    // We can't fetch join materialSampleChildren without getting duplicates since it's a read-only list and we can't use the OrderColumn
    // This will let materialSampleChildren be lazy loaded
    Set<String> filteredRelationships = relationships.stream().filter( rel -> !rel.equalsIgnoreCase(MaterialSample.CHILDREN_COL_NAME)).collect(Collectors.toSet());

    List<T> all = super.findAll(entityClass, where, orderBy, startIndex, maxResult, includes, filteredRelationships);

    // sanity checks
    if(entityClass != MaterialSample.class || CollectionUtils.isEmpty(all)) {
      return all;
    }

    // augment information where required
    all.forEach(t -> {
      if (t instanceof MaterialSample ms) {
        try {
          if (includes.contains(MaterialSample.HIERARCHY_PROP_NAME)) {
            setHierarchy(ms);
          }
          if (includes.contains(MaterialSample.CHILDREN_COL_NAME)) {
            setChildrenOrdinal(ms);
          }
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    });
    return all;
  }

  public void setHierarchy(MaterialSample sample) throws JsonProcessingException {
    sample.setHierarchy(hierarchicalDataService.getHierarchy(sample.getId()));
  }

  public void setChildrenOrdinal(MaterialSample sample) {
    List<ImmutableMaterialSample> sortedChildren = sample.getMaterialSampleChildren().stream()
            .sorted(Comparator.comparingInt(ImmutableMaterialSample::getId)).toList();

    for (int i = 0; i < sortedChildren.size(); i++) {
      sortedChildren.get(i).setOrdinal(i);
    }
  }

  @Override
  protected void preCreate(MaterialSample entity) {
    entity.setUuid(UUID.randomUUID());
    linkAssociations(entity);
  }

  @Override
  protected void preUpdate(MaterialSample entity) {
    linkAssociations(entity);
  }

  private void linkAssociations(MaterialSample entity) {
    if (CollectionUtils.isNotEmpty(entity.getAssociations())) {
      entity.getAssociations().forEach(association -> {
        UUID associatedUuid = association.getAssociatedSample().getUuid();
        association.setSample(entity);
        association.setAssociatedSample(this.findOne(associatedUuid, MaterialSample.class));
      });
    }
  }

  @Override
  public void validateBusinessRules(MaterialSample entity) {
    applyBusinessRule(entity, materialSampleValidator);
    validateManagedAttribute(entity);
    validateAssociations(entity);
    validateExtensionValues(entity);
  }

  private void validateManagedAttribute(MaterialSample entity) {
    collectionManagedAttributeValueValidator.validate(entity, entity.getManagedAttributes(), validationContext);
  }

  private void validateAssociations(MaterialSample entity) {
    if (CollectionUtils.isNotEmpty(entity.getAssociations())) {
      int associationIndex = 0;
      for (Association association : entity.getAssociations()) {
        applyBusinessRule(entity.getUuid().toString() + associationIndex, association, associationValidator);
        associationIndex++;
      }
    }
  }

  private void validateExtensionValues(@NonNull MaterialSample entity) {

    if(MapUtils.isNotEmpty(entity.getExtensionValues())) {
      for (String currExt : entity.getExtensionValues().keySet()) {
        entity.getExtensionValues().get(currExt).forEach((k, v) -> applyBusinessRule(
                entity.getUuid().toString(),
                ExtensionValue.builder().extKey(currExt).extFieldKey(k).value(v).build(),
                materialSampleExtensionValueValidator
        ));
      }
    }

    if (MapUtils.isNotEmpty(entity.getRestrictionFieldsExtension())) {
      for (String currExt : entity.getRestrictionFieldsExtension().keySet()) {
        entity.getRestrictionFieldsExtension().get(currExt).forEach((k, v) -> applyBusinessRule(
                entity.getUuid().toString(),
                ExtensionValue.builder().extKey(currExt).extFieldKey(k).value(v).build(),
                restrictionExtensionValueValidator
        ));
      }
    }
  }



  @Override
  public MaterialSample create(MaterialSample entity) {
    MaterialSample sample = super.create(entity);
    return detachParent(sample);
  }

  @Override
  public MaterialSample update(MaterialSample entity) {
    MaterialSample sample = super.update(entity);
    return detachParent(sample);
  }

  /**
   * Detaches the parent to make sure it reloads its children list
   *
   * @param sample
   * @return
   */
  private MaterialSample detachParent(MaterialSample sample) {
    if (sample.getParentMaterialSample() != null) {
      detach(sample.getParentMaterialSample());
    }
    return sample;
  }
}
