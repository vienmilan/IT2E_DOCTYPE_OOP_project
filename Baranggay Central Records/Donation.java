public class Donation {
    public int id;
    public String donor;
    public double amount;
    public String description;
    public String type;

    // Constructor for new donations (ID is set by DB)
    public Donation(String donor, double amount, String description, String type) {
        this.donor = donor;
        this.amount = amount;
        this.description = description;
        this.type = type;
    }

    // Constructor for loading from DB (with ID)
    public Donation(int id, String donor, double amount, String description, String type) {
        this(donor, amount, description, type);
        this.id = id;
    }
}