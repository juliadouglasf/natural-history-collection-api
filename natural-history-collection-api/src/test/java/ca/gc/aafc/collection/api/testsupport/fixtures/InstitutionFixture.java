package ca.gc.aafc.collection.api.testsupport.fixtures;

import ca.gc.aafc.collection.api.dto.InstitutionDto;
import ca.gc.aafc.collection.api.entities.Institution;
import lombok.SneakyThrows;

import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public final class InstitutionFixture {

  private InstitutionFixture() {
  }

  public static InstitutionDto.InstitutionDtoBuilder newInstitution() {
    return InstitutionDto.builder()
      .name(RandomStringUtils.randomAlphabetic(4))
      .createdBy(RandomStringUtils.randomAlphabetic(4))
      .multilingualDescription(CollectionMethodTestFixture.newMulti());
  }

  @SneakyThrows(MalformedURLException.class)
  public static Institution.InstitutionBuilder<?, ?> newInstitutionEntity() {
    return Institution.builder()
      .uuid(UUID.randomUUID())
      .name(org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(3))
      .createdBy(org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(3))
      .multilingualDescription(CollectionMethodTestFixture.newMulti())
      .webpage(new URL("https://github.com/DINA-Web"))
      .address("123 Street \n City")
      .remarks(org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(30));
  }
}
