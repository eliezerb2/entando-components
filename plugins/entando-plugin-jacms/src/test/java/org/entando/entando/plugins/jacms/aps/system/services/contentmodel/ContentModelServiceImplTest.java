package org.entando.entando.plugins.jacms.aps.system.services.contentmodel;

import com.agiletec.aps.system.common.FieldSearchFilter;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.SmallContentType;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentModel;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentModelReference;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.IContentModelManager;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.dictionary.ContentModelDictionaryProvider;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentModelDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.entando.entando.aps.system.exception.RestRourceNotFoundException;
import org.entando.entando.plugins.jacms.web.contentmodel.validator.ContentModelValidator;
import org.entando.entando.web.common.model.Filter;
import org.entando.entando.web.common.model.PagedMetadata;
import org.entando.entando.web.common.model.RestListRequest;
import org.entando.entando.web.common.exceptions.ValidationConflictException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import org.entando.entando.aps.system.services.dataobjectmodel.model.IEntityModelDictionary;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.validation.ObjectError;

public class ContentModelServiceImplTest {

    @Mock
    private IContentManager contentManager;

    @Mock
    private IContentModelManager contentModelManager;

    @Spy
    private ContentModelDictionaryProvider dictionaryProvider;

    @InjectMocks
    private ContentModelServiceImpl contentModelService;

    private Map<Long, ContentModel> mockedContentModels;
    private Map<String, SmallContentType> mockedContentTypes;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        fillMockedContentModelsMap();
        fillMockedContentTypesMap();

        when(contentModelManager.getContentModel(anyLong()))
                .thenAnswer(invocation -> mockedContentModels.get((long) invocation.getArgument(0)));
        when(contentModelManager.getContentModels()).thenReturn(new ArrayList<>(mockedContentModels.values()));

        when(contentManager.getSmallContentTypesMap()).thenReturn(mockedContentTypes);

        when(contentModelManager.getContentModelReferences(1l))
                .thenReturn(Collections.singletonList(new ContentModelReference()));

