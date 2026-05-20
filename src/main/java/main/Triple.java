package main;

import java.util.Objects;

public record Triple(String subject, String predicate, String object) {
    
    // Compact constructor for validation
    public Triple {
        Objects.requireNonNull(subject, "Subject cannot be null");
        Objects.requireNonNull(predicate, "Predicate cannot be null");
        Objects.requireNonNull(object, "Object cannot be null");
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", subject, predicate, object);
    }
}