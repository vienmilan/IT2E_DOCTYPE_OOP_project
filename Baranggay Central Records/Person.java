public class Person {
    public int id;
    public String name;
    public String address;
    public String needs;
    public int age;
    public String contact;
    public String category;

    // Constructor for new people (ID is set by DB)
    public Person(String name, String address, String needs, int age, String contact, String category) {
        this.name = name;
        this.address = address;
        this.needs = needs;
        this.age = age;
        this.contact = contact;
        this.category = category;
    }

    // Constructor for loading from DB (with ID)
    public Person(int id, String name, String address, String needs, int age, String contact, String category) {
        this(name, address, needs, age, contact, category);
        this.id = id;
    }
}