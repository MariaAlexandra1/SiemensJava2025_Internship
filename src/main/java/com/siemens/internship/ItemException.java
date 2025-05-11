package com.siemens.internship;

// Custom exception class for handling item-related errors
// This exception can be thrown when an item is not found or when there is an issue with the item data
public class ItemException extends Exception{
    public ItemException(String message) {
        super(message);
    }
}