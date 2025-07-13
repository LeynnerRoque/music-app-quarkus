package org.music.app.business.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.music.app.api.dto.request.StyleRequest;
import org.music.app.api.dto.response.StyleResponse;
import org.music.app.domain.model.Style;
import org.music.app.domain.repository.impl.StyleRepostiory;
import org.music.app.domain.repository.mappers.StyleMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class StyleServiceTest {

    @InjectMock
    StyleRepostiory repostiory;

    @InjectMock
    StyleMapper mapper;

    // We can inject the service directly, as its dependencies are mocked
    private StyleService styleService;

    @BeforeEach
    void setUp() {
        // Manually instantiate StyleService with mocked dependencies if not using @InjectMocks
        // If @InjectMock for repostiory and mapper are sufficient for Quarkus to wire,
        // you might not need this manual instantiation in simple cases.
        // However, it's good practice for clarity or if there are specific constructor arguments
        styleService = new StyleService(repostiory, mapper);

        // Reset mocks before each test to ensure a clean state
        Mockito.reset(repostiory, mapper);
    }

    @Test
    void create_shouldReturnCreatedOnSuccess() {
        StyleRequest request = new StyleRequest("Rock");
        Style styleEntity = new Style();
        styleEntity.setNameStyle("Rock");

        // Mock mapper behavior
        when(mapper.toEntity(request)).thenReturn(styleEntity);

        // Mock repository persist behavior
        // PanacheMock.persist(any(Style.class)); // For static Panache methods
        // For injected repository, mock its specific method
        doNothing().when(repostiory).persist(any(Style.class));

        String result = styleService.create(request);

        assertEquals("Created", result);
        verify(mapper, times(1)).toEntity(request);
        verify(repostiory, times(1)).persist(styleEntity);
    }

    @Test
    void create_shouldReturnErrorOnException() {
        StyleRequest request = new StyleRequest("Jazz");
        Style styleEntity = new Style();
        styleEntity.setNameStyle("Jazz");

        when(mapper.toEntity(request)).thenReturn(styleEntity);
        // Simulate an exception during persist
        doThrow(new RuntimeException("Database error")).when(repostiory).persist(any(Style.class));

        String result = styleService.create(request);

        assertEquals("Error on create object", result);
        verify(mapper, times(1)).toEntity(request);
        verify(repostiory, times(1)).persist(styleEntity);
    }

    @Test
    void findById_shouldReturnStyleResponse() {
        int id = 1;
        Style styleEntity = new Style();
        styleEntity.setId(id);
        styleEntity.setNameStyle("Pop");
        StyleResponse expectedResponse = new StyleResponse();
        expectedResponse.setId(1L);
        expectedResponse.setNameStyle("Rock");

        when(repostiory.findById(1L)).thenReturn(styleEntity);
        when(mapper.toResponse(styleEntity)).thenReturn(expectedResponse);

        StyleResponse result = styleService.findById(1L);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getNameStyle(), result.getNameStyle());
        verify(repostiory, times(1)).findById(1L);
        verify(mapper, times(1)).toResponse(styleEntity);
    }

    @Test
    void findById_shouldReturnNullIfNotFound() {
        Long id = 99L;

        when(repostiory.findById(id)).thenReturn(null); // Panache findById returns null if not found
        // The mapper.toResponse will be called with null, handle accordingly if your mapper throws NPT
        when(mapper.toResponse(null)).thenReturn(null);


        StyleResponse result = styleService.findById(id);

        assertNull(result);
        verify(repostiory, times(1)).findById(id);
        // mapper.toResponse will be called once with null
        verify(mapper, times(1)).toResponse(null);
    }


    @Test
    void listAll_shouldReturnListOfStyleResponses() {
        Style style1 = new Style();
        style1.setId(1);
        style1.setNameStyle("Rock");
        Style style2 = new Style();
        style2.setId(2);
        style2.setNameStyle("Pop");
        List<Style> entities = Arrays.asList(style1, style2);

        StyleResponse response1 = new StyleResponse();
        response1.setId(1l);
        response1.setNameStyle("Rock");

        StyleResponse response2 = new StyleResponse();
        response1.setId(2l);
        response1.setNameStyle("Pop");
        List<StyleResponse> expectedResponses = Arrays.asList(response1, response2);

        when(repostiory.listAll()).thenReturn(entities); // Use PanacheMock.to for listAll return
        when(mapper.toList(entities)).thenReturn(expectedResponses);

        List<StyleResponse> result = styleService.listAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedResponses, result);
        verify(repostiory, times(1)).listAll();
        verify(mapper, times(1)).toList(entities);
    }

    @Test
    void listAll_shouldReturnEmptyListWhenNoStyles() {
        List<Style> entities = Collections.emptyList();
        List<StyleResponse> expectedResponses = Collections.emptyList();

        when(repostiory.listAll()).thenReturn(entities);
        when(mapper.toList(entities)).thenReturn(expectedResponses);

        List<StyleResponse> result = styleService.listAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repostiory, times(1)).listAll();
        verify(mapper, times(1)).toList(entities);
    }

    @Test
    void update_shouldReturnUpdatedStyleResponseOnSuccess() {
        int id = 1;
        StyleResponse updateRequest = new StyleResponse();
        updateRequest.setId(1L);
        updateRequest.setNameStyle("Updated Rock");

        Style existingEntity = new Style();
        existingEntity.setId(id);
        existingEntity.setNameStyle("Old Rock");

        Style updatedEntity = new Style();
        updatedEntity.setId(id);
        updatedEntity.setNameStyle("Updated Rock"); // State after update

        StyleResponse expectedResponse = new StyleResponse();
        expectedResponse.setId(1L);
        expectedResponse.setNameStyle("Updated Rock");

        when(repostiory.findByIdOptional(1L)).thenReturn(Optional.of(existingEntity));
        // Mock persistAndFlush on the mocked repository instance
        doNothing().when(repostiory).persistAndFlush(any(Style.class));
        when(mapper.toResponse(any(Style.class))).thenReturn(expectedResponse);


        StyleResponse result = styleService.update(updateRequest);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getNameStyle(), result.getNameStyle());
        verify(repostiory, times(1)).findByIdOptional(1L);
        // Verify that persistAndFlush was called with the entity that has the updated name
        verify(repostiory, times(1)).persistAndFlush(argThat(style ->
                style.getId() == id && style.getNameStyle().equals("Updated Rock")
        ));
        verify(mapper, times(1)).toResponse(updatedEntity); // This depends on how mapper.toResponse is mocked
    }

