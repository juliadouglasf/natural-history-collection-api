package ca.gc.aafc.collection.api.testsupport.fixtures;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import ca.gc.aafc.collection.api.dto.MaterialSampleDto;
import ca.gc.aafc.dina.dto.ExternalRelationDto;
import org.apache.commons.lang3.RandomStringUtils;

public class MaterialSampleTestFixture {

  public static final String DWC_CATALOG_NUMBER = "R-4313";
  public static final String[] DWC_OTHER_CATALOG_NUMBERS = new String[]{"A-1111", "B-2222"};
  public static final String GROUP = "aafc";
  public static final String MATERIAL_SAMPLE_NAME = "ocean water sample";
  public static final UUID PREPARED_BY = UUID.randomUUID();
  public static final LocalDate PREPARATION_DATE = LocalDate.now();
  public static final String CREATED_BY = "test user";
  public static final String DWC_DEGREE_OF_ESTABLISHMENT = "established";
  public static final String PREPARATION_REMARKS = "this is a remark on the preparation";
  public static final String HOST = "host";
  public static final Boolean PUBLICLY_RELEASABLE = false;
  public static final String NOT_PUBLICLY_RELEASABLE_REASON = "because it is not allowed";
  public static final String[] TAGS = new String[]{"0-Tag", "1-Tag"};
  public static final String MATERIAL_SAMPLE_STATE = "Damaged";
  public static final LocalDate STATE_CHANGED_ON = LocalDate.now();
  public static final String STATE_CHANGE_REMARKS = "state change remarks";
  public static final String MATERIAL_SAMPLE_REMARKS = "This sample is damaged";
  public static final String FILED_AS = "example of filedAs";
  public static final String PREPARATION_METHOD = "microwaving";
  public static final Boolean ALLOW_DUPLICATE_NAME = true;

  public static MaterialSampleDto newMaterialSample() {
    MaterialSampleDto materialSampleDto = new MaterialSampleDto();
    materialSampleDto.setDwcCatalogNumber(DWC_CATALOG_NUMBER);
    materialSampleDto.setDwcOtherCatalogNumbers(DWC_OTHER_CATALOG_NUMBERS);
    materialSampleDto.setCollectingEvent(null);
    materialSampleDto.setPreparedBy(ExternalRelationDto.builder().id(PREPARED_BY.toString()).type("agent").build());
    materialSampleDto.setPreparationDate(PREPARATION_DATE);
    materialSampleDto.setGroup(GROUP);
    materialSampleDto.setMaterialSampleName(MATERIAL_SAMPLE_NAME);
    materialSampleDto.setAttachment(List.of(
        ExternalRelationDto.builder().id(UUID.randomUUID().toString()).type("metadata").build()));
    materialSampleDto.setCreatedBy(CREATED_BY);
    materialSampleDto.setDwcDegreeOfEstablishment(DWC_DEGREE_OF_ESTABLISHMENT);
    materialSampleDto.setPreparationRemarks(PREPARATION_REMARKS);
    materialSampleDto.setHost(HOST);
    materialSampleDto.setBarcode(RandomStringUtils.randomAlphabetic(4));
    materialSampleDto.setPubliclyReleasable(PUBLICLY_RELEASABLE);
    materialSampleDto.setNotPubliclyReleasableReason(NOT_PUBLICLY_RELEASABLE_REASON);
    materialSampleDto.setTags(TAGS);
    materialSampleDto.setMaterialSampleState(MATERIAL_SAMPLE_STATE);
    materialSampleDto.setStateChangedOn(STATE_CHANGED_ON);
    materialSampleDto.setStateChangeRemarks(STATE_CHANGE_REMARKS);
    materialSampleDto.setMaterialSampleRemarks(MATERIAL_SAMPLE_REMARKS);
    materialSampleDto.setPreparationMethod(PREPARATION_METHOD);
    materialSampleDto.setPreparationAttachment(List.of(
        ExternalRelationDto.builder().id(UUID.randomUUID().toString()).type("metadata").build()));
    materialSampleDto.setAllowDuplicateName(ALLOW_DUPLICATE_NAME);
    return materialSampleDto;
  }
}
