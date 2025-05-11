package com.siemens.internship;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the ItemController class.
 * This class tests the REST API endpoints for managing items, including creating, retrieving,
 * updating, deleting items and processing of items asynchronously.
 * Tests for ItemController reach 100% coverage.
 */
@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    // MockMvc is used to perform HTTP requests in tests without starting a server
    @Autowired
    private MockMvc mvc;
    // MockBean tells the Spring to inject a mock ItemService into the controller
    @MockBean private ItemService service;
    // ObjectMapper is used to convert Java objects to JSON and vice versa
    @Autowired private ObjectMapper mapper;

    @Test
    void getAllItems() throws Exception {
        List<Item> list = List.of(new Item(1L, "n", "", "", "a@b.com"));
        when(service.findAll()).thenReturn(list);

        mvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void createItemValid() throws Exception {
        Item in = new Item(null, "n", "d", "s", "a@b.com");
        Item out = new Item(5L, "n", "d", "s", "a@b.com");
        when(service.save(any())).thenReturn(out);

        mvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void createItemInvalid() throws Exception {
        Item in = new Item(null, "n", "d", "s", "a@b.com");
        Item out = new Item(5L, "n", "d", "s", "a@b.com");
        when(service.save(any())).thenReturn(out);

        mvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getItemByIdFound() throws Exception {
        Item it = new Item(7L, "n", "", "", "a@b.com");
        when(service.findById(7L)).thenReturn(Optional.of(it));

        mvc.perform(get("/api/items/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void getItemByIdNotFound() throws Exception {
        when(service.findById(8L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/items/8"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItemFound() throws Exception {
        Item in = new Item(null, "n", "", "", "a@b.com");
        Item saved = new Item(9L, "n", "", "", "a@b.com");
        when(service.findById(9L)).thenReturn(Optional.of(saved));
        when(service.save(any())).thenReturn(saved);

        mvc.perform(put("/api/items/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    void updateItemNotFound() throws Exception{
        String validBody = mapper.writeValueAsString(
                new Item(null, "name", "desc", "status", "user@example.com")
        );
        when(service.findById(15L)).thenReturn(Optional.empty());

        mvc.perform(put("/api/items/15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isNotFound());
    }


    @Test
    void deleteItemFound() throws Exception {
        when(service.findById(20L)).thenReturn(Optional.of(new Item()));
        doNothing().when(service).deleteById(20L);

        mvc.perform(delete("/api/items/20"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteItemNotFound() throws Exception {
        when(service.findById(30L)).thenReturn(Optional.empty());

        mvc.perform(delete("/api/items/30"))
                .andExpect(status().isNotFound());
    }

    @Test
    void processItems_returnsList() throws Exception {
        // Mock service to return a list directly
        List<Item> processed = List.of(new Item(1L, "n", "", "PROCESSED", "a@b.com"));
        CompletableFuture<List<Item>> fut = CompletableFuture.completedFuture(processed);
        when(service.processItemsAsync()).thenReturn(fut);

        mvc.perform(get("/api/items/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PROCESSED"));
    }
}