//    @Test
//    void update_shouldReturnNullIfNotFound() {
//        Long id = 99L;
//        StyleResponse updateRequest = new StyleResponse(id, "Non Existent");
//
//        when(repostiory.findByIdOptional(id)).thenReturn(Optional.empty());
//
//        StyleResponse result = styleService.update(updateRequest);
//
//        assertNull(result);
//        verify(repostiory, times(1)).findByIdOptional(id);
//        verify(repostiory, never()).persistAndFlush(any(Style.class));
//        verify(mapper, never()).toResponse(any(Style.class));
//    }

//    @Test
//    void update_shouldReturnNullOnError() {
//        Long id = 1L;
//        StyleResponse updateRequest = new StyleResponse(id, "Error Trigger");
//        Style existingEntity = new Style();
//        existingEntity.setId(id);
//        existingEntity.setNameStyle("Existing");
//
//        when(repostiory.findByIdOptional(id)).thenReturn(Optional.of(existingEntity));
//        // Simulate an exception during persistAndFlush
//        doThrow(new RuntimeException("Update error")).when(repostiory).persistAndFlush(any(Style.class));
//
//        StyleResponse result = styleService.update(updateRequest);
//
//        assertNull(result);
//        verify(repostiory, times(1)).findByIdOptional(id);
//        verify(repostiory, times(1)).persistAndFlush(any(Style.class));
//        verify(mapper, never()).toResponse(any(Style.class)); // Mapper should not be called if update fails
//    }

//    @Test
//    void findByName_shouldReturnStyleResponse() {
//        String name = "Blues";
//        Style styleEntity = new Style();
//        styleEntity.setId(3L);
//        styleEntity.setNameStyle(name);
//        StyleResponse expectedResponse = new StyleResponse(3L, name);
//
//        // Mock the find method on the repository
//        // PanacheMock.every(p -> p.find(anyString(), any(Parameters.class))).thenReturn(PanacheMock.to(styleEntity)); // For static Panache methods
//        when(repostiory.find(eq("nameStyle = :name"), any(io.quarkus.panache.common.Parameters.class)))
//                .thenReturn(PanacheMock.to(styleEntity)); // Mock the chain for the specific query
//
//        when(mapper.toResponse(styleEntity)).thenReturn(expectedResponse);
//
//        StyleResponse result = styleService.findByName(name);
//
//        assertNotNull(result);
//        assertEquals(expectedResponse.getNameStyle(), result.getNameStyle());
//        verify(repostiory, times(1)).find(eq("nameStyle = :name"), any(io.quarkus.panache.common.Parameters.class));
//        verify(mapper, times(1)).toResponse(styleEntity);
//    }

//    @Test
//    void findByName_shouldReturnNullIfNotFound() {
//        String name = "NonExistent";
//
//        when(repostiory.find(eq("nameStyle = :name"), any(io.quarkus.panache.common.Parameters.class)))
//                .thenReturn(PanacheMock.to(null)); // Mock the chain for not found
//
//        when(mapper.toResponse(null)).thenReturn(null);
//
//        StyleResponse result = styleService.findByName(name);
//
//        assertNull(result);
//        verify(repostiory, times(1)).find(eq("nameStyle = :name"), any(io.quarkus.panache.common.Parameters.class));
//        verify(mapper, times(1)).toResponse(null);
//    }
}
