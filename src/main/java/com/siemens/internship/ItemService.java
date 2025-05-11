package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

//    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
//    private List<Item> processedItems = new ArrayList<>();
//    private int processedCount = 0;


    public List<Item> findAll() {return itemRepository.findAll();}


    // Retrieves an item by its ID
    // Throws an ItemException if the item is not found
    public Optional<Item> findById(Long id) throws ItemException {

        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            throw new ItemException("Item not found with id: " + id);
        }
        return item;
    }

    // Validate the item email format using ItemValidator
    // Throws an ItemException if the email format is invalid
    // Saves the item using the itemRepository
    public Item save(Item item) throws ItemException {
        ItemValidator validator = new ItemValidator();
        if (!validator.validateItemEmail(item.getEmail())) {
            throw new ItemException("Invalid email format" + item.getEmail());
        }
        return itemRepository.save(item);
    }

    // Deletes an item if exists
    public void deleteById(Long id) throws ItemException {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            throw new ItemException("Item not found with id: " + id);
        }
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */

    /**
     * The original implementation had several issues:
     * Declares @Async but returns a List<Item>
     * In that way, the processedItems list was returned before all items were processed.
     * By returning a CompletableFuture<List<Item>>, we explicitly control when that future completes.
     * Multiple threads increment processedCount and add to processedItems without any synchronization.
     * It was a fixed number of threads (10) regardless of the number of items.
     * Those threads were 'fighting' for tasks in an inefficient way.
     * The old version never managed the executor.
     */

    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();
        if (itemIds.isEmpty()) {
            // if we don't have any items, we can return an empty list
            return CompletableFuture.completedFuture(List.of());
        }

        // Determine how many threads to use
        int maxThreads = Runtime.getRuntime().availableProcessors();
        int threadCount = Math.min(itemIds.size(), maxThreads);

        // Partition id lis into 'equal' parts for each thread
        List<List<Long>> partitions = new ArrayList<>();
        int chunkSize = (itemIds.size() + threadCount - 1) / threadCount;
        for (int start = 0; start < itemIds.size(); start += chunkSize) {
            int end = Math.min(start + chunkSize, itemIds.size());
            partitions.add(itemIds.subList(start, end));
        }

        // Create a local executor to manage our threads
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // We will use CompletableFuture to process each partition in parallel
        List<CompletableFuture<List<Item>>> futures = partitions.stream()
                .map(chunk -> CompletableFuture.supplyAsync(() -> {
                    List<Item> processed = new ArrayList<>();
                    for (Long id : chunk) {
                        try {
                            Thread.sleep(100);
                            itemRepository.findById(id).ifPresent(item -> {   //retrieve
                                item.setStatus("PROCESSED");    //update status
                                processed.add(itemRepository.save(item)); //save
                            });
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            // stop processing if interrupted
                            break;
                        } catch (Exception ex) {
                            System.err.println("Failed processing item " + id);
                        }
                    }
                    return processed;
                }, executor))
                .collect(Collectors.toList());

        // Combine all the results into a single CompletableFuture and block until all are done
        CompletableFuture<Void> allDone =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        List<Item> result = allDone
                .thenApply(v -> futures.stream()
                        .flatMap(f -> f.join().stream())
                        .collect(Collectors.toList()))
                .join();

        // Clean up the custom executor
        executor.shutdown();

        // Return the processed items as a CompletableFuture (done = true)
        return CompletableFuture.completedFuture(result);
    }
}

