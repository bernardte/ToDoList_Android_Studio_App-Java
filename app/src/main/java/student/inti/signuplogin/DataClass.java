package student.inti.signuplogin;

public class DataClass {

    private String imageURL;
    private String caption;
    private String userId;
    private String documentId; // New field for document ID

    // Default constructor
    public DataClass() {
    }

    // Constructor with all parameters
    public DataClass(String imageURL, String caption, String userId, String documentId) {
        this.imageURL = imageURL;
        this.caption = caption;
        this.userId = userId;
        this.documentId = documentId;
    }

    // Getter for imageURL
    public String getImageURL() {
        return imageURL;
    }

    // Setter for imageURL
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    // Getter for caption
    public String getCaption() {
        return caption;
    }

    // Setter for caption
    public void setCaption(String caption) {
        this.caption = caption;
    }

    // Getter for userId
    public String getUserId() {
        return userId;
    }

    // Getter for documentId
    public String getDocumentId() {
        return documentId; // Method to retrieve document ID
    }

    // Setter for documentId
    public void setDocumentId(String documentId) {
        this.documentId = documentId; // Method to set document ID
    }
}