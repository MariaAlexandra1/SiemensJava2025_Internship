package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ItemService class.
 * This class tests the functionality of the ItemService, including methods for finding all items,
 * finding an item by ID, saving an item, deleting an item, and processing items asynchronously.
 * This class includes tests for both valid and invalid scenarios.
 * Tests for ItemService reach 100% coverage.
 */

public class ItemServiceTest {
    // Mock the ItemRepository to simulate database interactions
    @Mock private ItemRepository repo;
    // Inject the mocked repository into the ItemService
    @InjectMocks private ItemService service;


    // Before each test, initialize the mocks
    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllTest() {
        List<Item> dummy = List.of(
                new Item(1L, "A","D","OLD","a@b.com"),
                new Item(2L, "B","D","OLD","c@d.com")
        );
        when(repo.findAll()).thenReturn(dummy);

        List<Item> result = service.findAll();

        assertSame(dummy, result, "findAll() should return exactly what the repository returns");
        verify(repo).findAll();
    }

    @Test
    void findByIdExists() throws ItemException {
        Item item = new Item(1L, "n", "d", "s", "a@b.com");
        when(repo.findById(1L)).thenReturn(Optional.of(item));

        Optional<Item> res = service.findById(1L);
        assertTrue(res.isPresent());
        assertEquals("n", res.get().getName());
        assertEquals("d", res.get().getDescription());
        assertEquals("s", res.get().getStatus());
        assertEquals("a@b.com", res.get().getEmail());
    }

    @Test
    void findByIdNotExists() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        ItemException ex = assertThrows(ItemException.class, () -> service.findById(99L));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void saveValidEmail() throws ItemException {
        Item in = new Item(null, "n", "d", "s", "a@b.com");
        Item saved = new Item(1L, "n", "d", "s", "a@b.com");
        when(repo.save(in)).thenReturn(saved);

        Item out = service.save(in);
        assertEquals(1L, out.getId());
    }

    @Test
    void saveInvalidEmail() {
        Item in = new Item(null, "n", "d", "s", "bad-email");
        ItemException ex = assertThrows(ItemException.class, () -> service.save(in));
        assertTrue(ex.getMessage().contains("Invalid email"));
    }

    @Test
    void deleteByIdExists() throws ItemException {
        when(repo.findById(2L)).thenReturn(Optional.of(new Item()));
        // should not throw
        service.deleteById(2L);
        verify(repo).deleteById(2L);
    }

    @Test
    void deleteByIdNotExists() {
        when(repo.findById(3L)).thenReturn(Optional.empty());
        assertThrows(ItemException.class, () -> service.deleteById(3L));
    }

    @Test
    void processItemsAsyncEmpty() throws Exception {
        when(repo.findAllIds()).thenReturn(List.of());
        CompletableFuture<List<Item>> future = service.processItemsAsync();
        assertTrue(future.isDone());
        assertTrue(future.get().isEmpty());
    }

    @Test
    void processItemsAsyncNonEmpty() throws Exception {
        // prepare three items
        List<Long> ids = List.of(10L, 20L, 30L);
        when(repo.findAllIds()).thenReturn(ids);

        // for each id, return an item
        for (Long id : ids) {
            when(repo.findById(id)).thenReturn(Optional.of(new Item(id, "n", "d", "s", "x@y.com")));
            // we simulate save by returning same object with status updated
            when(repo.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        }

        CompletableFuture<List<Item>> future = service.processItemsAsync();
        List<Item> processed = future.get();  // will block until done

        // assert all got processed
        assertEquals(3, processed.size());
        processed.forEach(it -> assertEquals("PROCESSED", it.getStatus()));
    }

    @Test
    void processItemsAsyncExceptions() throws Exception {
        // IDs: 1 succeeds, 2 throws in findById
        List<Long> ids = List.of(1L, 2L);
        when(repo.findAllIds()).thenReturn(ids);

        // ID 1: normal item
        Item item1 = new Item(1L, "n","d","OLD","x@y.com");
        when(repo.findById(1L)).thenReturn(Optional.of(item1));
        when(repo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        // ID 2: simulate repository error
        when(repo.findById(2L)).thenThrow(new RuntimeException("DB down"));

        // run
        CompletableFuture<List<Item>> future = service.processItemsAsync();
        List<Item> processed = future.get();  // shouldn't throw

        // only ID 1 should be processed
        assertEquals(1, processed.size(), "Only the item without repo error should be in the result");
        assertEquals(1L, processed.get(0).getId());
        assertEquals("PROCESSED", processed.get(0).getStatus());
    }
}
