package io.github.sps23.designpatterns.prototype;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Document Prototype Tests")
class DocumentTest {

    @Test
    @DisplayName("Deep copy should create an independent document")
    void deepCopyShouldBeIndependent() {
        Document original = new Document("Annual Report", "Ada",
                new ArrayList<>(List.of("Introduction", "Market Analysis")));

        Document deepCopy = original.deepCopy();
        deepCopy.getSections().add("Conclusion");

        assertNotSame(original, deepCopy, "Deep copy should be a new object");
        assertEquals(2, original.getSections().size(),
                "Original should keep its original sections");
        assertEquals(3, deepCopy.getSections().size(), "Deep copy should be mutable independently");
    }

    @Test
    @DisplayName("Shallow clone should share mutable nested state")
    void shallowCloneShouldShareNestedState() {
        Document original = new Document("Annual Report", "Ada",
                new ArrayList<>(List.of("Introduction")));

        Document shallowCopy = original.clone();
        shallowCopy.getSections().add("Conclusion");

        assertNotSame(original, shallowCopy, "Clone should be a new object");
        assertEquals(2, original.getSections().size(),
                "Original sections are shared with the shallow clone");
    }
}
