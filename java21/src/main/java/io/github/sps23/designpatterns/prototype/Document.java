package io.github.sps23.designpatterns.prototype;

import java.util.ArrayList;
import java.util.List;

/**
 * Prototype pattern in Java 21.
 *
 * <p>
 * Demonstrates both a shallow copy via the classic {@link Cloneable} mechanism
 * and a safe deep copy via a dedicated method. The {@code sections} list is the
 * mutable nested state that separates the two copying strategies.
 * </p>
 */
public class Document implements Cloneable {

    private String title;
    private String author;
    private List<String> sections;

    public Document(String title, String author, List<String> sections) {
        this.title = title;
        this.author = author;
        this.sections = new ArrayList<>(sections);
    }

    /**
     * Shallow copy. The new {@code Document} shares the same {@code sections} list
     * reference with the original, which is the classic Java prototype behaviour
     * when relying on {@link Object#clone()}.
     */
    @Override
    public Document clone() {
        try {
            return (Document) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen because we implement Cloneable.
            throw new AssertionError(e);
        }
    }

    /**
     * Deep copy. Creates a new list so mutations on the copy do not leak back to
     * the original document.
     */
    public Document deepCopy() {
        return new Document(title, author, new ArrayList<>(sections));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getSections() {
        return sections;
    }

    public void setSections(List<String> sections) {
        this.sections = sections;
    }
}