        dictionaryProvider.setContentMap(new ArrayList<>());
        dictionaryProvider.setI18nMap(new ArrayList<>());
        dictionaryProvider.setInfoMap(new ArrayList<>());
        dictionaryProvider.setCommonMap(new ArrayList<>());
        dictionaryProvider.setAllowedPublicAttributeMethods(new Properties());
    }

    @Test
    public void findManyShouldFindAll() {
        RestListRequest request = new RestListRequest();
        PagedMetadata<ContentModelDto> result = contentModelService.findMany(request);
        assertThat(result.getBody()).isNotNull().hasSize(3);
    }

    @Test
    public void findManyShouldFilter() {
        RestListRequest request = new RestListRequest();
        String contentType = "AAA";
        Filter filter = new Filter("contentType", contentType);
        request.addFilter(filter);
        PagedMetadata<ContentModelDto> result = contentModelService.findMany(request);
        assertThat(result.getBody()).isNotNull().hasSize(1);
        assertThat(result.getBody().get(0).getContentType()).isEqualTo(contentType);
    }

    @Test
    public void findManyShouldSort() {
        RestListRequest req = new RestListRequest();
        req.setSort("contentType");
        req.setDirection(FieldSearchFilter.DESC_ORDER);
        PagedMetadata<ContentModelDto> res = contentModelService.findMany(req);
        assertThat(res.getBody()).isNotNull().hasSize(3);
        assertThat(res.getBody().stream().map(cm -> cm.getContentType()))
                .containsExactly("CCC", "BBB", "AAA");
    }

    @Test
    public void shouldFindOne() {
        assertThat(contentModelService.getContentModel(1l)).isNotNull();
    }

    @Test(expected = RestRourceNotFoundException.class)
    public void shouldFailWithNotFound() {
        try {
            contentModelService.getContentModel(20l);
        } catch (RestRourceNotFoundException ex) {
            assertThat(ex.getErrorCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_NOT_FOUND);
            throw ex;
        }
    }

    @Test
    public void shouldFindOneUsingOptional() {
        long id = 1l;
        Optional<ContentModelDto> result = contentModelService.findById(id);
        assertTrue(result.isPresent());
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    public void shouldFindNothingUsingOptional() {
        Optional<ContentModelDto> result = contentModelService.findById(20l);
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldCreateContentModel() {
        ContentModelDto contentModelToCreate = new ContentModelDto();
        contentModelToCreate.setContentType("AAA");
        contentModelToCreate.setId(4l);

        ContentModelDto result = contentModelService.create(contentModelToCreate);
        assertThat(result.getId()).isEqualTo(contentModelToCreate.getId());
        assertThat(result.getContentType()).isEqualTo(contentModelToCreate.getContentType());
    }

    @Test(expected = ValidationConflictException.class)
    public void shouldFailCreatingContentModel() {
        try {
            ContentModelDto contentModelToCreate = new ContentModelDto();

            // Existing content model id
            long id = 1l;

            // Content type not found
            contentModelToCreate.setContentType("XXX");
            contentModelToCreate.setId(id);

            // Wrong utilizer
            SmallContentType utilizer = new SmallContentType();
            utilizer.setCode("DEF");
            when(contentModelManager.getDefaultUtilizer(id)).thenReturn(utilizer);

            contentModelService.create(contentModelToCreate);

        } catch (ValidationConflictException ex) {
            List<ObjectError> errors = ex.getBindingResult().getAllErrors();
            assertThat(errors).isNotNull().hasSize(3);
            assertThat(errors.stream().map(e -> e.getCode()))
                    .containsExactlyInAnyOrder(
                            ContentModelValidator.ERRCODE_CONTENTMODEL_ALREADY_EXISTS,
                            ContentModelValidator.ERRCODE_CONTENTMODEL_TYPECODE_NOT_FOUND,
                            ContentModelValidator.ERRCODE_CONTENTMODEL_WRONG_UTILIZER
                    );
            throw ex;
        }
    }

    @Test
    public void shouldUpdateContentModel() {
        long id = 1l;
        ContentModelDto contentModelToUpdate = new ContentModelDto();
        contentModelToUpdate.setId(id);
        contentModelToUpdate.setContentType("AAA");

        String updatedDescription = "test description";
        contentModelToUpdate.setDescr(updatedDescription);

        this.mockedContentModels.get(id).setDescription(updatedDescription);

        ContentModelDto result = contentModelService.update(contentModelToUpdate);
        assertThat(result.getDescr()).isEqualTo(updatedDescription);
    }

    @Test(expected = RestRourceNotFoundException.class)
    public void shouldFailUpdatingContentModelBecauseNotFound() {
        try {
            long id = 20l; // inexistent content model
            ContentModelDto contentModelToUpdate = new ContentModelDto();
            contentModelToUpdate.setId(id);
            contentModelService.update(contentModelToUpdate);
        } catch (RestRourceNotFoundException ex) {
            assertThat(ex.getErrorCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_NOT_FOUND);
            throw ex;
        }
    }

    @Test(expected = ValidationConflictException.class)
    public void shouldFailUpdatingContentModelBecauseInvalidContentType() {
        try {
            long id = 1l;
            ContentModelDto contentModelToUpdate = new ContentModelDto();
            contentModelToUpdate.setId(id);
            contentModelToUpdate.setContentType("BBB");
            contentModelService.update(contentModelToUpdate);
        } catch (ValidationConflictException ex) {
            List<ObjectError> errors = ex.getBindingResult().getAllErrors();
            assertThat(errors).isNotNull().hasSize(1);
            assertThat(errors.get(0).getCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_CANNOT_UPDATE_CONTENT_TYPE);
            throw ex;
        }
    }

    @Test(expected = ValidationConflictException.class)
    public void shouldFailUpdatingContentModelBecauseContentTypeNotFound() {
        try {
            long id = 3l;
            ContentModelDto contentModelToUpdate = new ContentModelDto();
            contentModelToUpdate.setId(id);
            contentModelToUpdate.setContentType("CCC");
            contentModelService.update(contentModelToUpdate);
        } catch (ValidationConflictException ex) {
            List<ObjectError> errors = ex.getBindingResult().getAllErrors();
            assertThat(errors).isNotNull().hasSize(1);
            assertThat(errors.get(0).getCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_TYPECODE_NOT_FOUND);
            throw ex;
        }
    }

    @Test
    public void shoudlDeleteContentModel() {
        contentModelService.delete(2l);
    }

    @Test(expected = ValidationConflictException.class)
    public void shoudlFailDeletingContentModel() {
        try {
            contentModelService.delete(1l);
        } catch (ValidationConflictException ex) {
            List<ObjectError> errors = ex.getBindingResult().getAllErrors();
            assertThat(errors).isNotNull().hasSize(1);
            assertThat(errors.get(0).getCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_REFERENCES);
            throw ex;
        }
    }

    @Test
    public void shouldReturnReferences() {
        assertThat(contentModelService.getContentModelReferences(1l))
                .isNotNull().hasSize(1);
    }

    @Test(expected = RestRourceNotFoundException.class)
    public void shouldFailReturningReferences() {
        try {
            contentModelService.getContentModelReferences(20l);
        } catch (RestRourceNotFoundException ex) {
            assertThat(ex.getErrorCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_NOT_FOUND);
            throw ex;
        }
    }

    @Test
    public void shouldReturnDictionary() {
        Content contentPrototype = new Content();
        contentPrototype.setTypeCode("AAA");
        when(this.contentManager.getEntityPrototype("AAA")).thenReturn(contentPrototype);
        IEntityModelDictionary dictionary = contentModelService.getContentModelDictionary("AAA");
        assertThat(dictionary).isNotNull();
    }

    public void shouldFailReturningDictionary() {

    }

    private void fillMockedContentModelsMap() {
        this.mockedContentModels = new HashMap<>();
        addMockedContentModel(1l, "AAA");
        addMockedContentModel(2l, "BBB");
        addMockedContentModel(3l, "CCC");
    }

    private void addMockedContentModel(long id, String contentType) {
        ContentModel contentModel = new ContentModel();
        contentModel.setId(id);
        contentModel.setContentType(contentType);
        contentModel.setDescription("description");
        this.mockedContentModels.put(id, contentModel);
    }

    private void fillMockedContentTypesMap() {
        this.mockedContentTypes = new HashMap<>();
        addMockedContentType("AAA");
        addMockedContentType("BBB");
        // CCC needs to be missing for testing validation
    }

    private void addMockedContentType(String code) {
        SmallContentType contentType = new SmallContentType();
        contentType.setCode(code);
        this.mockedContentTypes.put(code, contentType);
    }
}
