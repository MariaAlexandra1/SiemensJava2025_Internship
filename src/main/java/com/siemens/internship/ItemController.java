package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    // Just returns the list of existing items
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    // Creates a new item. Validation is done automatically by @Valid annotation
    // and BindingResult. If there are validation errors, it returns 400 - Bad Request
    // and a map of field errors.
    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) throws ItemException {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage
                    ));
            return ResponseEntity.badRequest().body(errors);
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    // Retrieves an item by its ID. If the item is not found, it returns 404 - Not Found
    // If the item is found, it returns 200 - OK
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) throws ItemException {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Updates an existing item. If the item is not found, it returns 404 - Not Found
    // If the item is found it returns 201 - Created
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @Valid @RequestBody Item item) throws ItemException {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Deletes an item by its ID. If the item is not found, it returns 404 - Not Found
    // If the item is found, it returns 204 - No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) throws ItemException {
        if(itemService.findById(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        itemService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // This method processes items asynchronously using CompletableFuture
    // It returns a list of processed items
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        List<Item> items = itemService.processItemsAsync().join();
        return ResponseEntity.ok(items);
    }
}
