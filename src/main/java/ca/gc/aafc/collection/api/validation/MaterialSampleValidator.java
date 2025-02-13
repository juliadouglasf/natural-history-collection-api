package ca.gc.aafc.collection.api.validation;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import ca.gc.aafc.collection.api.entities.MaterialSample;

import lombok.NonNull;

@Component
public class MaterialSampleValidator implements Validator {

  private final MessageSource messageSource;

  public MaterialSampleValidator(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public static final String VALID_PARENT_RELATIONSHIP_LOOP = "validation.constraint.violation.loopingParentMaterialSample";
  public static final String PARENT_AND_EVENT_ERROR_KEY = "validation.constraint.violation.sample.parentWithEvent";

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return MaterialSample.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {
    if (!supports(target.getClass())) {
      throw new IllegalArgumentException("MaterialSampleValidator not supported for class " + target.getClass());
    }
    MaterialSample materialSample = (MaterialSample) target;
    checkParentIsNotSelf(errors, materialSample);
    checkHasParentOrEventOrAcquisitionEvent(errors, materialSample);
  }

  private void checkHasParentOrEventOrAcquisitionEvent(Errors errors, MaterialSample materialSample) {
    if (isMoreThanOne(materialSample.getParentMaterialSample() != null, materialSample.getCollectingEvent() != null, materialSample.getAcquisitionEvent() != null)) {
      String errorMessage = getMessage(PARENT_AND_EVENT_ERROR_KEY);
      errors.rejectValue("parentMaterialSample", PARENT_AND_EVENT_ERROR_KEY, errorMessage);
    }
  }

  private void checkParentIsNotSelf(Errors errors, MaterialSample materialSample) {
    if (materialSample.getParentMaterialSample() != null
      && materialSample.getParentMaterialSample().getUuid().equals(materialSample.getUuid())) {
      String errorMessage = getMessage(VALID_PARENT_RELATIONSHIP_LOOP);
      errors.rejectValue("parentMaterialSample", VALID_PARENT_RELATIONSHIP_LOOP, errorMessage);
    }
  }
  
  private Boolean isMoreThanOne(boolean b1, boolean b2, boolean b3) {
    return b1 && b2 || b1 && b3 || b2 && b3;
  }

  private String getMessage(String key) {
    return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
  }

}
